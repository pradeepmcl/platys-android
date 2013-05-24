package edu.ncsu.mas.platys.android;

import edu.ncsu.mas.platys.android.sensor.SensorService;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HomeActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    startService(new Intent(this, SensorService.class));
    Button tempButton = (Button) findViewById(R.id.tempButton);
    tempButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.home, menu);
    return true;
  }

}
