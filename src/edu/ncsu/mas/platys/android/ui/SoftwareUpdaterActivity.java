package edu.ncsu.mas.platys.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import edu.ncsu.mas.platys.android.PlatysReceiver;
import edu.ncsu.mas.platys.android.R;

public class SoftwareUpdaterActivity extends Activity {

  private static final String TAG = "Platys" + SoftwareUpdaterActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

    setContentView(R.layout.activity_software_updater);

    Log.i(TAG, "Updating the Platys software");

    // Start sync and software update
    Intent platysUpdateSwIntent = new Intent(getApplicationContext(), PlatysReceiver.class);
    platysUpdateSwIntent.setAction(PlatysReceiver.ACTION_UPDATE_SW);
    sendBroadcast(platysUpdateSwIntent);

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.software_updater, menu);
    return true;
  }

}
