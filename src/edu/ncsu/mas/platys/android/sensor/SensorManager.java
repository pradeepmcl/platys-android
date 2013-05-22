package edu.ncsu.mas.platys.android.sensor;

import android.content.Context;

public class SensorManager {

  private Context mContext = null;

  private WiFiAccessPointSensor mWiFiAccessPointSensor = null;;

  public SensorManager(Context context) {
    mContext = context;
    mWiFiAccessPointSensor = new WiFiAccessPointSensor(mContext);
  }

  public void init() {
    // TODO: Add an alarmmanager here.
  }

  public void sense(String sensorType) {
    mWiFiAccessPointSensor.sense();
  }

  public void cleanUp() {
    mWiFiAccessPointSensor.cleanUp();
    mWiFiAccessPointSensor = null;
    mContext = null;
  }

}
