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

public class WiFiAccessPointSensor implements Sensor {

  private static final String TAG = "Platys" + WiFiAccessPointSensor.class.getSimpleName();

  private static final long DEFAULT_TIMEOUT = 120000; // two minutes

  private final Context mContext;
  private final Handler mHandler;
  private final WifiManager mWifiMgr;
  private final SensorDbHelper mDbHelper;
  private final int mSensorIndex;

  private WifiAccessPointReceiver mWifiAccessPointReceiver = null;

  private long mSensingStartTime;

  private final Message mMsgToPoller;

  public WiFiAccessPointSensor(Context context, Handler handler, SensorDbHelper dbHelper,
      int sensorIndex) {
    mContext = context;
    mHandler = handler;
    mWifiMgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    mDbHelper = dbHelper;
    mSensorIndex = sensorIndex;
    mMsgToPoller = mHandler.obtainMessage(Sensor.MSG_FROM_SENSOR);
    mMsgToPoller.arg1 = mSensorIndex;
  }

  @Override
  public void startSensor() {
    if (!mWifiMgr.isWifiEnabled()) {
      mMsgToPoller.arg2 = SENSOR_DISABLED;
      mMsgToPoller.sendToTarget();
    }

    mWifiAccessPointReceiver = new WifiAccessPointReceiver();
    mContext.registerReceiver(mWifiAccessPointReceiver, new IntentFilter(
        WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

    Log.i(TAG, "Starting WiFi AP scan.");

    mSensingStartTime = System.currentTimeMillis();

    if (mWifiMgr.startScan() == false) {
      mMsgToPoller.arg2 = SENSING_NOT_INITIATED;
      mMsgToPoller.sendToTarget();
    }
  }

  @Override
  public void stopSensor() {
    if (mWifiAccessPointReceiver != null) {
      mContext.unregisterReceiver(mWifiAccessPointReceiver);
    }
  }

  @Override
  public long getTimeoutValue() {
    return DEFAULT_TIMEOUT;
  }

  private class WifiAccessPointReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
      Log.i(TAG, "Received WiFI AP list available broadcast");
      int result = Sensor.SENSING_SUCCEEDED;
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
            wifiApData.setSensingEndTime(mSensingStartTime);
            wifiApData.setSensingEndTime(System.currentTimeMillis());
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
        result = SENSING_FAILED;
        Log.e(TAG, "Database operation failed.", e);
      } catch (Exception e) {
        result = SENSING_FAILED;
        Log.e(TAG, "Unknown error", e);
      }

      mMsgToPoller.arg2 = result;
      mMsgToPoller.sendToTarget();
    }
  }

}
