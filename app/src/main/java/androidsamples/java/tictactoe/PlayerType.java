package androidsamples.java.tictactoe;

public enum PlayerType {
  PLAYER_ONE(1), PLAYER_TWO(2);

  private final int mPlayerNumber;

  PlayerType(int playerNumber) {
    this.mPlayerNumber = playerNumber;
  }

  public int getPlayerNumber() {
    return mPlayerNumber;
  }
}
