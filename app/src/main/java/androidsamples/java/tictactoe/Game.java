package androidsamples.java.tictactoe;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Game {
  private static final String TAG = "Game";
  public static final int BOARD_SIZE = 3;
  public static final int DEFAULT_MARKER = 0;

  private PlayerType mCurrentPlayer;
  private List<List<Integer>> mLocations;
  private PlayerType mWinningPlayer;
  private List<HashMap<Integer, Integer>> mRowCounts;
  private List<HashMap<Integer, Integer>> mColumnCounts;
  private HashMap<Integer, Integer> mDiagonalCounts;
  private HashMap<Integer, Integer> mOffDiagonalCounts;
  private GameStatus mGameStatus;

  public Game() {
    Log.d(TAG, "default constructor");
    mCurrentPlayer = PlayerType.PLAYER_ONE;
    mWinningPlayer = null;
    mGameStatus = GameStatus.IN_PROGRESS;

    mLocations = new ArrayList<>(BOARD_SIZE);
    ArrayList<Integer> arrayElement = new ArrayList<>(BOARD_SIZE);
    for (int i = 0; i < BOARD_SIZE; ++i) arrayElement.add(DEFAULT_MARKER);
    for (int i = 0; i < BOARD_SIZE; ++i) //noinspection unchecked
      mLocations.add((List<Integer>) arrayElement.clone());

    mRowCounts = new ArrayList<>(BOARD_SIZE);
    mColumnCounts = new ArrayList<>(BOARD_SIZE);
    for (int i = 0; i < BOARD_SIZE; ++i) {
      mRowCounts.add(new HashMap<>());
      mColumnCounts.add(new HashMap<>());
    }
    mDiagonalCounts = new HashMap<>();
    mOffDiagonalCounts = new HashMap<>();
  }

  public boolean move(PlayerType player, int x, int y) {
    if (mGameStatus != GameStatus.IN_PROGRESS) return false;
    if (player != mCurrentPlayer) throw new  IllegalArgumentException("It is not the given player's turn");
    if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
      throw new IllegalArgumentException("Invalid coordinates. x and y should be between 0 and 2 (both inclusive)");
    }
    if (mLocations.get(x).get(y) != DEFAULT_MARKER) return false;

    mLocations.get(x).set(y, player.getPlayerNumber());

    updateGameStatus(x, y, player);
    if (mGameStatus != GameStatus.IN_PROGRESS) return true;

    if (player == PlayerType.PLAYER_ONE) mCurrentPlayer = PlayerType.PLAYER_TWO;
    else if (player == PlayerType.PLAYER_TWO) mCurrentPlayer = PlayerType.PLAYER_ONE;
    else {
      Log.d(TAG, "unknown player type");
      throw new IllegalArgumentException("Unexpected player type");
    }
    return true;
  }

  public List<List<Integer>> getLocations() {
    List<List<Integer>> returnList = new ArrayList<>(BOARD_SIZE);
    for (int i = 0; i < BOARD_SIZE; ++i) {
      returnList.add(Collections.unmodifiableList(mLocations.get(i)));
    }
    return Collections.unmodifiableList(returnList);
  }

  public PlayerType getWinningPlayer() {
    return mWinningPlayer;
  }

  public GameStatus getGameStatus() {
    return mGameStatus;
  }

  public PlayerType getCurrentPlayer() {
    return mCurrentPlayer;
  }

  private void updateGameStatus(int x, int y, PlayerType player) {
    // for rows
    incrementCountAndUpdateGameStatus(mRowCounts.get(x), player);
    // for columns
    incrementCountAndUpdateGameStatus(mColumnCounts.get(y), player);
    // for diagonal
    if (x == y) incrementCountAndUpdateGameStatus(mDiagonalCounts, player);
    // for off-diagonal
    if (x + y == BOARD_SIZE - 1) incrementCountAndUpdateGameStatus(mOffDiagonalCounts, player);

    checkAndUpdateGameStatusIfTied();
  }

  private void incrementCountAndUpdateGameStatus(HashMap<Integer, Integer> counts, PlayerType player) {
    int playerNumber = player.getPlayerNumber();
    Integer currentValue = counts.get(playerNumber);
    if (currentValue == null) currentValue = 0;
    currentValue++;
    counts.put(playerNumber, currentValue);
    if (currentValue == BOARD_SIZE) {
      mWinningPlayer = player;
      mGameStatus = GameStatus.WON;
    }
  }

  private void checkAndUpdateGameStatusIfTied() {
    int markedCount = 0;
    int[] counts = new int[BOARD_SIZE];
    for (int i = 0; i < BOARD_SIZE; ++i) {
      Integer tempCount = mRowCounts.get(i).get(PlayerType.PLAYER_ONE.getPlayerNumber());
      if (tempCount == null) tempCount = 0;
      markedCount += tempCount;
      tempCount = mRowCounts.get(i).get(PlayerType.PLAYER_TWO.getPlayerNumber());
      if (tempCount == null) tempCount = 0;
      markedCount += tempCount;
    }
    if (mGameStatus == GameStatus.IN_PROGRESS && markedCount == BOARD_SIZE * BOARD_SIZE) mGameStatus = GameStatus.TIED;
  }
}
