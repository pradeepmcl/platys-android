package edu.ncsu.mas.platys.android;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PlatysReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    scheduleNext(context);
    context.startService(new Intent(context, PlatysService.class));
  }

  private void scheduleNext(Context context) {
    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, PlatysReceiver.class);
    intent.setAction("platys.intent.action.SENSE_ALL");

    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MINUTE, 10);

    am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
        PendingIntent.getBroadcast(context, 0, intent, 0));
  }

}
