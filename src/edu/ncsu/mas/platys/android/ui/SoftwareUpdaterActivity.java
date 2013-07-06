package edu.ncsu.mas.platys.android.ui;

import edu.ncsu.mas.platys.android.PlatysReceiver;
import edu.ncsu.mas.platys.android.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.Window;

public class SoftwareUpdaterActivity extends Activity {
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
    // Start sync and software update
    Intent platysUpdateSwIntent = new Intent(getApplicationContext(), PlatysReceiver.class);
    platysUpdateSwIntent.setAction(PlatysReceiver.ACTION_SYNC_AND_UPDATE_SW);
    sendBroadcast(platysUpdateSwIntent);
    
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.software_updater, menu);
    return true;
  }

}
