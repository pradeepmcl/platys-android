package edu.ncsu.mas.platys.android.sync;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

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

  private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
  
  private ScheduledFuture<?> syncMgrHandle;
  
  private Context mContext = null;
  private SensorManager mSensorManager = null;

  public SyncManager(Context context, SensorManager sensorManager) {
    mContext = context;
    mSensorManager = sensorManager;
    mDbxAcctMgr = DbxAccountManager.getInstance(mContext.getApplicationContext(), mDbxAppKey,
        mDbxAppSecret);
  }

  public void close() {
    syncMgrHandle.cancel(false);
    mSensorManager = null;
    mContext = null;
  }
  
  public void startSensorSync() {
    syncMgrHandle = mScheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        
        final File sensorDbFile = mContext.getDatabasePath(mSensorManager.getHelper()
            .getDatabaseName());

        if (mDbxAcctMgr.hasLinkedAccount() && sensorDbFile.length() != 0) {
          // Step 1. Stop sensors.
          Log.i(SyncManager.class.getName(), "Stopping sensors");
          mSensorManager.stopSensors();

          // Step 2. Copy database file.
          DbxFile syncFile = null;
          boolean success = true;
          try {
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
            syncFile = dbxFs.create(new DbxPath(SENSOR_SYNC_DIR + System.currentTimeMillis()
                + ".db"));
            syncFile.writeFromExistingFile(sensorDbFile, false);
          } catch (Unauthorized e) {
            success = false;
            e.printStackTrace();
          } catch (InvalidPathException e) {
            success = false;
            e.printStackTrace();
          } catch (DbxException e) {
            success = false;
            e.printStackTrace();
          } catch (IOException e) {
            success = false;
            e.printStackTrace();
          } finally {
            syncFile.close();
            if (success) {
              Log.i(SyncManager.class.getName(), "Successful backup. Deleting local data");
              mSensorManager.getHelper().truncateTables();
            } else {
              Log.i(SyncManager.class.getName(), "There was an exception during backup.");
            }
          }

          // Step 3. Start sensors.
          Log.i(SyncManager.class.getName(), "Starting sensors");
          mSensorManager.initSensors();

        }
      }
    }, 30, 120, MINUTES);
  }

  /*public synchronized void startSensorSync1() {
    final File sensorDbFile = mContext
        .getDatabasePath(mSensorManager.getHelper().getDatabaseName());
    
    if (mDbxAcctMgr.hasLinkedAccount() && sensorDbFile.length() != 0) {
      //final ExecutorService backupTasks = Executors.newSingleThreadExecutor();

      mScheduler.submit(new Runnable() {
        @Override
        public void run() {
          Log.i(SyncManager.class.getName(), "Stopping sensors");
          mSensorManager.stopSensors();
        }
      });

      mScheduler.submit(new Runnable() {
        @Override
        public void run() {
          DbxFile syncFile = null;
          boolean success = true;
          try {
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
            syncFile = dbxFs.create(new DbxPath(SENSOR_SYNC_DIR + System.currentTimeMillis()
                + ".db"));
            syncFile.writeFromExistingFile(sensorDbFile, false);
          } catch (Unauthorized e) {
            success = false;
            e.printStackTrace();
          } catch (InvalidPathException e) {
            success = false;
            e.printStackTrace();
          } catch (DbxException e) {
            success = false;
            e.printStackTrace();
          } catch (IOException e) {
            success = false;
            e.printStackTrace();
          } finally {
            syncFile.close();
            if (success) {
              Log.i(SyncManager.class.getName(), "Successful backup. Deleting local data");
              mSensorManager.getHelper().truncateTables();
            } else {
              Log.i(SyncManager.class.getName(), "There was an exception during backup.");
            }
          }
        }
      });

      mScheduler.submit(new Runnable() {
        @Override
        public void run() {
          Log.i(SyncManager.class.getName(), "Starting sensors");
          mSensorManager.initSensors();
        }
      });
    }
  }*/
  
}
