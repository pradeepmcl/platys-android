package edu.ncsu.mas.platys.android.sensor;

import static java.util.concurrent.TimeUnit.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import edu.ncsu.mas.platys.android.sensor.instances.WiFiAccessPointSensor;
import android.content.Context;

public class SensorManager {

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  // TODO Understand the generic argument used here.
  private ScheduledFuture<?> wifiSensorHandle;

  private Context mContext = null;

  private WiFiAccessPointSensor mWiFiAccessPointSensor = null;

  public SensorManager(Context context) {
    mContext = context;
    mWiFiAccessPointSensor = new WiFiAccessPointSensor(mContext);
  }

  public void init() {
    wifiSensorHandle = scheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mWiFiAccessPointSensor.sense();
      }
    }, 10, 2 * 60, SECONDS);
  }

  public void cleanUp() {
    wifiSensorHandle.cancel(true);
    mWiFiAccessPointSensor.cleanUp();
    mWiFiAccessPointSensor = null;
    mContext = null;
  }

}
