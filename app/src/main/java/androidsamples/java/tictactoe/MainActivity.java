package androidsamples.java.tictactoe;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";
  private UserDetailsViewModel mUserDetailsVM;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mUserDetailsVM = new ViewModelProvider(this).get(UserDetailsViewModel.class);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_logout) {
      Log.d(TAG, "logout clicked");
      mUserDetailsVM.signOut();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}