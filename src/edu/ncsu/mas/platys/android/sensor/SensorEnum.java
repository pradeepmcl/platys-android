package edu.ncsu.mas.platys.android.sensor;

import edu.ncsu.mas.platys.android.sensor.instances.BluetoothDeviceSensor2;
import edu.ncsu.mas.platys.android.sensor.instances.WiFiAccessPointSensor2;

public enum SensorEnum {
  WiFiApSensor (WiFiAccessPointSensor2.class),
  BluetoothDeviceSensor (BluetoothDeviceSensor2.class);

  private final Class<? extends Sensor> sensorClass;
  SensorEnum(Class<? extends Sensor> cls) {
    sensorClass = cls;
  }

  public Class<? extends Sensor> getSensorClass() {
    return sensorClass;
  }
}