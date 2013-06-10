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
    // scheduleNext(context, 10 * 60 * 1000); // 10 minutes.
  }

  private void scheduleNext(Context context, long delayInMillis) {
    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, PlatysReceiver.class);
    intent.setAction(PlatysService.PLATYS_ACTION_SENSE);

    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayInMillis,
        PendingIntent.getBroadcast(context, 0, intent, 0));
  }

}
