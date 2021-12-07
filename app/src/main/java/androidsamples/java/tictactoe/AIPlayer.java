package androidsamples.java.tictactoe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIPlayer implements Player {
  private static final String TAG = "AIPlayer";

  private PlayerType mPlayerType;
  private OnMoveListener mOnMoveListener;
  private Random mRandom;

  public AIPlayer(PlayerType playerType) {
    mPlayerType = playerType;
    mRandom = new Random();
  }

  @Override
  public void setOnMoveListener(OnMoveListener listener) {
    mOnMoveListener = listener;
  }

  @Override
  public void setOnPresenceStatusChangeListener(OnPresenceStatusChangeListener listener) {
    listener.onPresenceStatusChange(PresenceStatus.ONLINE);
  }

  @Override
  public void notifyNextTurn(List<List<Integer>> locations) {
    List<Integer> emptyLocations = new ArrayList<>();
    for (int i = 0; i < Game.BOARD_SIZE; ++i) {
      for (int j = 0; j < Game.BOARD_SIZE; ++j) {
        if (locations.get(i).get(j) == Game.DEFAULT_MARKER) {
          emptyLocations.add(i * Game.BOARD_SIZE + j);
        }
      }
    }

    int randIdx = mRandom.nextInt(emptyLocations.size());
    int chosenIdx = emptyLocations.get(randIdx);
    int chosenX = chosenIdx / Game.BOARD_SIZE;
    int chosenY = chosenIdx % Game.BOARD_SIZE;
    mOnMoveListener.onMove(chosenX, chosenY);
  }

  @Override
  public void onCleared() {
    // nothing to do
  }
}
