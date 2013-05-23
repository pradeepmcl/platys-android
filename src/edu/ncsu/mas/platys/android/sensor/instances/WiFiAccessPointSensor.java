package edu.ncsu.mas.platys.android.sensor.instances;

import java.sql.SQLException;
import java.util.List;

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
import edu.ncsu.mas.platys.common.sensordata.WifiAccessPointData;

public class WiFiAccessPointSensor implements ISensor {

  private Context mContext = null;
  private WifiManager mWifiMgr = null;
  private SensorDbHelper mDatabaseHelper = null;

  public WiFiAccessPointSensor(Context context) {
    mContext = context;
    mDatabaseHelper = SensorDbHelper.getHelper(mContext);
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
          List<ScanResult> apList = mWifiMgr.getScanResults();
          WifiInfo connectedAp = mWifiMgr.getConnectionInfo();
          long curTime = System.currentTimeMillis();
          WifiAccessPointData wifiApDaata = new WifiAccessPointData();
          wifiApDaata.setSensingTime(curTime);
          wifiApDaata.setBssid(connectedAp.getBSSID());
          wifiApDaata.setIsConnected(true);
          try {
            mDatabaseHelper.getDao(PlatysSensorEnum.WIFI_ACCESS_POINT_SENSOR.getDataClass())
            .create(wifiApDaata);
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      }).start();
    }
  };

  @Override
  public void sense() {
    mWifiMgr.startScan();
  }

  @Override
  public void close() {
    mContext.unregisterReceiver(wifiAccessPointReceiver);
    mDatabaseHelper.close();
    mDatabaseHelper = null;
    mWifiMgr = null;
    mContext = null;
  }
}
