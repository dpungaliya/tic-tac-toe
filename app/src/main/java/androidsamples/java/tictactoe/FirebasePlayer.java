package androidsamples.java.tictactoe;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class FirebasePlayer implements Player {
  private static final String TAG = "FirebasePlayer";

  private final FirebaseDatabase mDB;
  private final PlayerType mPlayerType;
  private OnMoveListener mOnMoveListener;
  private OnPresenceStatusChangeListener mOnPresenceStatusChangeListener;
  private final String mGameId;
  private final ValueEventListener mFirebasePlayerPresenceListener;
  private final ChildEventListener mFirebaseMoveEventListener;
  private String mOtherPlayerUid;

  public FirebasePlayer(PlayerType playerType, String gameId) {
    mPlayerType = playerType;
    mGameId = gameId;
    mDB = FirebaseDatabase.getInstance();
    mOtherPlayerUid = null;


    String playerKey = (playerType == PlayerType.PLAYER_ONE) ? "player_1" : "player_2";
    mFirebasePlayerPresenceListener = new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (snapshot.exists()) {
          String value = snapshot.getValue(String.class);
          if (value == null) Log.d(TAG, "mFirebasePlayerPresenceListener snapshot exists but value was null");
          else if (value.equals("")) Log.d(TAG, "mFirebasePlayerPresenceListener snapshot exists but value was empty");
          else {
            mOtherPlayerUid = value;
            mOnPresenceStatusChangeListener.onPresenceStatusChange(PresenceStatus.ONLINE);
          }
        } else {
          Log.d(TAG, "mFirebasePlayerPresenceListener snapshot was null");
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.d(TAG, "mFirebasePlayerPresenceListener was cancelled reason: " + error);
      }
    };
    mDB.getReference("games")
        .child("details")
        .child(gameId)
        .child(playerKey)
        .addValueEventListener(mFirebasePlayerPresenceListener);

    mFirebaseMoveEventListener = new ChildEventListener() {
      @Override
      public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        onChildChanged(snapshot, previousChildName);
      }

      @Override
      public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        if (!snapshot.exists() || snapshot.getValue() == null) {
          Log.d(TAG, "mFirebaseMoveEventListener : snapshot doesn't exist or was null");
        } else {
          String key = snapshot.getKey();
          Integer value = snapshot.getValue(Integer.class);
          if (value == null || key == null) {
            Log.d(TAG, "mFirebaseMoveEventListener : key was null or value was null");
          }
          else if (value == mPlayerType.getPlayerNumber()) {
            int intKey = Integer.parseInt(key);
            mOnMoveListener.onMove(intKey / Game.BOARD_SIZE, intKey % Game.BOARD_SIZE);
          }
        }
      }

      @Override
      public void onChildRemoved(@NonNull DataSnapshot snapshot) {
      }

      @Override
      public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.d(TAG, "mFirebaseMoveEventListener was cancelled");
      }
    };
    mDB.getReference("games")
        .child("details")
        .child(mGameId)
        .child("locations")
        .addChildEventListener(mFirebaseMoveEventListener);
  }

  @Override
  public void setOnMoveListener(OnMoveListener listener) {
    mOnMoveListener = listener;
  }

  @Override
  public void setOnPresenceStatusChangeListener(OnPresenceStatusChangeListener listener) {
    mOnPresenceStatusChangeListener = listener;
  }

  @Override
  public void notifyNextTurn(List<List<Integer>> locations) {
    // nothing to do, DB changes will notify automatically
  }

  @Override
  public void onCleared() {
    String playerKey = (mPlayerType == PlayerType.PLAYER_ONE) ? "player_1" : "player_2";
    mDB.getReference("games")
        .child("details")
        .child(mGameId)
        .child(playerKey)
        .addValueEventListener(mFirebasePlayerPresenceListener);

    mDB.getReference("games")
        .child("details")
        .child(mGameId)
        .child("locations")
        .removeEventListener(mFirebaseMoveEventListener);
  }
}
