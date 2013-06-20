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

import com.j256.ormlite.android.apptools.OpenHelperManager;

import edu.ncsu.mas.platys.android.PlatysReceiver.PlatysTask;
import edu.ncsu.mas.platys.android.labels.PlaceLabelSaver;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;
import edu.ncsu.mas.platys.android.sensor.SensorEnum;
import edu.ncsu.mas.platys.android.sensor.SensorPoller;
import edu.ncsu.mas.platys.android.sync.DbxSyncer;

public class PlatysService extends Service {

  private static final String TAG = PlatysService.class.getSimpleName();

  private static final String LOCK_NAME_STATIC = "edu.ncsu.mas.platys.android.PlatysService";

  public static final int PLATYS_MSG_SENSE_FINISHED = 0;
  public static final int PLATYS_MSG_SYNC_FINISHED = 1;
  public static final int PLATYS_MSG_SAVE_LABELS_FINISHED = 2;

  private static volatile PowerManager.WakeLock lockStatic = null;

  private Handler mServiceHandler = null;
  private SensorDbHelper mSensorDbHelper = null;

  private Thread runningThread = null;
  private final List<Intent> pendingTasks = new LinkedList<Intent>();

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
    mSensorDbHelper = OpenHelperManager.getHelper(getApplicationContext(), SensorDbHelper.class);
  }

  @Override
  public IBinder onBind(Intent intent) {
    // No binding required.
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      Log.i(TAG, "Starting PlatysSerice for action: " + intent.getAction());
      pendingTasks.add(intent);
      runAPendingTask();
    }

    return START_STICKY;
  }

  private void runAPendingTask() {
    if (runningThread == null) {
      if (pendingTasks.isEmpty()) {
        Log.i(TAG, "No more tasks to run. Releasing the lock and stopping the service.");
        PowerManager.WakeLock lock = getLock(getApplicationContext());
        if (lock.isHeld()) {
          lock.release();
        }

        stopSelf();

      } else {
        Log.i(TAG, "Running next task in the queue.");
        PowerManager.WakeLock lock = getLock(this.getApplicationContext());
        if (!lock.isHeld()) { // ( && flags & START_FLAG_REDELIVERY) != 0
          lock.acquire();
        }

        if (mSensorDbHelper == null || !mSensorDbHelper.isOpen()) {
          mSensorDbHelper = OpenHelperManager.getHelper(getApplicationContext(),
              SensorDbHelper.class);
        }

        Intent pendingTaskIntent = pendingTasks.remove(0);
        PlatysReceiver.PlatysTask platysTask = (PlatysTask) pendingTaskIntent
            .getSerializableExtra(PlatysReceiver.EXTRA_TASK);

        switch (platysTask) {
        case PLATYS_TASK_SENSE:
          Log.i(TAG, "Perform Platys sense action.");
          runningThread = new SensorPoller(getApplicationContext(), mServiceHandler,
              mSensorDbHelper, SensorEnum.values());
          runningThread.start();
          break;
        case PLATYS_TASK_SYNC:
          Log.i(TAG, "Perform Platys sync action.");
          runningThread = new Thread(new DbxSyncer(getApplicationContext(), mServiceHandler,
              OpenHelperManager.getHelper(getApplicationContext(), SensorDbHelper.class)));
          runningThread.start();
          break;
        case PLATYS_TASK_SAVE_LABELS:
          Log.i(TAG, "Perform Platys save labels action.");
          runningThread = new Thread(new PlaceLabelSaver(getApplicationContext(), mServiceHandler,
              mSensorDbHelper, pendingTaskIntent));
          runningThread.start();
          break;
        default:
          break;
        }
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
        OpenHelperManager.releaseHelper();
        runningThread = null;
        
      } else if (msg.what == PlatysService.PLATYS_MSG_SAVE_LABELS_FINISHED) {
        Log.i(TAG, "LabelSaver finished.");
        runningThread = null;
      }

      runAPendingTask();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    OpenHelperManager.releaseHelper();
    PowerManager.WakeLock lock = getLock(getApplicationContext());
    if (lock.isHeld()) {
      lock.release();
    }
  }

}
