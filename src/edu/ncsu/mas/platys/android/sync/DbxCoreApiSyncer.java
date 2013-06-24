package edu.ncsu.mas.platys.android.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import edu.ncsu.mas.platys.android.PlatysService;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;
import edu.ncsu.mas.platys.android.ui.ServerModeChooserActivity;

public class DbxCoreApiSyncer implements Runnable {
  private static final String TAG = "Platys" + DbxCoreApiSyncer.class.getSimpleName();

  private static final String SENSOR_SYNC_DIR_PATH = "/PlatysApp/SensorData/";

  private final Context mContext;
  private final Handler mServiceHandler;
  private final SensorDbHelper mSensorDbHelper;

  public DbxCoreApiSyncer(Context context, Handler serviceHandler,
      SensorDbHelper sensorDbHelper) {
    Log.i(TAG, "Creating SyncHandler.");
    mContext = context;
    mServiceHandler = serviceHandler;
    mSensorDbHelper = sensorDbHelper;
  }

  @Override
  public void run() {
    int syncSuccess = 0;
    InputStream sensorDbFileIs = null;

    try {
      DropboxAPI<AndroidAuthSession> dbxApi = new DropboxAPI<AndroidAuthSession>(
          ServerModeChooserActivity.getDbxSession(mContext));
      if (dbxApi.getSession().isLinked()) {
        final File sensorDbFile = mContext.getDatabasePath(mSensorDbHelper.getDatabaseName());
        final String fileToUpload = SENSOR_SYNC_DIR_PATH + System.currentTimeMillis() + ".db";
        sensorDbFileIs = new FileInputStream(sensorDbFile);
        Entry newEntry = dbxApi.putFile(fileToUpload, sensorDbFileIs, sensorDbFile.length(), null,
            null);
        Log.i(TAG, "Synced file revision: " + newEntry.rev);
        syncSuccess = 1;
      } else {
        Log.i(TAG, "Not syncing; dropbox account not linked.");
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (DropboxException e) {
      e.printStackTrace();
    } finally {
      if (sensorDbFileIs != null) {
        try {
          sensorDbFileIs.close();
        } catch (IOException e) {
          // Can't do much!
        }
      }

      Message msgToService = mServiceHandler.obtainMessage(PlatysService.PLATYS_MSG_SYNC_FINISHED);
      msgToService.arg1 = syncSuccess;
      msgToService.sendToTarget();
    }
  }

}
