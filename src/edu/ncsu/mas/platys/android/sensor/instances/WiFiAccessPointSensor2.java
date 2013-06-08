package edu.ncsu.mas.platys.android.sensor.instances;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import edu.ncsu.mas.platys.android.sensor.Sensor;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;
import edu.ncsu.mas.platys.common.constasnts.PlatysSensorEnum;
import edu.ncsu.mas.platys.common.sensordata.SensorData;
import edu.ncsu.mas.platys.common.sensordata.WifiAccessPointData;

public class WiFiAccessPointSensor2 implements Sensor {

  private static final String TAG = WiFiAccessPointSensor2.class.getSimpleName();

  private static final long DEFAULT_TIMEOUT = 120000; // two minutes

  private final Context mContext;
  private final Handler mHandler;
  private final WifiManager mWifiMgr;
  private final SensorDbHelper mDbHelper;
  private final int mSensorIndex;

  public WiFiAccessPointSensor2(Context context, Handler handler, SensorDbHelper dbHelper,
      int sensorIndex) {
    mContext = context;
    mHandler = handler;
    mWifiMgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    mDbHelper = dbHelper;
    mSensorIndex = sensorIndex;
  }

  @Override
  public boolean startSensor() {
    if (!mWifiMgr.isWifiEnabled()) {
      return false;
    }
    mContext.registerReceiver(wifiAccessPointReceiver, new IntentFilter(
        WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

    return mWifiMgr.startScan();
  }

  @Override
  public boolean stopSensor() {
    if (wifiAccessPointReceiver != null) {
      mContext.unregisterReceiver(wifiAccessPointReceiver);
    }
    return true;
  }

  @Override
  public long getTimeoutValue() {
    return DEFAULT_TIMEOUT;
  }

  private final BroadcastReceiver wifiAccessPointReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(final Context context, Intent intent) {
      int result = 1;
      final long curTime = System.currentTimeMillis();
      final List<ScanResult> apList = mWifiMgr.getScanResults();
      final WifiInfo connectedAp = mWifiMgr.getConnectionInfo();
      try {
        final Dao<SensorData, ?> sensorDao = mDbHelper
            .getDao(PlatysSensorEnum.WIFI_ACCESS_POINT_SENSOR.getDataClass());

        // TODO Find out if bulk insert is worth for few inserts.
        sensorDao.callBatchTasks(new Callable<Void>() {
          @Override
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
        result = 0;
        Log.e(TAG, "Database insert failed", e);
      } catch (Exception e) {
        result = 0;
        Log.e(TAG, "Unknown error", e);
      }

      Message msg = mHandler.obtainMessage(101, mSensorIndex, result);
      msg.sendToTarget();
    }
  };

}
