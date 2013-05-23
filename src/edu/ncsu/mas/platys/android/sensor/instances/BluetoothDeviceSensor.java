package edu.ncsu.mas.platys.android.sensor.instances;

import java.sql.SQLException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import edu.ncsu.mas.platys.android.sensor.ISensor;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;
import edu.ncsu.mas.platys.common.constasnts.PlatysSensorEnum;
import edu.ncsu.mas.platys.common.sensordata.BluetoothDeviceData;

public class BluetoothDeviceSensor implements ISensor {

  private Context mContext = null;
  private BluetoothAdapter btAdapter = null;
  private SensorDbHelper mDatabaseHelper = null;

  public BluetoothDeviceSensor(Context context) {
    mContext = context;
    mDatabaseHelper = SensorDbHelper.getHelper(mContext);
    btAdapter = BluetoothAdapter.getDefaultAdapter();
    mContext.registerReceiver(bluetoothScanResultReceiver, new IntentFilter(
        BluetoothDevice.ACTION_FOUND));
  }

  private final BroadcastReceiver bluetoothScanResultReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(final Context context, final Intent intent) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
          BluetoothDevice dev = intent
              .getParcelableExtra(android.bluetooth.BluetoothDevice.EXTRA_DEVICE);
          Short devRssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0);

          BluetoothDeviceData btDeviceData = new BluetoothDeviceData();
          btDeviceData.setSensingTime(System.currentTimeMillis());
          btDeviceData.setBssid(dev.getAddress());
          btDeviceData.setSsid(dev.getName());
          btDeviceData.setRssi(devRssi);
          try {
            mDatabaseHelper.getDao(PlatysSensorEnum.BLUETOOTH_DEVICE_SENSOR.getDataClass()).create(btDeviceData);
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      }).start();
    }
  };

  @Override
  public void sense() {
    btAdapter.startDiscovery();
  }

  @Override
  public void close() {
    mContext.unregisterReceiver(bluetoothScanResultReceiver);
    mDatabaseHelper.close();
    mDatabaseHelper = null;
    btAdapter = null;
    mContext = null;
  }
}
