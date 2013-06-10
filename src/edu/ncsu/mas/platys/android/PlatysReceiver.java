package edu.ncsu.mas.platys.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PlatysReceiver extends BroadcastReceiver {

  private static final String TAG = PlatysReceiver.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    Log.i(TAG, "Alarm received for " + action);
    PlatysService.startWakefulAction(context, intent);
    scheduleNext(context, intent);
  }

  private void scheduleNext(Context context, Intent intent) {
    long delayInMillis = 0;
    if (intent.getAction().equals(PlatysService.PLATYS_ACTION_SENSE)) {
      delayInMillis = 10 * 60 * 1000; // 10 minutes.
    } else if (intent.getAction().equals(PlatysService.PLATYS_ACTION_SYNC)) {
      delayInMillis = 60 * 60 * 1000; // 60 minutes.
    } else {
      return;
    }

    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayInMillis,
        PendingIntent.getBroadcast(context, 0, intent, 0));
  }

}
