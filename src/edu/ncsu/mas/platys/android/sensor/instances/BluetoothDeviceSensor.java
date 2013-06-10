package edu.ncsu.mas.platys.android.sensor.instances;

import java.sql.SQLException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import edu.ncsu.mas.platys.android.sensor.Sensor;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;
import edu.ncsu.mas.platys.common.constasnts.PlatysSensorEnum;
import edu.ncsu.mas.platys.common.sensordata.BluetoothDeviceData;

public class BluetoothDeviceSensor implements Sensor {

  private static final String TAG = "Platys" + BluetoothDeviceSensor.class.getSimpleName();

  private static final long DEFAULT_TIMEOUT = 120000; // two minutes

  private final Context mContext;
  private final Handler mHandler;
  private final BluetoothAdapter mBluetoothAdapter;
  private final SensorDbHelper mDbHelper;
  private final int mSensorIndex;
  
  private long mSensingStartTime;

  public BluetoothDeviceSensor(Context context, Handler handler, SensorDbHelper dbHelper,
      int sensorIndex) {
    mContext = context;
    mHandler = handler;
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    mDbHelper = dbHelper;
    mSensorIndex = sensorIndex;
  }

  @Override
  public boolean startSensor() {
    if (!mBluetoothAdapter.isEnabled()) {
      return false;
    }

    mContext.registerReceiver(bluetoothDeviceFoundReceiver, new IntentFilter(
        BluetoothDevice.ACTION_FOUND));

    if (!mBluetoothAdapter.isDiscovering()) {
      Log.i(TAG, "Starting Bluetooth discovery");
      mSensingStartTime = System.currentTimeMillis();
      return mBluetoothAdapter.startDiscovery();
    }

    return true;
  }

  @Override
  public boolean stopSensor() {
    if (bluetoothDeviceFoundReceiver != null) {
      mContext.unregisterReceiver(bluetoothDeviceFoundReceiver);
    }
    return true;
  }

  @Override
  public long getTimeoutValue() {
    return DEFAULT_TIMEOUT;
  }

  private final BroadcastReceiver bluetoothDeviceFoundReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(final Context context, Intent intent) {
      Log.i(TAG, "Received Bluetooth device found broadcast");
      int result = Sensor.SENSING_SUCCEEDED;
      BluetoothDevice dev = intent
          .getParcelableExtra(android.bluetooth.BluetoothDevice.EXTRA_DEVICE);
      Short devRssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0);

      BluetoothDeviceData btDeviceData = new BluetoothDeviceData();
      btDeviceData.setSensingStartTime(mSensingStartTime);
      btDeviceData.setSensingEndTime(System.currentTimeMillis());
      btDeviceData.setBssid(dev.getAddress());
      btDeviceData.setSsid(dev.getName());
      btDeviceData.setRssi(devRssi);
      try {
        mDbHelper.getDao(PlatysSensorEnum.BLUETOOTH_DEVICE_SENSOR.getDataClass()).create(
            btDeviceData);
      } catch (SQLException e) {
        result = Sensor.SENSING_FAILED;
        Log.e(TAG, "Database operation failed.", e);
      }

      Message msg = mHandler.obtainMessage(Sensor.MSG_FROM_SENSOR, mSensorIndex, result);
      msg.sendToTarget();
    }
  };

}
