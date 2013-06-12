package edu.ncsu.mas.platys.android.sensor;

public interface Sensor {
  public static final int MSG_FROM_SENSOR = 101;
  
  public static final int SENSOR_DISABLED = 0;
  public static final int SENSING_NOT_INITIATED = 1;
  public static final int SENSING_SUCCEEDED = 2;
  public static final int SENSING_FAILED = 3;

  public void startSensor();
  public void stopSensor();
  public long getTimeoutValue();
}
