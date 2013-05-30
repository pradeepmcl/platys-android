package edu.ncsu.mas.platys.android.sensor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import edu.ncsu.mas.platys.android.sync.SyncManager;

public class SensorService extends Service {

  private SensorManager mSensorManager = null;
  private SyncManager mSyncManager = null;

  @Override
  public void onCreate() {
    super.onCreate();
    mSensorManager = new SensorManager(this);
    mSyncManager = new SyncManager(this, mSensorManager);
    // TODO: Test only
    mSyncManager.startSensorSync();
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

    mSensorManager.close();
    mSensorManager = null;

    mSyncManager.close();
    mSyncManager = null;
  }

}
