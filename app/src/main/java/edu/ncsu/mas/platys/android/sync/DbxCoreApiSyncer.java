package edu.ncsu.mas.platys.android.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import edu.ncsu.mas.platys.android.PlatysReceiver;
import edu.ncsu.mas.platys.android.PlatysService.PlatysTask;
import edu.ncsu.mas.platys.android.PlatysTaskHandler;
import edu.ncsu.mas.platys.android.network.DbxClientFactory;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;
import edu.ncsu.mas.platys.android.ui.ServerModeChooserActivity;

import static edu.ncsu.mas.platys.android.ui.ServerModeChooserActivity.PLATYS_SERVER_PREFS;
import static edu.ncsu.mas.platys.android.ui.ServerModeChooserActivity.PREFS_DBX_ACCESS_TOKEN;
import static edu.ncsu.mas.platys.android.ui.ServerModeChooserActivity.PREFS_KEY_SERVER_MODE;

public class DbxCoreApiSyncer implements Runnable, PlatysTaskHandler {
  private static final String TAG = "Platys" + DbxCoreApiSyncer.class.getSimpleName();

  private static final String SENSOR_SYNC_DIR_PATH = "/PlatysApp/SensorData/";

  private static final long SYNC_FREQUENCY = 60 * 60 * 1000; // 1 hour.

  private final Context mContext;
  private final Handler mServiceHandler;
  private final SensorDbHelper mSensorDbHelper;

  private Thread mSyncerThread = null;

  public DbxCoreApiSyncer(Context context, Handler serviceHandler, SensorDbHelper sensorDbHelper) {
    Log.i(TAG, "Creating SyncHandler.");
    mContext = context;
    mServiceHandler = serviceHandler;
    mSensorDbHelper = sensorDbHelper;
  }

  @Override
  public void startTask() {
    mSyncerThread = new Thread(this);
    mSyncerThread.start();
  }

  @Override
  public void run() {
    int syncSuccess = 0;
    final File sensorDbFile = mContext.getDatabasePath(mSensorDbHelper.getDatabaseName());
    InputStream sensorDbFileIs = null;

    try {
//      DropboxAPI<AndroidAuthSession> dbxApi = new DropboxAPI<AndroidAuthSession>(
//          ServerModeChooserActivity.getDbxSession(mContext));
//      if (dbxApi.getSession().isLinked()) {
        final String fileToUpload = SENSOR_SYNC_DIR_PATH + System.currentTimeMillis() + ".db";
        sensorDbFileIs = new FileInputStream(sensorDbFile);
//        Entry newEntry = dbxApi.putFile(fileToUpload, sensorDbFileIs, sensorDbFile.length(), null,
//            null);
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PLATYS_SERVER_PREFS,
                Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(PREFS_DBX_ACCESS_TOKEN, "");
        DbxClientFactory.init(accessToken);

        FileMetadata fileMetadata = DbxClientFactory.getClient().files().uploadBuilder(fileToUpload)
                .withMode(WriteMode.OVERWRITE)
                .uploadAndFinish(sensorDbFileIs);
        Log.i(TAG, "Synced file revision: " + fileMetadata.getRev());
        syncSuccess = 1;
//      } else {
//        Log.i(TAG, "Not syncing; dropbox account not linked.");
//      }
    } catch (DbxException | IOException e) {
      e.printStackTrace();
    } finally {
      if (sensorDbFileIs != null) {
        try {
          sensorDbFileIs.close();
        } catch (IOException e) {
          // Can't do much!
        }
      }

      if (syncSuccess == 1) {
        sensorDbFile.delete();
      }

      scheduleNext();

      Message msgToService = mServiceHandler.obtainMessage(PlatysTask.SYNC.ordinal());
      msgToService.arg1 = syncSuccess;
      msgToService.sendToTarget();
    }
  }

  private void scheduleNext() {
    Intent intentToSchedule = new Intent(mContext.getApplicationContext(), PlatysReceiver.class);
    intentToSchedule.setAction(PlatysReceiver.ACTION_SYNC);
    PlatysReceiver.schedulePlatysAction(mContext, intentToSchedule, SYNC_FREQUENCY);
  }

  @Override
  public void stopTask() {
    // TODO Auto-generated method stub

  }

}
