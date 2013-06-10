package edu.ncsu.mas.platys.android.sensor;

public interface Sensor {
  public static final int MSG_FROM_SENSOR = 101;
  public static final int SENSING_SUCCEEDED = 0;
  public static final int SENSING_FAILED = 1;

  public boolean startSensor();
  public boolean stopSensor();
  public long getTimeoutValue();
}
