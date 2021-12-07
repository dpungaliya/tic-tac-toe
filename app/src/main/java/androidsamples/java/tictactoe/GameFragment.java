package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.List;

public class GameFragment extends Fragment {
  private static final String TAG = "GameFragment";
  private static final int GRID_SIZE = 9;

  private final Button[] mButtons = new Button[GRID_SIZE];
  private NavController mNavController;

  private GameViewModel mGameVM;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Extract the argument passed with the action in a type-safe way
    GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
    Log.d(TAG, "New game type = " + args.getGameType());

    // Handle the back press by adding a confirmation dialog
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        Log.d(TAG, "Back pressed");

        GameStatus gameStatus = mGameVM.getGameStatusLiveData().getValue();
        if (gameStatus == null) {
          mGameVM.forfeit();
          mNavController.popBackStack();
          return;
        }
        if (gameStatus == GameStatus.WON || gameStatus == GameStatus.TIED)  {
          mNavController.popBackStack();
          return;
        }

        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
            .setTitle(R.string.confirm)
            .setMessage(R.string.forfeit_game_dialog_message)
            .setPositiveButton(R.string.yes, (d, which) -> {
              Log.d(TAG, "forfeiting game");
              mGameVM.forfeit();
              mNavController.popBackStack();
            })
            .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
            .create();
        dialog.show();
      }
    };
    requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_game, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Log.d(TAG, "onViewCreated");

    setupUIReferences(view);
    mNavController = Navigation.findNavController(view);

    mGameVM = new ViewModelProvider(this).get(GameViewModel.class);
    if  (!mGameVM.isInitialised()) {
      GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
      if (args.getGameType() == GameType.ONE_PLAYER) {
        Log.d(TAG, "creating 1 player game");
        mGameVM.createGame(GameType.ONE_PLAYER);
      } else if (args.getGameType() == GameType.TWO_PLAYER && args.getIsCreating()) {
        Log.d(TAG, "creating 2 player game as creator");
        mGameVM.createGame(GameType.TWO_PLAYER);
      } else if (args.getGameType() == GameType.TWO_PLAYER && !args.getIsCreating()) {
        Log.d(TAG, "creating 2 player game as joiner");
        mGameVM.joinTwoPlayerGame(args.getGameId());
      }
    }

    mGameVM.getLocationsLiveData().observe(getViewLifecycleOwner(), this::updateButtons);

    mGameVM.getGameStatusLiveData().observe(getViewLifecycleOwner(), gameStatus -> {
      Log.d(TAG, "gameStatus : " + gameStatus);
      if (gameStatus == GameStatus.IN_PROGRESS) return;
      if (mGameVM.hasLocalPlayerWon()) {
        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
            .setMessage(R.string.you_won_message)
            .setPositiveButton(R.string.ok, (d, which) -> {
              mNavController.popBackStack();
            })
            .create();
        dialog.show();
      } else if (mGameVM.hasLocalPlayerLost()) {
        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
            .setMessage(R.string.you_lost_message)
            .setPositiveButton(R.string.ok, (d, which) -> {
              mNavController.popBackStack();
            })
            .create();
        dialog.show();
      } else if (mGameVM.hasLocalPlayerTied()) {
        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
            .setMessage(R.string.you_tied_message)
            .setPositiveButton(R.string.ok, (d, which) -> {
              mNavController.popBackStack();
            })
            .create();
        dialog.show();
      }
    });

    mGameVM.getCurrentPlayerLiveData().observe(getViewLifecycleOwner(), currentPlayer -> {
      Log.d(TAG, "currentPlayer : " + currentPlayer);
    });

    for (int i = 0; i < mButtons.length; i++) {
      int finalI = i;
      mButtons[i].setOnClickListener(v -> {
        Log.d(TAG, "Button " + finalI + " clicked");
        mGameVM.move(finalI / Game.BOARD_SIZE, finalI % Game.BOARD_SIZE);
      });
    }
  }

  private void setupUIReferences(View view) {
    // get game board buttons
    mButtons[0] = view.findViewById(R.id.button0);
    mButtons[1] = view.findViewById(R.id.button1);
    mButtons[2] = view.findViewById(R.id.button2);

    mButtons[3] = view.findViewById(R.id.button3);
    mButtons[4] = view.findViewById(R.id.button4);
    mButtons[5] = view.findViewById(R.id.button5);

    mButtons[6] = view.findViewById(R.id.button6);
    mButtons[7] = view.findViewById(R.id.button7);
    mButtons[8] = view.findViewById(R.id.button8);
  }

  private void updateButtons(List<List<Integer>> locations) {
    for (int i = 0; i < Game.BOARD_SIZE; ++i) {
      for (int j = 0; j < Game.BOARD_SIZE; ++j) {
        int markerNumber = locations.get(i).get(j);
        String markerText = " ";
        if (markerNumber == PlayerType.PLAYER_ONE.getPlayerNumber()) markerText = "X";
        else if (markerNumber == PlayerType.PLAYER_TWO.getPlayerNumber()) markerText = "O";
        mButtons[i * Game.BOARD_SIZE + j].setText(markerText);
      }
    }
  }
}