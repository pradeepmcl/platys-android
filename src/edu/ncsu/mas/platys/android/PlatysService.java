package edu.ncsu.mas.platys.android;

import java.util.LinkedList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import edu.ncsu.mas.platys.android.sensor.Sensor;
import edu.ncsu.mas.platys.android.sensor.SensorEnum;
import edu.ncsu.mas.platys.android.sensor.SensorPoller;
import edu.ncsu.mas.platys.android.sync.SyncHandler;

public class PlatysService extends Service {

  private static final String TAG = PlatysService.class.getSimpleName();

  private static final String LOCK_NAME_STATIC = "edu.ncsu.mas.platys.android.PlatysService";

  public static final String PLATYS_ACTION_SENSE = "platys.intent.action.START_SENSING";
  public static final String PLATYS_ACTION_SYNC = "platys.intent.action.START_SYNCING";

  private static volatile PowerManager.WakeLock lockStatic = null;

  private Handler mServiceHandler;

  private Runnable runningTask = null;
  private final List<Runnable> pendingTasks = new LinkedList<Runnable>();

  synchronized private static PowerManager.WakeLock getLock(Context context) {
    if (lockStatic == null) {
      PowerManager mgr = (PowerManager) context.getApplicationContext().getSystemService(
          Context.POWER_SERVICE);
      lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
      lockStatic.setReferenceCounted(true);
    }

    return lockStatic;
  }

  public static void startWakefulAction(Context context, Intent intent) {
    getLock(context.getApplicationContext()).acquire();
    intent.setClass(context, PlatysService.class);
    context.startService(intent);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    mServiceHandler = new PlatysServiceHandler();
  }

  @Override
  public IBinder onBind(Intent intent) {
    // No binding required.
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      String action = intent.getAction();
      Log.i(TAG, "Starting PlatysSerice for action: " + action);

      PowerManager.WakeLock lock = getLock(this.getApplicationContext());

      if (action.equals(PLATYS_ACTION_SENSE)) {
        Log.i(TAG, "Perform Platys sense action.");
        if (!lock.isHeld() || (flags & START_FLAG_REDELIVERY) != 0) {
          lock.acquire();
        }
        pendingTasks.add(new SensorPoller(getApplicationContext(), mServiceHandler, SensorEnum
            .values()));

      } else if (action.equals(PLATYS_ACTION_SYNC)) {
        Log.i(TAG, "Perform Platys sync action.");
        if (!lock.isHeld() || (flags & START_FLAG_REDELIVERY) != 0) {
          lock.acquire();
        }
        pendingTasks.add(new SyncHandler(getApplicationContext(), mServiceHandler));
      }

      runTasks();
    }

    return START_STICKY;
  }

  private void runTasks() {
    if (runningTask == null && !pendingTasks.isEmpty()) {
      Log.i(TAG, "Running next tasks in the queue");
      runningTask = pendingTasks.remove(0);
      new Thread(runningTask).start();
    } else {
      Log.i(TAG, "No more tasks to run. Releasing the lock and stopping the service");
      PowerManager.WakeLock lock = getLock(getApplicationContext());
      if (lock.isHeld()) {
        lock.release();
      }
      stopSelf();
    }
  }

  private final class PlatysServiceHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      Log.i(TAG, "Received message " + msg.arg1);
      if (msg.arg1 == Sensor.MSG_FROM_SENSOR && runningTask instanceof  SensorPoller) {
        Log.i(TAG, "SensorPoller finished.");
        runningTask = null;
      } else if (msg.arg1 == Sensor.MSG_FROM_SENSOR && runningTask instanceof SyncHandler) {
        Log.i(TAG, "SyncHandler finished.");
        runningTask = null;
      }

      runTasks();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

}
