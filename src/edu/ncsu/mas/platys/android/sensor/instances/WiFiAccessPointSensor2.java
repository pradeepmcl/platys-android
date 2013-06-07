package edu.ncsu.mas.platys.android.sensor.instances;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

public class WiFiAccessPointSensor2 {

  private final Context mContext;
  private final Handler mHandler;
  private final WifiManager mWifiMgr;

  public WiFiAccessPointSensor2(Context context, Handler handler) {
    mContext = context;
    mHandler = handler;
    mWifiMgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
  }

  private final BroadcastReceiver wifiAccessPointReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(final Context context, Intent intent) {
      List<ScanResult> apList = mWifiMgr.getScanResults();
      Log.i("Pradeep", "Received" + apList.size() + " APs");
      mHandler.sendEmptyMessage(101);
    }
  };
  
  public void startSensor() {
    mContext.registerReceiver(wifiAccessPointReceiver, new IntentFilter(
        WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    mWifiMgr.startScan();
  }

  public void stopSensor() {
    mContext.unregisterReceiver(wifiAccessPointReceiver);
  }
  
}
