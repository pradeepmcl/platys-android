package edu.ncsu.mas.platys.android.sync;

import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxPath.InvalidPathException;

import edu.ncsu.mas.platys.android.sensor.SensorManager;

public class SyncManager {

  private static final String SENSOR_SYNC_DIR = "/SensorData/";

  private static final String mDbxAppKey = "x6cj580qfc2zjxu";
  private static final String mDbxAppSecret = "5bpxqwkyym3zwol";

  private final DbxAccountManager mDbxAcctMgr;

  private Context mContext = null;
  private SensorManager mSensorManager = null;

  public SyncManager(Context context, SensorManager sensorManager) {
    mContext = context;
    mSensorManager = sensorManager;
    mDbxAcctMgr = DbxAccountManager.getInstance(mContext.getApplicationContext(), mDbxAppKey,
        mDbxAppSecret);
  }

  public void close() {
    mSensorManager = null;
    mContext = null;
  }

  public synchronized void startSensorSync() {
    if (mDbxAcctMgr.hasLinkedAccount()) {
      FileOutputStream syncOutputStream = null;
      try {
        DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
        DbxFile syncFile = dbxFs.create(new DbxPath(SENSOR_SYNC_DIR + System.currentTimeMillis()
            + ".db"));
        syncOutputStream = syncFile.getWriteStream();
        mSensorManager.createSensorDbBackup(syncOutputStream);
      } catch (Unauthorized e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InvalidPathException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (DbxException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
        if (syncOutputStream != null) {
          try {
            syncOutputStream.close();
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    } else {
      Log.i("Pradeep", "No linked account in Sync");
    }
  }
}
