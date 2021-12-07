package androidsamples.java.tictactoe;

import java.util.List;

public interface Player {
  void setOnMoveListener(OnMoveListener listener);
  void setOnPresenceStatusChangeListener(OnPresenceStatusChangeListener listener);
  void notifyNextTurn(List<List<Integer>> locations);

  interface OnMoveListener {
    void onMove(int x, int y);
  }

  interface OnPresenceStatusChangeListener {
    void onPresenceStatusChange(PresenceStatus presenceStatus);
  }

  void onCleared();
}
