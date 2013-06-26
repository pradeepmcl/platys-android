package edu.ncsu.mas.platys.android.sensor;

import edu.ncsu.mas.platys.android.sensor.instances.BluetoothDeviceSensor;
import edu.ncsu.mas.platys.android.sensor.instances.GpsSensor;
import edu.ncsu.mas.platys.android.sensor.instances.WiFiAccessPointSensor;

public enum SensorEnum {
  WiFiApSensor (WiFiAccessPointSensor.class),
  BluetoothDeviceSensor (BluetoothDeviceSensor.class),
  GpsSensor (GpsSensor.class);

  private final Class<? extends Sensor> sensorClass;
  SensorEnum(Class<? extends Sensor> cls) {
    sensorClass = cls;
  }

  public Class<? extends Sensor> getSensorClass() {
    return sensorClass;
  }
}