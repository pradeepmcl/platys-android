package edu.ncsu.mas.platys.android.sensor;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import edu.ncsu.mas.platys.android.sensor.instances.BluetoothDeviceSensor;
import edu.ncsu.mas.platys.android.sensor.instances.WiFiAccessPointSensor;

public class SensorManager {

  private Context mContext = null;

  SensorDbHelper dbHelper = null;

  private WiFiAccessPointSensor mWiFiAccessPointSensor = null;
  private BluetoothDeviceSensor mBluetoothSensor = null;

  public SensorManager(Context context) {
    mContext = context;
    initSensors();
  }

  public void close() {
    stopSensors();

    if (dbHelper != null) {
      OpenHelperManager.releaseHelper();
      dbHelper = null;
    }

    mContext = null;
  }

  public void initSensors() {
    mWiFiAccessPointSensor = new WiFiAccessPointSensor(mContext, getHelper());
    mWiFiAccessPointSensor.startSensing();

    mBluetoothSensor = new BluetoothDeviceSensor(mContext, getHelper());
    mBluetoothSensor.startSensing();
  }

  public void stopSensors() {
    if (mWiFiAccessPointSensor != null) {
      mWiFiAccessPointSensor.stopSensing();
      mWiFiAccessPointSensor = null;
    }

    if (mBluetoothSensor != null) {
      mBluetoothSensor.stopSensing();
      mBluetoothSensor = null;
    }
  }

  public SensorDbHelper getHelper() {
    if (dbHelper == null) {
      dbHelper = OpenHelperManager.getHelper(mContext, SensorDbHelper.class);
    }
    return dbHelper;
  }
}
