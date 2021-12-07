package androidsamples.java.tictactoe;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.FirebaseDatabase;

public class LoginFragment extends Fragment {
  private static final String TAG = "LoginFragment";

  private UserDetailsViewModel mUserDetailVM;
  private EditText mEditEmail;
  private EditText mEditPassword;
  private Button mBtnLogIn;
  private Button mBtnSignUp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        view.findViewById(R.id.btn_log_in)
                .setOnClickListener(v -> {
                    NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                    Navigation.findNavController(v).navigate(action);
                });

        return view;
    }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Log.d(TAG, "onViewCreated");

    setupUIReferences(view);

    mUserDetailVM = new ViewModelProvider(requireActivity()).get(UserDetailsViewModel.class);

    mUserDetailVM.getLoginStatusLiveData().observe(getViewLifecycleOwner(), loginStatus -> {
      Integer messageResId = null;
      switch(loginStatus) {
        case LOGGED_OUT:
          break;
        case LOGGED_IN:
          messageResId = R.string.MSG_AUTH_LOG_IN_SUCCESS;
          Navigation.findNavController(view).navigate(LoginFragmentDirections.actionLoginSuccessful());
          break;
        case INCORRECT_PASSWORD:
          messageResId = R.string.MSG_AUTH_INCORRECT_PASSWORD;
          break;
        case MALFORMED_EMAIL:
          messageResId = R.string.MSG_AUTH_MALFORMED_EMAIL;
          break;
        case WEAK_PASSWORD:
          messageResId = R.string.MSG_AUTH_WEAK_PASSWORD;
          break;
        case ACCOUNT_ALREADY_EXISTS:
          messageResId = R.string.MSG_AUTH_ACCOUNT_ALREADY_EXISTS;
          break;
        case ACCOUNT_DOES_NOT_EXIST:
          messageResId = R.string.MSG_AUTH_ACCOUNT_DOES_NOT_EXIST;
          break;
        case NETWORK_ERROR:
          messageResId = R.string.MSG_AUTH_NETWORK_ERROR;
          break;
        case UNKNOWN_ERROR:
          messageResId = R.string.MSG_AUTH_UNKNOWN_ERROR;
      }
      if (messageResId != null) {
        Toast.makeText(requireActivity(), messageResId, Toast.LENGTH_LONG).show();
      }
    });

    mBtnLogIn.setOnClickListener(btnView -> {
      String email = mEditEmail.getText().toString();
      String password = mEditPassword.getText().toString();
      mUserDetailVM.signIn(email, password);
    });

    mBtnSignUp.setOnClickListener(btnView -> {
      String email = mEditEmail.getText().toString();
      String password = mEditPassword.getText().toString();
      mUserDetailVM.signUp(email, password);
    });
  }

  /**
   * Sets up references to all the UI elements
   * @param view The view on which the elements reside
   */
  private void setupUIReferences(@NonNull View view) {
      mEditEmail = view.findViewById(R.id.edit_email);
      mEditPassword = view.findViewById(R.id.edit_password);
      mBtnLogIn = view.findViewById(R.id.btn_log_in);
      mBtnSignUp = view.findViewById(R.id.btn_sign_up);
  }
}