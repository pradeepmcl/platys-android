package edu.ncsu.mas.platys.android;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import edu.ncsu.mas.platys.android.labels.PlaceLabelSaver;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;
import edu.ncsu.mas.platys.android.sensor.SensorEnum;
import edu.ncsu.mas.platys.android.sensor.SensorPoller;
import edu.ncsu.mas.platys.android.software.SoftwareUpdater;
import edu.ncsu.mas.platys.android.sync.DbxCoreApiSyncer;

public class PlatysService extends Service {

  private static final String TAG = PlatysService.class.getSimpleName();

  public enum PlatysTask {
    PLATYS_TASK_SENSE(PlatysReceiver.ACTION_SENSE),
    PLATYS_TASK_SYNC(PlatysReceiver.ACTION_SYNC),
    PLATYS_TASK_SAVE_LABELS(PlatysReceiver.ACTION_SAVE_LABELS),
    PLATYS_CHECK_SOFTWARE_UPDATES(PlatysReceiver.ACTION_UPDATE_SW),
    UNKNOWN_TASK ("Unknown_Task");

    String mValue;

    PlatysTask(String value) {
      mValue = value;
    }

    @Override
    public String toString() {
      return mValue;
    }

    public static PlatysTask fromString(String value) {
      for (PlatysTask task : PlatysTask.values()) {
        if (task.toString().equalsIgnoreCase(value)) {
          return task;
        }
      }
      // throw new IllegalArgumentException();
      return UNKNOWN_TASK;
    }
  }

  private static final String LOCK_NAME_STATIC = "edu.ncsu.mas.platys.android.PlatysService";

  private static volatile PowerManager.WakeLock lockStatic = null;

  private Handler mServiceHandler = null;
  private SensorDbHelper mSensorDbHelper = null;

  private Thread runningSequentialTaskThread = null;
  private final List<Intent> pendingSequentialTaskIntents = new LinkedList<Intent>();
  private final Map<PlatysTask, Thread> runningParallelTasksMap = new HashMap<PlatysTask, Thread>();

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
    // No binding required yet.
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      Log.i(TAG, "Starting PlatysSerice for action: " + intent.getAction());
      startTask(intent);
    }

    return START_REDELIVER_INTENT;
  }

  private void startTask(Intent intent) {
    String taskStr = intent.getAction();
    PlatysTask platysTask = PlatysTask.fromString(taskStr);

    switch (platysTask) {
    case PLATYS_CHECK_SOFTWARE_UPDATES:
      startTaskInParallel(platysTask, intent);
      break;
    case PLATYS_TASK_SAVE_LABELS:
      startTaskInSequence(platysTask, intent);
      break;
    case PLATYS_TASK_SENSE:
      startTaskInSequence(platysTask, intent);
      break;
    case PLATYS_TASK_SYNC:
      startTaskInSequence(platysTask, intent);
      break;
    default:
      break;
    }
  }

  private void startTaskInParallel(PlatysTask platysTask, Intent intent) {
    PowerManager.WakeLock lock = getLock(this.getApplicationContext());
    if (!lock.isHeld()) { // ( && flags & START_FLAG_REDELIVERY) != 0
      lock.acquire();
      Log.i(TAG, "Acquired lock");
    }

    switch (platysTask) {
    case PLATYS_CHECK_SOFTWARE_UPDATES:
      Log.i(TAG, "Perform Platys software update action.");
      runningParallelTasksMap.put(platysTask, new Thread(new SoftwareUpdater(
          getApplicationContext(), mServiceHandler)));
      runningParallelTasksMap.get(platysTask).start();
      break;

    default: // This should never happen
      break;
    }
  }

  private void startTaskInSequence(PlatysTask platysTask, Intent intent) {
    if (runningSequentialTaskThread == null) {
      PowerManager.WakeLock lock = getLock(this.getApplicationContext());
      if (!lock.isHeld()) { // ( && flags & START_FLAG_REDELIVERY) != 0
        lock.acquire();
        Log.i(TAG, "Acquired lock");
      }

      if (mSensorDbHelper == null || !mSensorDbHelper.isOpen()) {
        mSensorDbHelper = OpenHelperManager
            .getHelper(getApplicationContext(), SensorDbHelper.class);
      }

      switch (platysTask) {
      case PLATYS_TASK_SAVE_LABELS:
        Log.i(TAG, "Perform Platys save labels action.");
        runningSequentialTaskThread = new Thread(new PlaceLabelSaver(mServiceHandler,
            mSensorDbHelper, intent));
        runningSequentialTaskThread.start();
        break;

      case PLATYS_TASK_SENSE:
        Log.i(TAG, "Perform Platys sense action.");
        runningSequentialTaskThread = new SensorPoller(getApplicationContext(), mServiceHandler,
            mSensorDbHelper, SensorEnum.values());
        runningSequentialTaskThread.start();
        break;

      case PLATYS_TASK_SYNC:
        Log.i(TAG, "Perform Platys sync action.");
        runningSequentialTaskThread = new Thread(new DbxCoreApiSyncer(getApplicationContext(),
            mServiceHandler, OpenHelperManager.getHelper(getApplicationContext(),
                SensorDbHelper.class)));
        runningSequentialTaskThread.start();
        break;

      default: // This should never happen
        break;
      }

    } else {
      pendingSequentialTaskIntents.add(intent);
    }
  }

  private final class PlatysServiceHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      Log.i(TAG, "Received message " + msg.arg1);

      PlatysTask msgFromTask = PlatysTask.values()[msg.what];
      switch (msgFromTask) {
      case PLATYS_CHECK_SOFTWARE_UPDATES:
        Log.i(TAG, "Software update finished.");
        runningParallelTasksMap.remove(msgFromTask);
        break;

      case PLATYS_TASK_SAVE_LABELS:
        Log.i(TAG, "LabelSaver finished.");
        runningSequentialTaskThread = null;
        break;

      case PLATYS_TASK_SENSE:
        Log.i(TAG, "SensorPoller finished.");
        runningSequentialTaskThread = null;
        break;

      case PLATYS_TASK_SYNC:
        Log.i(TAG, "SyncHandler finished.");
        OpenHelperManager.releaseHelper();
        runningSequentialTaskThread = null;
        break;
      default:
        break;
      }

      if (!pendingSequentialTaskIntents.isEmpty()) {
        startTask(pendingSequentialTaskIntents.remove(0));
      } else {
        if (runningSequentialTaskThread != null || !runningParallelTasksMap.isEmpty()) {
          // Wait for running tasks to finish.
          Log.i(TAG, "Waiting for running tasks to finish");
        } else {
          PlatysService.this.stopSelf();
        }
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    OpenHelperManager.releaseHelper();
    PowerManager.WakeLock lock = getLock(getApplicationContext());
    if (lock.isHeld()) {
      Log.i(TAG, "Releasing lock");
      lock.release();
    }
  }

}
