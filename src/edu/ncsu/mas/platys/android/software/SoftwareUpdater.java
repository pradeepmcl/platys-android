package edu.ncsu.mas.platys.android.software;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import edu.ncsu.mas.platys.android.PlatysReceiver.PlatysTask;
import edu.ncsu.mas.platys.android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SoftwareUpdater implements Runnable {
  private static final String TAG = "Platys" + SoftwareUpdater.class.getSimpleName();

  public static final String SOFTWARE_UPDATE_URL = 
      "http://platys.csc.ncsu.edu/platys/resources/PlatysAndroid.apk";

  private final Context mContext;
  private final Handler mServiceHandler;

  public SoftwareUpdater(Context context, Handler serviceHandler) {
    Log.i(TAG, "Creating SoftwareUpdater.");
    mContext = context;
    mServiceHandler = serviceHandler;
  }

  @Override
  public void run() {
    int updateStatus = 0;
    try {
      PackageManager pkgMgr = mContext.getPackageManager();
      ApplicationInfo appInfo = pkgMgr.getApplicationInfo("edu.ncsu.mas.platys.android", 0);
      String appFile = appInfo.sourceDir;
      long appInstallTime = new File(appFile).lastModified(); // Epoch Time

      long serverLastUpdateTime = getLastModifiedTime();

      Log.i(TAG, appInstallTime + ", " + serverLastUpdateTime + ", " + System.currentTimeMillis());
      if (appInstallTime < serverLastUpdateTime) {
        Intent notifIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(SOFTWARE_UPDATE_URL));
        PendingIntent pNotifIntent = PendingIntent.getActivity(mContext, 0, notifIntent, 0);
        
        Notification notif = new Notification.Builder(mContext)
            .setContentTitle("Platys")
            .setContentText("A new version of the app is available.")
            .setSmallIcon(R.drawable.ic_launcher_platys)
            .setContentIntent(pNotifIntent)
            .getNotification();
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        
        NotificationManager notifMgr = (NotificationManager) mContext
            .getSystemService(Context.NOTIFICATION_SERVICE);
        notifMgr.notify(0, notif); 
        
      } else {
        Log.i(TAG, "No updates available");
      }
    } catch (NameNotFoundException e) {
      Log.e(TAG, "Can't find the app! Something wrong", e);
    } finally {
      Message msgToService = mServiceHandler.obtainMessage(PlatysTask.PLATYS_CHECK_SOFTWARE_UPDATES
          .ordinal());
      msgToService.arg1 = updateStatus;
      msgToService.sendToTarget();
    }
  }

  private long getLastModifiedTime() {
    long lastModifiedTime = -1;

    try {
      URL resourceUrl = new URL(SOFTWARE_UPDATE_URL);
      URLConnection conn = resourceUrl.openConnection();
      String lastModifiedTimeString = conn.getHeaderField("Last-Modified");
      if (lastModifiedTimeString != null) {
        lastModifiedTime = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).parse(
            lastModifiedTimeString).getTime();
      }
    } catch (IOException e) {
      lastModifiedTime = -1;
      Log.e(TAG, "Can't fetch software update.", e);
    } catch (ParseException e) {
      lastModifiedTime = -1;
      Log.e(TAG, "Can't fetch software update.", e);
    }

    return lastModifiedTime;
  }
}
