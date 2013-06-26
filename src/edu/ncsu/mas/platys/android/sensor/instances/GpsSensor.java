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
import edu.ncsu.mas.platys.android.sensor.Sensor;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;
import edu.ncsu.mas.platys.common.constasnts.PlatysSensorEnum;
import edu.ncsu.mas.platys.common.sensordata.GpsData;

public class GpsSensor implements Sensor {
  private static final String TAG = "Platys" + GpsSensor.class.getSimpleName();

  private static final long DEFAULT_TIMEOUT = 120000; // two minutes

  private final Context mContext;
  private final Handler mHandler;
  private final LocationManager mLocMgr;
  private final SensorDbHelper mDbHelper;
  private final int mSensorIndex;

  private LocationListener mLocListener = null;

  private long mSensingStartTime;

  private final Message mMsgToPoller;

  public GpsSensor(Context context, Handler handler, SensorDbHelper dbHelper, int sensorIndex) {
    mContext = context;
    mHandler = handler;
    mLocMgr = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    mDbHelper = dbHelper;
    mSensorIndex = sensorIndex;
    mMsgToPoller = mHandler.obtainMessage(Sensor.MSG_FROM_SENSOR);
    mMsgToPoller.arg1 = mSensorIndex;
  }

  @Override
  public void startSensor() {
    if (!mLocMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
      mMsgToPoller.arg2 = SENSOR_DISABLED;
      mMsgToPoller.sendToTarget();
      return;
    }

    mLocListener = new GpsListener();

    Log.i(TAG, "Starting GPS scan.");

    mSensingStartTime = System.currentTimeMillis();
    try {
    mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, mLocListener);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Location lastLoc = mLocMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);

    GpsData gpsData = new GpsData();
    gpsData.setSensingStartTime(mSensingStartTime);
    gpsData.setSensingEndTime(0L);
    gpsData.setLattitude(lastLoc.getLatitude());
    gpsData.setLongitude(lastLoc.getLongitude());
    gpsData.setAltitude(lastLoc.getAltitude());

    try {
      mDbHelper.getDao(PlatysSensorEnum.GPS_SENSOR.getDataClass()).create(gpsData);
    } catch (SQLException e) {
      Log.e(TAG, "Database operation failed.", e);
    }
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

  private class GpsListener implements LocationListener {
    @Override
    public void onLocationChanged(Location location) {
      Log.i(TAG, "Received Gps Location");
      int result = Sensor.SENSING_SUCCEEDED;

      GpsData gpsData = new GpsData();
      gpsData.setSensingStartTime(mSensingStartTime);
      gpsData.setSensingEndTime(System.currentTimeMillis());
      gpsData.setLattitude(location.getLatitude());
      gpsData.setLongitude(location.getLongitude());
      gpsData.setAltitude(location.getAltitude());

      try {
        mDbHelper.getDao(PlatysSensorEnum.GPS_SENSOR.getDataClass()).create(gpsData);
      } catch (SQLException e) {
        result = SENSING_FAILED;
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
