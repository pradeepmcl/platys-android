package edu.ncsu.mas.platys.android.sensor;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WiFiAccessPointSensor implements GenericSensor {

  private Context mContext;

  private BroadcastReceiver wifiAccessPointReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(final Context context, Intent _intent) {
      Log.i("Pradeep", "on receive");
      long curTime = System.currentTimeMillis();
      WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
      List<ScanResult> apList = wifiMgr.getScanResults();
      Log.i("Pradeep", "Found " + apList.size() + " at " + curTime);
      WifiInfo connectedAp = wifiMgr.getConnectionInfo();
      Log.i("Pradeep", "Connected to " + connectedAp.getSSID());
    }
  };

  public WiFiAccessPointSensor(Context context) {
    mContext = context;
    mContext.registerReceiver(wifiAccessPointReceiver, new IntentFilter(
        WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    Log.i("Pradeep", "Registered received");
  }

  @Override
  public void sense() {
    Log.i("Pradeep", "sense");
    WifiManager wifi_mgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    wifi_mgr.startScan();
  }

  @Override
  public void cleanUp() {
    mContext.unregisterReceiver(wifiAccessPointReceiver);
    Log.i("Pradeep", "Unregistered receiver");
  }
}
