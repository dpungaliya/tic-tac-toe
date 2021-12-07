package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DashboardFragment extends Fragment {

  private static final String TAG = "DashboardFragment";
  private NavController mNavController;

  private TextView mTxtWins;
  private TextView mTxtLosses;
  private TextView mTxtTies;
  private RecyclerView mRecyclerView;
  private TextView mTxtNoGames;

  private UserDetailsViewModel mUserDetailsVM;
  private DashboardViewModel mDashboardVM;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public DashboardFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    setHasOptionsMenu(true); // Needed to display the action menu for this fragment
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_dashboard, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mNavController = Navigation.findNavController(view);
    setupUIReferences(view);

    mUserDetailsVM = new ViewModelProvider(requireActivity()).get(UserDetailsViewModel.class);
    mUserDetailsVM.getLoginStatusLiveData().observe(getViewLifecycleOwner(), loginStatus -> {
      if (loginStatus != LoginStatus.LOGGED_IN) {
        Navigation.findNavController(view).navigate(DashboardFragmentDirections.actionNeedAuth());
      }
    });
    mUserDetailsVM.getNumWinsLiveData().observe(getViewLifecycleOwner(), numWins -> {
      mTxtWins.setText(getString(R.string.wins) + "\n" + numWins);
    });
    mUserDetailsVM.getNumTiesLiveData().observe(getViewLifecycleOwner(), numTies -> {
      mTxtTies.setText(getString(R.string.ties) + "\n" + numTies);
    });
    mUserDetailsVM.getNumLossesLiveData().observe(getViewLifecycleOwner(), numLosses -> {
      mTxtLosses.setText(getString(R.string.losses) + "\n" + numLosses);
    });

    mDashboardVM = new ViewModelProvider(this).get(DashboardViewModel.class);

    OpenGamesAdapter.ClickListener openGamesClickListener = gameId -> {
      NavDirections action = DashboardFragmentDirections.actionGame(GameType.TWO_PLAYER,
          gameId,
          false);
      Navigation.findNavController(view).navigate(action);
    };

    mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
    OpenGamesAdapter adapter = new OpenGamesAdapter(openGamesClickListener);
    mRecyclerView.setAdapter(adapter);

    mDashboardVM.getGameIdsLiveData().observe(getViewLifecycleOwner(), gameIds -> {
      if (gameIds != null) {
        Log.d(TAG, "gameIdsLiveData changed. Now has " + gameIds.size() + " entries.");
        adapter.setValues(gameIds);
        if (gameIds.size() == 0) mTxtNoGames.setVisibility(View.VISIBLE);
        else mTxtNoGames.setVisibility(View.GONE);
      } else {
        mTxtNoGames.setVisibility(View.VISIBLE);
        Log.d(TAG, "gameIdsLiveData changed to null");
      }
    });

    // Show a dialog when the user clicks the "new game" button
    view.findViewById(R.id.fab_new_game).setOnClickListener(v -> {

      // A listener for the positive and negative buttons of the dialog
      DialogInterface.OnClickListener listener = (dialog, which) -> {
        GameType gameType;
        if (which == DialogInterface.BUTTON_POSITIVE) {
          gameType = GameType.TWO_PLAYER;
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
          gameType = GameType.ONE_PLAYER;
        } else {
          Log.d(TAG, "Create game dialog : unknown option selected");
          return;
        }
        Log.d(TAG, "Create game dialog : selected game type : " + gameType);

        Boolean connected = mUserDetailsVM.getConnectionStatusLiveData().getValue();
        if (connected == null) {
          Log.d(TAG, "connected was null");
          return;
        }
        else if (!connected) {
          Toast.makeText(requireActivity(), R.string.err_cannot_create_game, Toast.LENGTH_LONG).show();
          return;
        }

        // Passing the game type as a parameter to the action
        // extract it in GameFragment in a type safe way
        NavDirections action = DashboardFragmentDirections.actionGame(gameType,
            null,
            true);
        mNavController.navigate(action);
      };

      // create the dialog
      AlertDialog dialog = new AlertDialog.Builder(requireActivity())
          .setTitle(R.string.new_game)
          .setMessage(R.string.new_game_dialog_message)
          .setPositiveButton(R.string.two_player, listener)
          .setNegativeButton(R.string.one_player, listener)
          .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
          .create();
      dialog.show();
    });
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }

  /**
   * Sets up references to all the UI elements
   * @param view The view on which the elements reside
   */
  private void setupUIReferences(@NonNull View view) {
    mTxtWins = view.findViewById(R.id.txt_num_wins);
    mTxtLosses = view.findViewById(R.id.txt_num_losses);
    mTxtTies = view.findViewById(R.id.txt_num_ties);
    mTxtNoGames = view.findViewById(R.id.txt_no_games);
    mRecyclerView = view.findViewById(R.id.list);
  }
}