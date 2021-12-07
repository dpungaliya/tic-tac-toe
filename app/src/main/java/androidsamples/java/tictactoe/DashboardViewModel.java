package androidsamples.java.tictactoe;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DashboardViewModel extends ViewModel {
  private static final String TAG = "DashboardViewModel";
  private static final int MAX_NUM_OPEN_GAMES_DISPLAYED = 20;

  private final FirebaseDatabase mDB;
  private final Query mOpenGamesQuery;
  private final ValueEventListener mOpenGamesValueEventListener;
  private final MutableLiveData<List<String>> mGameIds;

  public DashboardViewModel() {
    mGameIds = new MutableLiveData<>();
    mDB = FirebaseDatabase.getInstance();
    mOpenGamesQuery = mDB.getReference("games").child("open_games").limitToLast(MAX_NUM_OPEN_GAMES_DISPLAYED);
    mOpenGamesValueEventListener = mOpenGamesQuery.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (snapshot.exists()) {
          Log.d(TAG, "openGamesQuery : ValueEventListener : snapshot is non-null");
          List<String> newValues = new ArrayList<>();
          for (DataSnapshot valueSnapshot : snapshot.getChildren()) {
            newValues.add(valueSnapshot.getKey());
          }
          mGameIds.setValue(newValues);
        } else {
          mGameIds.setValue(new ArrayList<>());
          Log.d(TAG, "openGamesQuery : ValueEventListener : snapshot is null");
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.d(TAG, "openGamesQuery : ValueEventListener : onCancelled : reason : " + error);
      }
    });
  }

  public LiveData<List<String>> getGameIdsLiveData() {
    return mGameIds;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    Log.d(TAG, "onCleared");
    mOpenGamesQuery.removeEventListener(mOpenGamesValueEventListener);
  }
}
