package edu.ncsu.mas.platys.android.sensor;

public interface Sensor {
  public boolean startSensor();
  public boolean stopSensor();
  public long getTimeoutValue();
}
