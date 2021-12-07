package androidsamples.java.tictactoe;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A {@link ViewModel} for storing the details of the user
 */
public class UserDetailsViewModel extends ViewModel {

    private static final String TAG = "UserDetailsViewModel";

    private String mUID;
    private final MutableLiveData<Integer> mNumWins;
    private final MutableLiveData<Integer> mNumLosses;
    private final MutableLiveData<Integer> mNumTies;
    private final SingleLiveEvent<LoginStatus> mLoginStatus;
    private final ChildEventListener mUserChildEventListener;
    private final ValueEventListener mConnectionStateListener;
    private final FirebaseDatabase mDB;
    private final FirebaseAuth mAuth;
    private final FirebaseAuth.AuthStateListener mAuthStateListener;
    private final SingleLiveEvent<Boolean> mConnectionStatus;

    public UserDetailsViewModel() {
        mDB = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mUID = null;
        mNumWins = new MutableLiveData<>(0);
        mNumLosses = new MutableLiveData<>(0);
        mNumTies = new MutableLiveData<>(0);
        mLoginStatus = new SingleLiveEvent<>();
        mConnectionStatus = new SingleLiveEvent<>();

        if (mAuth.getCurrentUser() == null) mLoginStatus.setValue(LoginStatus.LOGGED_OUT);
        else mLoginStatus.setValue(LoginStatus.LOGGED_IN);

        mUserChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (!snapshot.exists()) {
                    Log.d(TAG, "users : childEventListener : non existent snapshot");
                    return;
                }
                if (snapshot.getKey() == null) {
                    Log.d(TAG, "users : childEventListener : received null as snapshot");
                    return;
                }
                switch(snapshot.getKey()) {
                    case "num_wins":
                        mNumWins.setValue(snapshot.getValue(Integer.class));
                        break;
                    case "num_losses":
                        mNumLosses.setValue(snapshot.getValue(Integer.class));
                        break;
                    case "num_ties":
                        mNumTies.setValue(snapshot.getValue(Integer.class));
                        break;
                    default:
                        Log.d(TAG, "users : childEventListener : unexpected key : " + snapshot.getKey());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                onChildAdded(snapshot, previousChildName);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "users : childEventListener : onCancelled : " + error);
            }
        };

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Log.d(TAG, "authStateChanged : user logged in");
                    String uid = firebaseAuth.getCurrentUser().getUid();
                    mUID = uid;
                    mDB.getReference("users").child(uid).child("player_stats")
                        .addChildEventListener(mUserChildEventListener);
                    mLoginStatus.setValue(LoginStatus.LOGGED_IN);
                } else {
                    if (mUID != null) {
                        Log.d(TAG, "authStateChanged : user logged out");
                        mDB.getReference("users").child(mUID).child("player_stats")
                            .removeEventListener(mUserChildEventListener);
                        mUID = null;
                        mLoginStatus.setValue(LoginStatus.LOGGED_OUT);
                    }
                }
            }
        };

        mAuth.addAuthStateListener(mAuthStateListener);

        mConnectionStateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                if (connected == null) {
                    Log.d(TAG, "mConnectionStateListener connected was null");
                    return;
                }
                mConnectionStatus.setValue(connected);
                Log.d(TAG, "mConnectionStateListener : connected : " + connected);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "mConnectionStateListener cancelled");
            }
        };

        mDB.getReference(".info/connected")
            .addValueEventListener(mConnectionStateListener);
    }

    public LiveData<Integer> getNumWinsLiveData() {
        return mNumWins;
    }

    public LiveData<Integer> getNumLossesLiveData() {
        return mNumLosses;
    }

    public LiveData<Integer> getNumTiesLiveData() {
        return mNumTies;
    }

    public LiveData<LoginStatus> getLoginStatusLiveData() {
        return mLoginStatus;
    }

    public LiveData<Boolean> getConnectionStatusLiveData() {
        return mConnectionStatus;
    }

    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Sign in success : signed in as " + email);
            } else {
                Log.d(TAG, "Sign in failed : reason : " + task.getException());
                handleSignInException(task.getException());
            }
        });
    }

    public void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Signup success. Signed up as " + email);
                String uid = task.getResult().getUser().getUid();
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                db.getReference("users").child(uid).child("player_stats").child("num_wins").setValue(0);
                db.getReference("users").child(uid).child("player_stats").child("num_ties").setValue(0);
                db.getReference("users").child(uid).child("player_stats").child("num_losses").setValue(0);
            } else {
                Log.d(TAG, "Signup failed : reason : " + task.getException());
                handleSignUpException(task.getException());
            }
        });
    }

    public void signOut() {
        mAuth.signOut();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mAuth.removeAuthStateListener(mAuthStateListener);
        if (mUID != null) {
            mDB.getReference("users").child(mUID).child("player_stats")
                .removeEventListener(mUserChildEventListener);
        }
        mDB.getReference(".info/connected")
            .removeEventListener(mConnectionStateListener);
    }

    private void handleSignUpException(Exception ex) {
        LoginStatus newStatus = LoginStatus.UNKNOWN_ERROR;
        if (ex instanceof FirebaseNetworkException) {
            newStatus = LoginStatus.NETWORK_ERROR;
        } else if (ex instanceof FirebaseAuthWeakPasswordException) {
            newStatus = LoginStatus.WEAK_PASSWORD;
        } else if (ex instanceof FirebaseAuthInvalidCredentialsException) {
            newStatus = LoginStatus.MALFORMED_EMAIL;
        } else if (ex instanceof FirebaseAuthUserCollisionException) {
            newStatus = LoginStatus.ACCOUNT_ALREADY_EXISTS;
        }
        mLoginStatus.setValue(newStatus);
    }

    private void handleSignInException(Exception ex) {
        LoginStatus newStatus = LoginStatus.UNKNOWN_ERROR;
        if (ex instanceof FirebaseNetworkException) {
            newStatus = LoginStatus.NETWORK_ERROR;
        } else if (ex instanceof FirebaseAuthInvalidCredentialsException) {
            FirebaseAuthInvalidCredentialsException invalidCredentialsException = (FirebaseAuthInvalidCredentialsException) ex;
            String errorCode = invalidCredentialsException.getErrorCode();
            if (errorCode.equals("ERROR_WRONG_PASSWORD")) newStatus = LoginStatus.INCORRECT_PASSWORD;
            else if (errorCode.equals("ERROR_INVALID_EMAIL")) newStatus = LoginStatus.MALFORMED_EMAIL;
        } else if (ex instanceof FirebaseAuthInvalidUserException) {
            newStatus = LoginStatus.ACCOUNT_DOES_NOT_EXIST;
        }
        mLoginStatus.setValue(newStatus);
    }
}
