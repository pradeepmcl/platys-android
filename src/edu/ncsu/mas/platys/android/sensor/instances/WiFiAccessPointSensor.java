package edu.ncsu.mas.platys.android.sensor.instances;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.j256.ormlite.dao.Dao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import edu.ncsu.mas.platys.android.sensor.ISensor;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;
import edu.ncsu.mas.platys.common.constasnts.PlatysSensorEnum;
import edu.ncsu.mas.platys.common.sensordata.SensorData;
import edu.ncsu.mas.platys.common.sensordata.WifiAccessPointData;

public class WiFiAccessPointSensor implements ISensor {

  private Context mContext = null;
  private WifiManager mWifiMgr = null;
  private SensorDbHelper mDbHelper = null;

  private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);

  // TODO Understand the generic argument used here.
  private ScheduledFuture<?> wifiSensorHandle;

  public WiFiAccessPointSensor(Context context, SensorDbHelper dbHelper) {
    mContext = context;
    mDbHelper = dbHelper;
    mWifiMgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    mContext.registerReceiver(wifiAccessPointReceiver, new IntentFilter(
        WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
  }

  private final BroadcastReceiver wifiAccessPointReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(final Context context, Intent intent) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
          final long curTime = System.currentTimeMillis();
          final List<ScanResult> apList = mWifiMgr.getScanResults();
          final WifiInfo connectedAp = mWifiMgr.getConnectionInfo();
          try {
            final Dao<SensorData, ?> sensorDao = mDbHelper
                .getDao(PlatysSensorEnum.WIFI_ACCESS_POINT_SENSOR.getDataClass());

            // TODO Find out if bulk insert is worth for few inserts.
            sensorDao.callBatchTasks(new Callable<Void>() {
              public Void call() throws SQLException {
                WifiAccessPointData wifiApData = new WifiAccessPointData();
                wifiApData.setSensingTime(curTime);
                wifiApData.setIsConnected(false);
                for (ScanResult ap : apList) {
                  wifiApData.setBssid(ap.BSSID);
                  wifiApData.setSsid(ap.SSID);
                  wifiApData.setRssi(ap.level);
                  sensorDao.create(wifiApData);
                }

                wifiApData.setIsConnected(true);
                wifiApData.setBssid(connectedAp.getBSSID());
                wifiApData.setSsid(connectedAp.getSSID());
                wifiApData.setRssi(connectedAp.getRssi());  
                sensorDao.create(wifiApData);

                return null;
              }
            });
          } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
          } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
          }
        }
      }).start();
    }
  };

  @Override
  public void startSensing() {
    wifiSensorHandle = mScheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mWifiMgr.startScan();
      }
    }, 10, 2 * 60, SECONDS);
  }

  @Override
  public void stopSensing() {
    wifiSensorHandle.cancel(false);
    mContext.unregisterReceiver(wifiAccessPointReceiver);
    mWifiMgr = null;
    mDbHelper = null;
    mContext = null;
  }
}
