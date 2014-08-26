package edu.ncsu.mas.platys.android;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import edu.ncsu.mas.platys.android.sync.DbxCoreApiSyncer;
import edu.ncsu.mas.platys.android.updates.SoftwareUpdatesChecker;

public class PlatysService extends Service {

  private static final String TAG = PlatysService.class.getSimpleName();

  // Don't know the significance!
  private static final String WAKE_LOCK_NAME = PlatysService.class.getName();

  private static volatile PowerManager.WakeLock mWakelock = null;

  private final Map<PlatysTask, Intent> pendingTasks = new LinkedHashMap<PlatysTask, Intent>();
  private final Map<PlatysTask, PlatysTaskHandler> runningTasks = new HashMap<PlatysTask, PlatysTaskHandler>();

  private Handler mServiceHandler = null;
  private SensorDbHelper mSensorDbHelper = null;

  synchronized private static PowerManager.WakeLock getLock(Context context) {
    if (mWakelock == null) {
      PowerManager mgr = (PowerManager) context.getApplicationContext().getSystemService(
          Context.POWER_SERVICE);
      mWakelock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_NAME);
      mWakelock.setReferenceCounted(false);
    }

    return mWakelock;
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
      String task = intent.getAction();

      if (!runningTasks.containsKey(task) && !pendingTasks.containsKey(task)) {
        Log.i(TAG, "Starting PlatysSerice for action: " + task);

        if (pendingTasks.isEmpty() && !runningTasks.containsKey(PlatysTask.SENSE)
            && !runningTasks.containsKey(PlatysTask.SYNC)
            && !runningTasks.containsKey(PlatysTask.SAVE_LABELS)) {
          if (task.equals(PlatysReceiver.ACTION_SENSE)) {
            startTask(PlatysTask.SENSE, intent);
          } else if (task.equals(PlatysReceiver.ACTION_SYNC)) {
            startTask(PlatysTask.SYNC, intent);
          } else if (task.equals(PlatysReceiver.ACTION_SAVE_LABELS)) {
            startTask(PlatysTask.SAVE_LABELS, intent);
          }
        } else {
          if (task.equals(PlatysReceiver.ACTION_SENSE)) {
            pendingTasks.put(PlatysTask.SENSE, intent);
          } else if (task.equals(PlatysReceiver.ACTION_SYNC)) {
            pendingTasks.put(PlatysTask.SYNC, intent);
          } else if (task.equals(PlatysReceiver.ACTION_SAVE_LABELS)) {
            pendingTasks.put(PlatysTask.SAVE_LABELS, intent);
          }
        }

        if (task.equals(PlatysReceiver.ACTION_CHECK_FOR_SW_UPDATE)) {
          startTask(PlatysTask.CHECK_FOR_SW_UPDATES, intent);
        } else if (task.equals(PlatysReceiver.ACTION_UPDATE_SW)) {
          pendingTasks.clear();
          for (PlatysTask runningTask : runningTasks.keySet()) {
            runningTasks.get(runningTask).stopTask();
          }
          Intent syncIntent = new Intent(intent);
          syncIntent.setAction(PlatysReceiver.ACTION_SYNC);
          startTask(PlatysTask.SYNC, syncIntent);
          pendingTasks.put(PlatysTask.UPDATE_SW, intent);
        }

      }
    }

    return START_REDELIVER_INTENT;
  }

  public enum PlatysTask {
    SENSE, SYNC, SAVE_LABELS, CHECK_FOR_SW_UPDATES, UPDATE_SW
  }

  private void startTask(PlatysTask task, Intent intent) {
    if (mSensorDbHelper == null || !mSensorDbHelper.isOpen()) {
      mSensorDbHelper = OpenHelperManager.getHelper(getApplicationContext(), SensorDbHelper.class);
    }

    PlatysTaskHandler platysThread = null;
    switch (task) {
    case SENSE:
      platysThread = new SensorPoller(getApplicationContext(), mServiceHandler, mSensorDbHelper,
          SensorEnum.values());
      break;
    case SYNC:
      platysThread = new DbxCoreApiSyncer(getApplicationContext(), mServiceHandler,
          OpenHelperManager.getHelper(getApplicationContext(), SensorDbHelper.class));
      break;
    case SAVE_LABELS:
      platysThread = new PlaceLabelSaver(mServiceHandler, mSensorDbHelper, intent);
      break;
    case CHECK_FOR_SW_UPDATES:
      platysThread = new SoftwareUpdatesChecker(getApplicationContext(), mServiceHandler);
      break;
    case UPDATE_SW:
      Intent updateIntent = new Intent(Intent.ACTION_VIEW,
          Uri.parse(SoftwareUpdatesChecker.SOFTWARE_UPDATE_URL));
      updateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      getApplication().startActivity(updateIntent);
      break;
    default:
      break;
    }

    if (platysThread != null) {
      runningTasks.put(task, platysThread);
      platysThread.startTask();
    }
  }

  private final class PlatysServiceHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      PlatysTask finishedTask = PlatysTask.values()[msg.what];
      Log.i(TAG, "Received message from " + finishedTask);

      switch (finishedTask) {
      case SENSE:
        break;
      case SYNC:
        OpenHelperManager.releaseHelper();
        break;
      case SAVE_LABELS:
        break;
      case CHECK_FOR_SW_UPDATES:
        break;
      default:
        break;
      }

      runningTasks.remove(finishedTask);

      if (pendingTasks.isEmpty() && runningTasks.isEmpty()) {
        Log.i(TAG, "No more tasks to run");
        PlatysService.this.stopSelf();
      } else if (!pendingTasks.isEmpty()) {
        if (runningTasks.containsKey(PlatysTask.SENSE) || runningTasks.containsKey(PlatysTask.SYNC)
            || runningTasks.containsKey(PlatysTask.SAVE_LABELS)) {
          Log.i(TAG, "Wait for a sequential task to finish");
        } else {
          PlatysTask nextTask = pendingTasks.keySet().iterator().next();
          Intent nextIntent = pendingTasks.get(nextTask);
          pendingTasks.remove(nextTask);
          startTask(nextTask, nextIntent);
        }
      } else {
        Log.i(TAG, "Wait for a task to finish");
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.i(TAG, "Destroying the service");

    for (PlatysTaskHandler taskHandler : runningTasks.values()) {
      taskHandler.stopTask();
    }
    runningTasks.clear();
    pendingTasks.clear();

    OpenHelperManager.releaseHelper();
    mSensorDbHelper = null;

    PowerManager.WakeLock lock = getLock(getApplicationContext());

    if (lock.isHeld()) {
      Log.i(TAG, "Releasing lock");
      lock.release();
    }

    mWakelock = null;
  }

}
