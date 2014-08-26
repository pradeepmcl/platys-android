package edu.ncsu.mas.platys.android.sensor.types;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import edu.ncsu.mas.platys.android.sensor.PlatysSensor;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;
import edu.ncsu.mas.platys.common.sensor.PlatysCommonSensor;
import edu.ncsu.mas.platys.common.sensor.datatypes.AccelerometerData;
import edu.ncsu.mas.platys.common.sensor.datatypes.SensorData;

public class AccelerometerSensor implements PlatysSensor {
  private static final String TAG = "Platys" + AccelerometerSensor.class.getSimpleName();

  private static final long DEFAULT_TIMEOUT = 120000; // two minutes

  private final Context mContext;
  private final Handler mHandler;
  private final SensorDbHelper mDbHelper;
  private final int mSensorIndex;

  private final SensorManager mSensorMgr;
  private Sensor mAccelerometer;
  private AccelerometerListener mAccelerometerListener = null;

  private long mSensingStartTime;

  private Dao<SensorData, ?> mAccelerometerDao = null;

  private final Message mMsgToPoller;

  public AccelerometerSensor(Context context, Handler handler, SensorDbHelper dbHelper,
      int sensorIndex) {
    mContext = context;
    mHandler = handler;
    mDbHelper = dbHelper;
    mSensorIndex = sensorIndex;

    mMsgToPoller = mHandler.obtainMessage(PlatysSensor.MSG_FROM_SENSOR);
    mMsgToPoller.arg1 = mSensorIndex;

    mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
  }

  @Override
  public void startSensor() {
    mAccelerometer = getAccelerometerSensor();
    if (mAccelerometer == null) {
      mMsgToPoller.arg2 = SensorMsg.SENSOR_DISABLED.ordinal();
      mMsgToPoller.sendToTarget();
      return;
    }

    Log.i(TAG, "Starting Accelerometer Scan.");

    mSensingStartTime = System.currentTimeMillis();

    mAccelerometerListener = new AccelerometerListener();

    if (mSensorMgr.registerListener(mAccelerometerListener, mAccelerometer,
        SensorManager.SENSOR_DELAY_NORMAL) == false) {
      mMsgToPoller.arg2 = SensorMsg.SENSING_NOT_INITIATED.ordinal();
      mMsgToPoller.sendToTarget();
      return;
    }
  }

  @Override
  public void stopSensor() {
    if (mAccelerometerListener != null && mAccelerometer != null) {
      mSensorMgr.unregisterListener(mAccelerometerListener, mAccelerometer);
    }
  }

  @Override
  public long getTimeoutValue() {
    return DEFAULT_TIMEOUT;
  }

  private Sensor getAccelerometerSensor() {
    List<Sensor> sensors = mSensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
    if (sensors.size() > 0) {
      return sensors.get(0);
    }

    return null;
  }

  private class AccelerometerListener implements SensorEventListener {
    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
      AccelerometerData accData = new AccelerometerData();
      accData.setSensingStartTime(mSensingStartTime);
      accData.setSensingEndTime(System.currentTimeMillis());
      accData.setxAcceleration((double) event.values[0]);
      accData.setyAcceleration((double) event.values[1]);
      accData.setzAcceleration((double) event.values[2]);

      try {
        if (mAccelerometerDao == null) {
          mAccelerometerDao = mDbHelper
              .getDao(PlatysCommonSensor.ACCELEROMETER_SENSOR.getDataClass());
        }
        mAccelerometerDao.create(accData);
      } catch (SQLException e) {
        Log.e(TAG, "Database operation failed.", e);
      }
    }
  }

}
