package edu.ncsu.mas.platys.android.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import edu.ncsu.mas.platys.android.sensor.instances.BluetoothDeviceSensor2;
import edu.ncsu.mas.platys.android.sensor.instances.WiFiAccessPointSensor2;
import edu.ncsu.mas.platys.android.utils.WakefulThread;

public class SensorPoller extends WakefulThread {

  private static final String TAG = "Platys" + SensorPoller.class.getSimpleName();
  private static final String HANDLER_NAME = SensorPoller.class.getName();

  private final ExecutorService mThreadPool;

  private final List<Sensor> mSensorList = new ArrayList<Sensor>();
  private final List<Boolean> mSensorFinishedList = new ArrayList<Boolean>();
  private Runnable mOnSensorTimeout = null;

  SensorDbHelper dbHelper = null;

  public SensorPoller(WakeLock lock, Context context) {
    super(lock, HANDLER_NAME);

    mSensorList.add(new WiFiAccessPointSensor2(context, sensorResponseHandler,
        getDbHelper(context), 0));
    mSensorFinishedList.add(false);
    mSensorList.add(new BluetoothDeviceSensor2(context, sensorResponseHandler,
        getDbHelper(context), 1));
    mSensorFinishedList.add(false);

    mThreadPool = Executors.newFixedThreadPool(mSensorList.size());
  }

  private final Handler sensorResponseHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      if (msg.what == 101) {
        Log.i(TAG, "Finished sensor index: " + msg.arg1);
        mSensorFinishedList.add(msg.arg1, true);
      }

      if (!mSensorFinishedList.contains(false)) {
        Log.i(TAG, "All sensors finished; halting the poller.");
        removeCallbacks(mOnSensorTimeout);
        quit();
      }
    }
  };

  @Override
  protected void onPreExecute() {
    long longestTimeOutValue = findLongestTimeoutValue();
    mOnSensorTimeout = new Runnable() {
      @Override
      public void run() {
        Log.i(TAG, "Some sensor must have timed out; halting the poller.");
        quit();
      }
    };
    sensorResponseHandler.postDelayed(mOnSensorTimeout, longestTimeOutValue);

    for (final Sensor sensor : mSensorList) {
      mThreadPool.execute(new Runnable() {
        @Override
        public void run() {
          sensor.startSensor();
        }
      });
    }
  }

  @Override
  protected void onPostExecute() {
    for (final Sensor sensor : mSensorList) {
      sensor.stopSensor();
    }

    if (dbHelper != null) {
      OpenHelperManager.releaseHelper();
      dbHelper = null;
    }

    super.onPostExecute();
  }

  private long findLongestTimeoutValue() {
    long longestTimeoutValue = 0;
    for (final Sensor sensor : mSensorList) {
      if (longestTimeoutValue < sensor.getTimeoutValue()) {
        longestTimeoutValue = sensor.getTimeoutValue();
      }
    }
    return longestTimeoutValue;
  }

  private SensorDbHelper getDbHelper(Context context) {
    if (dbHelper == null) {
      dbHelper = OpenHelperManager.getHelper(context, SensorDbHelper.class);
    }
    return dbHelper;
  }
}
