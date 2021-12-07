package androidsamples.java.tictactoe;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GameViewModel extends ViewModel {
  private static final String TAG = "GameViewModel";

  private final MutableLiveData<List<List<Integer>>> mLocations;
  private final MutableLiveData<PlayerType> mCurrentPlayer;
  private final MutableLiveData<GameStatus> mGameStatus;
  private PlayerType mLocalPlayerType;
  private PlayerType mWinningPlayer;
  private Player mOtherPlayer;
  private final Game mGame;
  private GameType mGameType;
  private final Observer<GameStatus> mGameStatusObserver;
  private String mGameId;

  private final FirebaseDatabase mDB;


  public GameViewModel() {
    Log.d(TAG, "default constructor : GameViewModel");
    mGame = new Game();
    mLocalPlayerType = null;
    mLocations = new MutableLiveData<>(mGame.getLocations());
    mGameStatus = new MutableLiveData<>();
    mCurrentPlayer = new MutableLiveData<>();
    mOtherPlayer = null;
    mWinningPlayer = null;
    mGameType = null;
    mGameId = null;
    mDB = FirebaseDatabase.getInstance();

    mGameStatusObserver = gameStatus -> {
      if (gameStatus == null || gameStatus == GameStatus.IN_PROGRESS || mGameType == null) return;
      String statsKey;
      if (gameStatus == GameStatus.WON && mWinningPlayer == mLocalPlayerType) statsKey = "num_wins";
      else if (gameStatus == GameStatus.WON) statsKey = "num_losses";
      else statsKey = "num_ties";
      mDB.getReference("users")
          .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
          .child("player_stats")
          .child(statsKey)
          .setValue(ServerValue.increment(1));
    };
    mGameStatus.observeForever(mGameStatusObserver);
  }

  public void createGame(@NonNull  GameType gameType) {
    Objects.requireNonNull(gameType);
    mGameType = gameType;
    mLocalPlayerType = PlayerType.PLAYER_ONE;
    if (gameType == GameType.ONE_PLAYER) {
      Log.d(TAG, "creating 1 player game");
      mOtherPlayer = new AIPlayer(PlayerType.PLAYER_TWO);
      mOtherPlayer.setOnMoveListener((x, y) -> {
        if (mGame.move(PlayerType.PLAYER_TWO, x, y)) {
          mWinningPlayer = mGame.getWinningPlayer();
          mCurrentPlayer.setValue(mGame.getCurrentPlayer());
          mGameStatus.setValue(mGame.getGameStatus());
          mLocations.setValue(mGame.getLocations());
        }
      });

      mOtherPlayer.setOnPresenceStatusChangeListener(presenceStatus -> {
        if (presenceStatus == PresenceStatus.ONLINE) {
          mGameStatus.setValue(GameStatus.IN_PROGRESS);
          mCurrentPlayer.setValue(PlayerType.PLAYER_ONE);
        }
      });
    } else {
      // TODO more stuff to do here
      createTwoPlayerGame();
      mOtherPlayer = new FirebasePlayer(PlayerType.PLAYER_TWO, mGameId);

      mOtherPlayer.setOnMoveListener((x, y) -> {
        if (mGame.move(PlayerType.PLAYER_TWO, x, y)) {
          mWinningPlayer = mGame.getWinningPlayer();
          mCurrentPlayer.setValue(mGame.getCurrentPlayer());
          mGameStatus.setValue(mGame.getGameStatus());
          mLocations.setValue(mGame.getLocations());
        }
      });

      mOtherPlayer.setOnPresenceStatusChangeListener(presenceStatus -> {
        if (presenceStatus == PresenceStatus.ONLINE) {
          mGameStatus.setValue(GameStatus.IN_PROGRESS);
          mCurrentPlayer.setValue(PlayerType.PLAYER_ONE);
        }
      });
    }
  }

  public void joinTwoPlayerGame(String gameId) {
    mGameId = gameId;
    mGameType = GameType.TWO_PLAYER;
    mLocalPlayerType = PlayerType.PLAYER_TWO;
    mGameStatus.setValue(GameStatus.IN_PROGRESS);
    mCurrentPlayer.setValue(PlayerType.PLAYER_ONE);

    mDB.getReference("games")
        .child("open_games")
        .removeValue();

    mDB.getReference("games")
        .child("details")
        .child(gameId)
        .child("player_2")
        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());

    mOtherPlayer = new FirebasePlayer(PlayerType.PLAYER_ONE, mGameId);

    mOtherPlayer.setOnMoveListener((x, y) -> {
      Log.d(TAG, "onMoveListener called");
      if (mGame.move(PlayerType.PLAYER_ONE, x, y)) {
        mWinningPlayer = mGame.getWinningPlayer();
        mCurrentPlayer.setValue(mGame.getCurrentPlayer());
        mGameStatus.setValue(mGame.getGameStatus());
        mLocations.setValue(mGame.getLocations());
      }
    });

    mOtherPlayer.setOnPresenceStatusChangeListener(presenceStatus -> {
      Log.d(TAG, "setOnPresenceStatusChangeListener called");
      if (presenceStatus == PresenceStatus.ONLINE) {
      }
    });

    // TODO more stuff here
  }

  public void move(int x, int y) {
    PlayerType currentPlayerType = mCurrentPlayer.getValue();
    if (currentPlayerType == null || currentPlayerType != mLocalPlayerType) return;
    if (mGame.move(mLocalPlayerType, x, y)) {
      mWinningPlayer = mGame.getWinningPlayer();
      mCurrentPlayer.setValue(mGame.getCurrentPlayer());
      mGameStatus.setValue(mGame.getGameStatus());
      mLocations.setValue(mGame.getLocations());

      if (mGameType == GameType.TWO_PLAYER) {
        mDB.getReference("games")
            .child("details")
            .child(mGameId)
            .child("locations")
            .child(Integer.toString(x * Game.BOARD_SIZE + y))
            .setValue(mLocalPlayerType.getPlayerNumber());
      }
      if (mGameStatus.getValue() == GameStatus.IN_PROGRESS) mOtherPlayer.notifyNextTurn(mGame.getLocations());
    }
  }

  public LiveData<List<List<Integer>>> getLocationsLiveData() {
    return mLocations;
  }

  public LiveData<PlayerType> getCurrentPlayerLiveData() {
    return mCurrentPlayer;
  }

  public MutableLiveData<GameStatus> getGameStatusLiveData() {
    return mGameStatus;
  }

  public PlayerType getWinningPlayer() {
    return mWinningPlayer;
  }

  public boolean hasLocalPlayerWon() {
    GameStatus gameStatus = mGameStatus.getValue();
    if (gameStatus == null) return false;
    return gameStatus == GameStatus.WON && mWinningPlayer == mLocalPlayerType;
  }

  public boolean hasLocalPlayerLost() {
    GameStatus gameStatus = mGameStatus.getValue();
    if (gameStatus == null) return false;
    return gameStatus == GameStatus.WON && mWinningPlayer != mLocalPlayerType;
  }

  public boolean hasLocalPlayerTied() {
    GameStatus gameStatus = mGameStatus.getValue();
    if (gameStatus == null) return false;
    return gameStatus == GameStatus.TIED;
  }

  public void forfeit() {
    Log.d(TAG, "forfeit");
    GameStatus gameStatus = mGameStatus.getValue();
    if (mGameType == null) {
      // nothing to do, game not created yet
      Log.d(TAG, "forfeit : gameType is null");
      return;
    }
    if (gameStatus == null) {
      Log.d(TAG, "forfeit : other player has not joined yet");
      // other player has not joined yet
      if (mGameType == GameType.ONE_PLAYER) {
        Log.d(TAG, "forfeit : other player has not joined yet : 1 player : no need for stats update");
        // no need for stats update
      } else {
        Log.d(TAG, "forfeit : 2 player : other player not joined yet : game will be removed : no stats update needed");
        mDB.getReference("games")
            .child("open_games")
            .child(mGameId)
            .removeValue();
      }
    }
    else if (gameStatus == GameStatus.IN_PROGRESS) {
      // other player has joined, game is in progress
      // forfeiting player loses the game
      if (mGameType == GameType.ONE_PLAYER) {
        mDB.getReference("users")
            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .child("player_stats")
            .child("num_losses")
            .setValue(ServerValue.increment(1));
      } else {
        // TODO for two player
      }
    }
  }

  private void createTwoPlayerGame() {
    DatabaseReference gameDetailsRef = mDB.getReference("games")
        .child("details")
        .push();
    mGameId = gameDetailsRef.getKey();

    // required to prevent inconsistent open games data in case client loses network
    mDB.getReference("games")
        .child("open_games")
        .child(mGameId)
        .onDisconnect()
        .removeValue();

    Map<String, Object> childValues = new HashMap<>();
    childValues.put("player_1", FirebaseAuth.getInstance().getCurrentUser().getUid());
    childValues.put("player_2", "");

    Map<String, Object> locationValues = new HashMap<>();
    for (int i = 0; i < Game.BOARD_SIZE * Game.BOARD_SIZE; ++i) {
      locationValues.put(Integer.toString(i), 0);
    }
    childValues.put("locations", locationValues);
    gameDetailsRef.updateChildren(childValues);

    mDB.getReference("games")
        .child("open_games")
        .child(mGameId)
        .setValue(true);
  }

  public boolean isInitialised() {
    return mGameType != null;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    Log.d(TAG, "onCleared");
    mGameStatus.removeObserver(mGameStatusObserver);
    mOtherPlayer.onCleared();
  }
}
