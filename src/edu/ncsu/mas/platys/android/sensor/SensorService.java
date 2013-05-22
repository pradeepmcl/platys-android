package edu.ncsu.mas.platys.android.sensor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SensorService extends Service {

  private SensorManager mSensorManager = null;

  @Override
  public void onCreate() {
    super.onCreate();
    mSensorManager = new SensorManager(this);
    mSensorManager.init();
  }

  @Override
  public IBinder onBind(Intent intent) {
    // No binding required.
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mSensorManager.cleanUp();
    mSensorManager = null;
  }

}
