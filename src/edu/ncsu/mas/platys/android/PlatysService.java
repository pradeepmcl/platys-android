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
  
  public static final int PLATYS_MSG_SENSE_FINISHED = 0;
  public static final int PLATYS_MSG_SYNC_FINISHED = 1;


  private static volatile PowerManager.WakeLock lockStatic = null;

  private Handler mServiceHandler;

  private Thread runningThread = null;
  private final List<Thread> pendingThreads = new LinkedList<Thread>();

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
        pendingThreads.add(new SensorPoller(getApplicationContext(), mServiceHandler, SensorEnum
            .values()));

      } else if (action.equals(PLATYS_ACTION_SYNC)) {
        Log.i(TAG, "Perform Platys sync action.");
        if (!lock.isHeld() || (flags & START_FLAG_REDELIVERY) != 0) {
          lock.acquire();
        }
        pendingThreads.add(new Thread(new SyncHandler(getApplicationContext(), mServiceHandler)));
      }

      runTasks();
    }

    return START_STICKY;
  }

  private void runTasks() {
    if (runningThread == null) {
      if (pendingThreads.isEmpty()) {
        Log.i(TAG, "No more tasks to run. Releasing the lock and stopping the service.");
        PowerManager.WakeLock lock = getLock(getApplicationContext());
        if (lock.isHeld()) {
          lock.release();
        }
        stopSelf();

      } else {
        Log.i(TAG, "Running next thread in the queue.");
        runningThread = pendingThreads.remove(0);
        runningThread.start();
      }
    } else {
      Log.i(TAG, "Waiting for running thread to finish.");
      // Do nothing. Wait for running thread to finish.
    }
  }

  private final class PlatysServiceHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      Log.i(TAG, "Received message " + msg.arg1);
      if (msg.what == PlatysService.PLATYS_MSG_SENSE_FINISHED) {
        Log.i(TAG, "SensorPoller finished.");
        runningThread = null;
      } else if (msg.what == PlatysService.PLATYS_MSG_SYNC_FINISHED) {
        Log.i(TAG, "SyncHandler finished.");
        runningThread = null;
      }

      runTasks();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

}
