package edu.ncsu.mas.platys.android.sensor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import edu.ncsu.mas.platys.android.sensor.instances.WiFiAccessPointSensor2;
import edu.ncsu.mas.platys.android.utils.WakefulThread;

public class SensorPoller extends WakefulThread {

  private static final int POOL_SIZE = 1;
  
  private static final int DEFAULT_TIMEOUT = 120000; // two minutes
  private static int TIMEOUT = DEFAULT_TIMEOUT;

  private final ExecutorService pool;
  
  private Runnable onTimeout = null;
  
  private Handler sensorHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      Log.i("Pradeep", "Received some msg");
      if (msg.what == 101) {
        Log.i("Pradeep", "Received msg to quit.");
        removeCallbacks(onTimeout);
        quit();
      }
    }
  };
  
  private final WiFiAccessPointSensor2 wifiApSensor;

  public SensorPoller(WakeLock lock, Context context) {
    super(lock, "Pradeep");
    pool = Executors.newFixedThreadPool(POOL_SIZE);
    wifiApSensor = new WiFiAccessPointSensor2(context, sensorHandler);
  }

  @Override
  protected void onPreExecute() {
    onTimeout = new Runnable() {
      public void run() {
        quit();
      }
    };
    sensorHandler.postDelayed(onTimeout, TIMEOUT);

    pool.execute(new Runnable() {
      @Override
      public void run() {
        wifiApSensor.startSensor();
      }
    });
  }

  @Override
  protected void onPostExecute() {
    wifiApSensor.stopSensor();
    super.onPostExecute();
  }
}
