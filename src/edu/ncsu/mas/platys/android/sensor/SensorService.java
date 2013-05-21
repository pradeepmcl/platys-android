package edu.ncsu.mas.platys.android.sensor;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class SensorService extends IntentService {
  
  private WiFiAccessPointSensor mWiFiAccessPointSensor;
  
  public SensorService() {
    super("SensorService");
  }
  
  @Override
  public void onCreate() {
    super.onCreate();
    mWiFiAccessPointSensor = new WiFiAccessPointSensor(this);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.i("Pradeep", "onHandleIntent");
    mWiFiAccessPointSensor.sense();
  }
  
  @Override
  public void onDestroy() {
    super.onDestroy();
    mWiFiAccessPointSensor.cleanUp();
  }

}
