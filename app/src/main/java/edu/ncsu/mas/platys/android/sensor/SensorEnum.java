package edu.ncsu.mas.platys.android.sensor;

import edu.ncsu.mas.platys.android.sensor.types.AccelerometerSensor;
import edu.ncsu.mas.platys.android.sensor.types.BluetoothDeviceSensor;
import edu.ncsu.mas.platys.android.sensor.types.GpsSensor;
import edu.ncsu.mas.platys.android.sensor.types.WiFiAccessPointSensor;

public enum SensorEnum {
  WiFiApSensor (WiFiAccessPointSensor.class),
  BluetoothDeviceSensor (BluetoothDeviceSensor.class),
  GpsSensor (GpsSensor.class),
  AccelerometerSensor (AccelerometerSensor.class);

  private final Class<? extends PlatysSensor> sensorClass;
  SensorEnum(Class<? extends PlatysSensor> cls) {
    sensorClass = cls;
  }

  public Class<? extends PlatysSensor> getSensorClass() {
    return sensorClass;
  }
}