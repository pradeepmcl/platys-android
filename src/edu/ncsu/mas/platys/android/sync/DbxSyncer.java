package edu.ncsu.mas.platys.android.sync;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxPath.InvalidPathException;

import edu.ncsu.mas.platys.android.PlatysService;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;

public class DbxSyncer implements Runnable {

  private static final String TAG = "Platys" + DbxSyncer.class.getSimpleName();

  private static final String SENSOR_SYNC_DIR = "/SensorData/";

  private static final String mDbxAppKey = "x6cj580qfc2zjxu";
  private static final String mDbxAppSecret = "5bpxqwkyym3zwol";

  private final DbxAccountManager mDbxAcctMgr;

  private final Context mContext;
  private final Handler mServiceHandler;
  private final SensorDbHelper mSensorDbHelper;

  public DbxSyncer(Context context, Handler serviceHandler, SensorDbHelper sensorDbHelper) {
    Log.i(TAG, "Creating SyncHandler.");
    mContext = context;
    mServiceHandler = serviceHandler;
    mSensorDbHelper = sensorDbHelper;

    mDbxAcctMgr = DbxAccountManager.getInstance(mContext.getApplicationContext(), mDbxAppKey,
        mDbxAppSecret);
  }

  @Override
  public void run() {
    Log.i(TAG, "Running SyncHandler.");
    final File sensorDbFile = mContext.getDatabasePath(mSensorDbHelper.getDatabaseName());

    DbxFile syncFile = null;
    int backupSuccess = 0;

    try {
      if (mDbxAcctMgr.hasLinkedAccount() && sensorDbFile.length() != 0) {
        DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
        syncFile = dbxFs.create(new DbxPath(SENSOR_SYNC_DIR + System.currentTimeMillis() + ".db"));
        syncFile.writeFromExistingFile(sensorDbFile, true); // Steal file
        backupSuccess = 1;
      }
    } catch (Unauthorized e) {
      Log.e(TAG, "Unauthorized", e);
    } catch (InvalidPathException e) {
      Log.e(TAG, "Invalid path", e);
    } catch (DbxException e) {
      Log.e(TAG, "Dropbox exception", e);
    } catch (IOException e) {
      Log.e(TAG, "I/O Exception", e);
    } finally {
      if (syncFile != null) {
        syncFile.close();
      }

      Log.i(TAG, "Finishing backup. Success? " + backupSuccess);

      Message msgToService = mServiceHandler.obtainMessage(PlatysService.PLATYS_MSG_SYNC_FINISHED);
      msgToService.arg1 = backupSuccess;
      msgToService.sendToTarget();
    }
  }

}
