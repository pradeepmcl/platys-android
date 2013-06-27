package edu.ncsu.mas.platys.android.sensor;

import java.util.Arrays;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import edu.ncsu.mas.platys.android.PlatysReceiver;
import edu.ncsu.mas.platys.android.PlatysService.PlatysTask;
import edu.ncsu.mas.platys.android.sensor.PlatysSensor.SensorMsg;
import edu.ncsu.mas.platys.android.sensor.instances.AccelerometerSensor;
import edu.ncsu.mas.platys.android.sensor.instances.BluetoothDeviceSensor;
import edu.ncsu.mas.platys.android.sensor.instances.GpsSensor;
import edu.ncsu.mas.platys.android.sensor.instances.WiFiAccessPointSensor;

public class SensorPoller extends HandlerThread {

  private static final String TAG = "Platys" + SensorPoller.class.getSimpleName();
  private static final String HANDLER_THREAD_NAME = SensorPoller.class.getName();

  private static final long SCAN_FREQUENCY = 5 * 60 * 1000; // 5 min.
  private static final long DEFAULT_TIMEOUT = 2 * 60 * 1000; // 2 min.

  private final Context mContext;
  // private final ExecutorService mThreadPool;

  private final PlatysSensor[] mSensorList;
  private final Boolean[] mSensorFinishedList;
  private Runnable mOnSensorTimeout = null;

  private final Handler mServiceHandler;
  private final SensorDbHelper mDbHelper;

  public SensorPoller(Context context, Handler handler, SensorDbHelper dbHelper,
      SensorEnum[] sensorEnums) {
    super(HANDLER_THREAD_NAME);

    Log.i(TAG, "Creating SensorPoller");

    mContext = context;
    mServiceHandler = handler;
    mDbHelper = dbHelper;

    mSensorList = new PlatysSensor[sensorEnums.length];
    mSensorFinishedList = new Boolean[sensorEnums.length];

    for (int i = 0; i < sensorEnums.length; i++) {
      SensorEnum sensorEnum = sensorEnums[i];
      PlatysSensor sensor = null;
      switch (sensorEnum) {
      case WiFiApSensor:
        sensor = new WiFiAccessPointSensor(mContext, mSensorResponseHandler, mDbHelper, i);
        break;
      case BluetoothDeviceSensor:
        sensor = new BluetoothDeviceSensor(mContext, mSensorResponseHandler, mDbHelper, i);
        break;
      case GpsSensor:
        sensor = new GpsSensor(mContext, mSensorResponseHandler, mDbHelper, i);
        break;
      case AccelerometerSensor:
        sensor = new AccelerometerSensor(mContext, mSensorResponseHandler, mDbHelper, i);
        break;
      }

      if (sensor != null) {
        mSensorList[i] = sensor;
        mSensorFinishedList[i] = false;
      }
    }

    // mThreadPool = Executors.newFixedThreadPool(mSensorList.length);
  }

  private final Handler mSensorResponseHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      if (msg.what == PlatysSensor.MSG_FROM_SENSOR) {
        Log.i(TAG, "Finished sensor index: " + msg.arg1);
        mSensorFinishedList[msg.arg1] = true;
        mSensorList[msg.arg1].stopSensor();
      }

      if (!(Arrays.asList(mSensorFinishedList).contains(false))) {
        Log.i(TAG, "All sensors finished; halting the poller.");
        mSensorResponseHandler.removeCallbacks(mOnSensorTimeout);
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
        for (int i = 0; i < mSensorList.length; i++) {
          if (mSensorFinishedList[i] == false) {
            mSensorList[i].stopSensor();
          }
        }
        quit();
      }
    };

    mSensorResponseHandler.postDelayed(mOnSensorTimeout, getTimeOutValue());

    for (final PlatysSensor sensor : mSensorList) {
      sensor.startSensor();

      // TODO: Right now, all operation execute on the main thread. Investigate
      // if multithreading can help. Some sensors (e.g., LocationListener
      // require that the thread be a Looper.

      /*mThreadPool.submit(new Runnable() {
        @Override
        public void run() {
          sensor.startSensor();
        }
      });*/
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
    scheduleNext();

    Message msgToService = mServiceHandler.obtainMessage(PlatysTask.PLATYS_TASK_SENSE.ordinal());
    msgToService.arg1 = SensorMsg.SENSING_SUCCEEDED.ordinal();
    msgToService.sendToTarget();
  }

  private void scheduleNext() {
    Intent intentToSchedule = new Intent(mContext.getApplicationContext(), PlatysReceiver.class);
    intentToSchedule.setAction(PlatysReceiver.ACTION_SENSE);
    PlatysReceiver.schedulePlatysAction(mContext, intentToSchedule, SCAN_FREQUENCY);
  }

  private long getTimeOutValue() {
    /*long longestTimeoutValue = 0;
    for (final Sensor sensor : mSensorList) {
      if (longestTimeoutValue < sensor.getTimeoutValue()) {
        longestTimeoutValue = sensor.getTimeoutValue();
      }
    }
    return longestTimeoutValue;*/

    return DEFAULT_TIMEOUT; // For testing.
  }

}
