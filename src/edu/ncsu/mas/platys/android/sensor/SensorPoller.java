package edu.ncsu.mas.platys.android.sensor;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import edu.ncsu.mas.platys.android.PlatysService;
import edu.ncsu.mas.platys.android.sensor.instances.BluetoothDeviceSensor;
import edu.ncsu.mas.platys.android.sensor.instances.WiFiAccessPointSensor;

public class SensorPoller extends HandlerThread {

  private static final String TAG = "Platys" + SensorPoller.class.getSimpleName();
  private static final String HANDLER_THREAD_NAME = SensorPoller.class.getName();

  private final Context mContext;
  private final ExecutorService mThreadPool;

  private final Sensor[] mSensorList;
  private final Boolean[] mSensorFinishedList;

  private final Handler mServiceHandler;

  private Runnable mOnSensorTimeout = null;
  private SensorDbHelper dbHelper = null;

  public SensorPoller(Context context, Handler handler, SensorEnum[] sensorEnums) {
    super(HANDLER_THREAD_NAME);

    Log.i(TAG, "Creating SensorPoller");

    mContext = context;

    mServiceHandler = handler;

    mSensorList = new Sensor[sensorEnums.length];
    mSensorFinishedList = new Boolean[sensorEnums.length];

    for (int i = 0; i < sensorEnums.length; i++) {
      SensorEnum sensorEnum = sensorEnums[i];
      Sensor sensor = null;
      switch (sensorEnum) {
      case WiFiApSensor:
        sensor = new WiFiAccessPointSensor(mContext, mSensorResponseHandler,
            getDbHelper(mContext), i);
        break;
      case BluetoothDeviceSensor:
        sensor = new BluetoothDeviceSensor(mContext, mSensorResponseHandler,
            getDbHelper(mContext), i);
        break;
      }

      if (sensor != null) {
        mSensorList[i] = sensor;
        mSensorFinishedList[i] = false;
      }
    }

    mThreadPool = Executors.newFixedThreadPool(mSensorList.length);
  }

  private final Handler mSensorResponseHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      if (msg.what == Sensor.MSG_FROM_SENSOR) {
        Log.i(TAG, "Finished sensor index: " + msg.arg1);
        mSensorFinishedList[msg.arg1] = true;
      }

      if (!(Arrays.asList(mSensorFinishedList).contains(false))) {
        Log.i(TAG, "All sensors finished; halting the poller.");
        quit();
      }
    }
  };

  @Override
  protected void onLooperPrepared() {
    Log.i(TAG, "Preparing SensorPoller Looper");

    mOnSensorTimeout = new Runnable() {
      @Override
      public void run() {
        Log.i(TAG, "Some sensor must have timed out; halting the poller.");
        quit();
      }
    };
    
    mSensorResponseHandler.postDelayed(mOnSensorTimeout, getTimeOutValue());

    for (final Sensor sensor : mSensorList) {
      mThreadPool.submit(new Runnable() {
        @Override
        public void run() {
          sensor.startSensor();
        }
      });
    }
  }

  @Override
  public void run() {
    try {
      super.run();
    } finally {
      onPostExecute();
    }
  }

  protected void onPostExecute() {
    for (final Sensor sensor : mSensorList) {
      sensor.stopSensor();
    }

    if (dbHelper != null) {
      OpenHelperManager.releaseHelper();
      dbHelper = null;
    }
    
    Message msgToService = mServiceHandler.obtainMessage();
    msgToService.what = PlatysService.PLATYS_MSG_SENSE_FINISHED;
    msgToService.arg1 = Sensor.SENSING_SUCCEEDED;
    msgToService.sendToTarget();
    
  }

  private long getTimeOutValue() {
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
