package edu.ncsu.mas.platys.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PlatysReceiver extends BroadcastReceiver {

  // private static final String TAG = PlatysReceiver.class.getSimpleName();

  public static final String ACTION_SENSE = "platys.intent.action.SENSE";
  public static final String ACTION_SYNC = "platys.intent.action.SYNC";
  public static final String ACTION_SAVE_LABELS = "platys.intent.action.SAVE_LABELS";
  public static final String ACTION_CHECK_SW_UPDATE = "platys.intent.action.CHECK_SW_UPDATE";
  public static final String ACTION_UPDATE_SW = "platys.intent.action.UPDATE_SW";
  public static final String ACTION_SYNC_AND_UPDATE_SW = "platys.intent.action.SYNC_AND_UPDATE_SW";

  // Sent with the PLATYS_TASK_SAVE_LABELS extra.
  public static final String EXTRA_LABELING_START_TIME = "platys.intent.extra.LABELING_START_TIME";
  public static final String EXTRA_LABELING_END_TIME = "platys.intent.extra.LABELING_END_TIME";
  public static final String EXTRA_LABELS_LIST = "platys.intent.extra.LABELS_LIST";
  public static final String EXTRA_LABEL_TYPES_LIST = "platys.intent.extra.LABEL_TYPES_LIST";

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
      PlatysReceiver.startBackgroundTasks(context);
    } else if (action.equals(ACTION_SENSE) || action.equals(ACTION_SYNC)
        || action.equals(ACTION_SAVE_LABELS) || action.equals(ACTION_CHECK_SW_UPDATE)
        || action.equals(ACTION_UPDATE_SW) || action.equals(ACTION_SYNC_AND_UPDATE_SW)) {
      PlatysService.startWakefulAction(context, intent);
    }
  }

  public static void schedulePlatysAction(Context context, Intent intentToSchedule,
      long delayInMillis) {
    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayInMillis, PendingIntent
        .getBroadcast(context.getApplicationContext(), 0, intentToSchedule,
            PendingIntent.FLAG_UPDATE_CURRENT));
  }

  public static void startBackgroundTasks(Context context) {
    // Start sensing
    Intent platysSenseIntent = new Intent(context.getApplicationContext(), PlatysReceiver.class);
    platysSenseIntent.setAction(PlatysReceiver.ACTION_SENSE);
    context.sendBroadcast(platysSenseIntent);

    // Start syncing
    Intent platysSyncingIntent = new Intent(context.getApplicationContext(), PlatysReceiver.class);
    platysSyncingIntent.setAction(PlatysReceiver.ACTION_SYNC);
    context.sendBroadcast(platysSyncingIntent);

    // Start update checking
    Intent platysCheckUpdatesIntent = new Intent(context.getApplicationContext(),
        PlatysReceiver.class);
    platysCheckUpdatesIntent.setAction(PlatysReceiver.ACTION_CHECK_SW_UPDATE);
    context.sendBroadcast(platysCheckUpdatesIntent);
  }

}
