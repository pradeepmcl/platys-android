package edu.ncsu.mas.platys.android.sensor.instances;

import java.sql.SQLException;
import java.util.List;

import edu.ncsu.mas.platys.android.sensor.GenericSensor;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;
import edu.ncsu.mas.platys.common.constasnts.PlatysSensorEnum;
import edu.ncsu.mas.platys.common.sensordataobjects.WifiAccessPointData;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WiFiAccessPointSensor implements GenericSensor {

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
    WifiManager wifi_mgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    wifi_mgr.startScan();
  }

  @Override
  public void cleanUp() {
    mContext.unregisterReceiver(wifiAccessPointReceiver);
    mDatabaseHelper.close();
    mDatabaseHelper = null;
    mWifiMgr = null;
    mContext = null;
  }
}
