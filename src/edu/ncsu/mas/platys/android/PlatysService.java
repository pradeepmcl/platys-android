package edu.ncsu.mas.platys.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import edu.ncsu.mas.platys.android.sensor.SensorPoller;

public class PlatysService extends Service {

  private static final String LOCK_NAME_STATIC = "edu.ncsu.mas.platys.android.PlatysService";

  private static volatile PowerManager.WakeLock lockStatic = null;

  synchronized private static PowerManager.WakeLock getLock(Context context) {
    if (lockStatic == null) {
      PowerManager mgr = (PowerManager) context.getApplicationContext().getSystemService(
          Context.POWER_SERVICE);
      lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
      lockStatic.setReferenceCounted(true);
    }

    return lockStatic;
  }

  public static void startSensing(Context context, Intent intent) {
    getLock(context.getApplicationContext()).acquire();
    intent.setClass(context, PlatysService.class);
    context.startService(intent);
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public IBinder onBind(Intent intent) {
    // No binding required.
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    PowerManager.WakeLock lock = getLock(this.getApplicationContext());
    if (!lock.isHeld() || (flags & START_FLAG_REDELIVERY) != 0) {
      lock.acquire();
    }

    new SensorPoller(lock, getApplicationContext()).start();

    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

}
