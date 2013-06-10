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
import com.j256.ormlite.android.apptools.OpenHelperManager;

import edu.ncsu.mas.platys.android.sensor.Sensor;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;

public class SyncHandler implements Runnable {

  private static final String TAG = "Platys" + SyncHandler.class.getSimpleName();

  private static final String SENSOR_SYNC_DIR = "/SensorData/";

  private static final String mDbxAppKey = "x6cj580qfc2zjxu";
  private static final String mDbxAppSecret = "5bpxqwkyym3zwol";

  private final DbxAccountManager mDbxAcctMgr;

  private final Context mContext;
  private SensorDbHelper sensorDbHelper = null;
  private final Handler mServiceHandler;

  public SyncHandler(Context context, Handler serviceHandler) {
    Log.i(TAG, "Creating SyncHandler.");
    mContext = context;
    mServiceHandler = serviceHandler;

    mDbxAcctMgr = DbxAccountManager.getInstance(mContext.getApplicationContext(), mDbxAppKey,
        mDbxAppSecret);
  }

  @Override
  public void run() {
    Log.i(TAG, "Running SyncHandler.");
    final File sensorDbFile = mContext.getDatabasePath(getSensorDbHelper().getDatabaseName());

    DbxFile syncFile = null;
    boolean backupSuccess = false;
    boolean deleteSuccess = false;
    try {
      if (mDbxAcctMgr.hasLinkedAccount() && sensorDbFile.length() != 0) {
        DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
        syncFile = dbxFs.create(new DbxPath(SENSOR_SYNC_DIR + System.currentTimeMillis() + ".db"));
        syncFile.writeFromExistingFile(sensorDbFile, false);
        backupSuccess = true;
        deleteSuccess = sensorDbFile.delete();
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
      Log.i(TAG, "Finishing backup. " + backupSuccess + ", " + deleteSuccess);
      if (syncFile != null) {
        syncFile.close();
      }

      if (sensorDbHelper != null) {
        OpenHelperManager.releaseHelper();
        sensorDbHelper = null;
      }

      Message msgToService = mServiceHandler.obtainMessage();
      msgToService.what = Sensor.MSG_FROM_SENSOR;
      msgToService.sendToTarget();
    }
  }

  private SensorDbHelper getSensorDbHelper() {
    if (sensorDbHelper == null) {
      sensorDbHelper = OpenHelperManager.getHelper(mContext, SensorDbHelper.class);
    }
    return sensorDbHelper;
  }

}
