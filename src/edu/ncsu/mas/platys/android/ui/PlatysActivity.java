package edu.ncsu.mas.platys.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import edu.ncsu.mas.platys.android.R;

public class PlatysActivity extends Activity {
  private final String mServerMode = null;
  private final String mUserName = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    startActivity(new Intent(this, ServerModeChooserActivity.class));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.platys_home, menu);
    return true;
  }

}
