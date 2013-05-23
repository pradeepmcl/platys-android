package edu.ncsu.mas.platys.android.sensor;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import edu.ncsu.mas.platys.android.sensor.instances.BluetoothDeviceSensor;
import edu.ncsu.mas.platys.android.sensor.instances.WiFiAccessPointSensor;
import android.content.Context;

public class SensorManager {

  private Context mContext = null;

  SensorDbHelper dbHelper = null;

  private WiFiAccessPointSensor mWiFiAccessPointSensor = null;
  private BluetoothDeviceSensor mBluetoothSensor = null;

  public SensorManager(Context context) {
    mContext = context;
    mWiFiAccessPointSensor = new WiFiAccessPointSensor(mContext, getHelper());
    mBluetoothSensor = new BluetoothDeviceSensor(mContext, getHelper());
  }

  private SensorDbHelper getHelper() {
    if (dbHelper == null) {
      dbHelper = OpenHelperManager.getHelper(mContext, SensorDbHelper.class);
    }
    return dbHelper;
  }

  public void close() {
    if (mWiFiAccessPointSensor != null) {
      mWiFiAccessPointSensor.stopSensing();
      mWiFiAccessPointSensor = null;
    }

    if (mBluetoothSensor != null) {
      mBluetoothSensor.stopSensing();
      mBluetoothSensor = null;
    }

    if (dbHelper != null) {
      OpenHelperManager.releaseHelper();
      dbHelper = null;
    }

    mContext = null;
  }

}
