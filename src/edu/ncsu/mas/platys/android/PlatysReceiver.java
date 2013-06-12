package edu.ncsu.mas.platys.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PlatysReceiver extends BroadcastReceiver {

  private static final String TAG = PlatysReceiver.class.getSimpleName();

  public static final String ACTION_PERIODIC = "platys.intent.action.PERIODIC";
  public static final String ACTION_ONE_TIME = "platys.intent.action.ONE_TIME";

  public static final String EXTRA_TASK = "platys.intent.extra.TASK";

  public static enum PlatysTask {
    PLATYS_TASK_SENSE, PLATYS_TASK_SYNC;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    Log.i(TAG, "Alarm received for " + action);

    if (action.equals(ACTION_PERIODIC)) {
      PlatysService.startWakefulAction(context, intent);
      schedulePlatysAction(context, intent);
    } else if (action.equals(ACTION_ONE_TIME)) {
      PlatysService.startWakefulAction(context, intent);
    }
  }

  private void schedulePlatysAction(Context context, Intent intent) {
    String actionName = intent.getStringExtra(EXTRA_TASK);

    Intent intentToSchedule = new Intent(context, PlatysReceiver.class);
    intentToSchedule.setAction(intent.getAction());
    intentToSchedule.putExtra(EXTRA_TASK, actionName);

    long delayInMillis = 0;
    switch (PlatysTask.valueOf(actionName)) {
    case PLATYS_TASK_SENSE:
      delayInMillis = 10 * 60 * 1000; // 10 minutes.
      break;
    case PLATYS_TASK_SYNC:
      delayInMillis = 60 * 60 * 1000; // 60 minutes.
      break;
    default:
      return;
    }

    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayInMillis,
        PendingIntent.getBroadcast(context, 0, intentToSchedule, 0));
  }
}
