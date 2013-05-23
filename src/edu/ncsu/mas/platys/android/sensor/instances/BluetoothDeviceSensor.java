package edu.ncsu.mas.platys.android.sensor.instances;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

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
  private BluetoothAdapter mBluetoothAdapter = null;
  private SensorDbHelper mDbHelper = null;

  private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
  
  private ScheduledFuture<?> mBluetoothSensorHandle;
  
  public BluetoothDeviceSensor(Context context, SensorDbHelper dbHelper) {
    mContext = context;
    mDbHelper = dbHelper;
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    mContext.registerReceiver(bluetoothScanResultReceiver, new IntentFilter(
        BluetoothDevice.ACTION_FOUND));
  }

  @Override
  public void startSensing() {
    mBluetoothSensorHandle = mScheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mBluetoothAdapter.startDiscovery();
      }
    }, 10, 2 * 60, SECONDS);
    
  }

  @Override
  public void stopSensing() {
    mBluetoothSensorHandle.cancel(true);
    mContext.unregisterReceiver(bluetoothScanResultReceiver);
    mDbHelper = null;
    mBluetoothAdapter = null;
    mContext = null;
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
            mDbHelper.getDao(PlatysSensorEnum.BLUETOOTH_DEVICE_SENSOR.getDataClass()).create(
                btDeviceData);
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      }).start();
    }
  };
}
