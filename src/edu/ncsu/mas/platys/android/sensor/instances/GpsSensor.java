package edu.ncsu.mas.platys.android.sensor.instances;

import java.sql.SQLException;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import edu.ncsu.mas.platys.android.sensor.PlatysSensor;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;
import edu.ncsu.mas.platys.common.constasnts.PlatysSensorEnum;
import edu.ncsu.mas.platys.common.sensordata.GpsData;
import edu.ncsu.mas.platys.common.sensordata.SensorData;

public class GpsSensor implements PlatysSensor {
  private static final String TAG = "Platys" + GpsSensor.class.getSimpleName();

  private static final long DEFAULT_TIMEOUT = 120000; // two minutes

  private static final float MIN_DISTANCE = 1; // 1 meter.

  private static final long MIN_TIME = 5000; // 5 sec.

  private final Context mContext;
  private final Handler mHandler;
  private final SensorDbHelper mDbHelper;
  private final int mSensorIndex;

  private final LocationManager mLocMgr;
  private LocationListener mLocListener = null;

  private long mSensingStartTime;

  private Dao<SensorData, ?> mGpsDao = null;

  private final Message mMsgToPoller;

  public GpsSensor(Context context, Handler handler, SensorDbHelper dbHelper, int sensorIndex) {
    mContext = context;
    mHandler = handler;
    mDbHelper = dbHelper;
    mSensorIndex = sensorIndex;

    mMsgToPoller = mHandler.obtainMessage(PlatysSensor.MSG_FROM_SENSOR);
    mMsgToPoller.arg1 = mSensorIndex;

    mLocMgr = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
  }

  @Override
  public void startSensor() {
    if (!mLocMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
      mMsgToPoller.arg2 = SensorMsg.SENSOR_DISABLED.ordinal();
      mMsgToPoller.sendToTarget();
      return;
    }

    Log.i(TAG, "Starting GPS scan.");

    mSensingStartTime = System.currentTimeMillis();

    storeLastKnownLocation();

    mLocListener = new GpsListener();
    mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE,
        mLocListener);
  }

  @Override
  public void stopSensor() {
    if (mLocListener != null) {
      mLocMgr.removeUpdates(mLocListener);
    }
  }

  @Override
  public long getTimeoutValue() {
    return DEFAULT_TIMEOUT;
  }

  private void storeLastKnownLocation() {
    Location lastLoc = mLocMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);

    if (lastLoc != null) {
      GpsData gpsData = new GpsData();
      gpsData.setSensingStartTime(mSensingStartTime);
      gpsData.setSensingEndTime(0L); // A way to identify stale locations.
      gpsData.setLattitude(lastLoc.getLatitude());
      gpsData.setLongitude(lastLoc.getLongitude());
      gpsData.setAltitude(lastLoc.getAltitude());

      try {
        if (mGpsDao == null) {
          mGpsDao = mDbHelper.getDao(PlatysSensorEnum.GPS_SENSOR.getDataClass());
        }
        mGpsDao.create(gpsData);
      } catch (SQLException e) {
        Log.e(TAG, "Database operation failed.", e);
      }
    }
  }

  private class GpsListener implements LocationListener {
    @Override
    public void onLocationChanged(Location location) {
      Log.i(TAG, "Received Gps Location");
      // int result = Sensor.SENSING_SUCCEEDED;

      GpsData gpsData = new GpsData();
      gpsData.setSensingStartTime(mSensingStartTime);
      gpsData.setSensingEndTime(System.currentTimeMillis());
      gpsData.setLattitude(location.getLatitude());
      gpsData.setLongitude(location.getLongitude());
      gpsData.setAltitude(location.getAltitude());

      try {
        if (mGpsDao == null) {
          mGpsDao = mDbHelper.getDao(PlatysSensorEnum.GPS_SENSOR.getDataClass());
        }
        mGpsDao.create(gpsData);
      } catch (SQLException e) {
        // result = SENSING_FAILED;
        Log.e(TAG, "Database operation failed.", e);
      }

      /*mMsgToPoller.arg2 = result;
      mMsgToPoller.sendToTarget();*/
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
  }

}
