package edu.ncsu.mas.platys.android.sensor;

public interface ISensor {
  public void init();
  public void sense();
  public void close();
}
