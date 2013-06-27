package edu.ncsu.mas.platys.android.sensor;

public interface PlatysSensor {
  public static final int MSG_FROM_SENSOR = 101;

  public enum SensorMsg {
    SENSOR_DISABLED,
    SENSOR_NOT_AVAILABLE,
    SENSING_NOT_INITIATED,
    SENSING_SUCCEEDED,
    SENSING_FAILED;
  }

  public void startSensor();
  public void stopSensor();
  public long getTimeoutValue();
}
