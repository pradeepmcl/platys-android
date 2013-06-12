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
  private final Message mMsgToPoller;

  private BluetoothDeviceFoundReceiver mBluetoothDeviceFoundReceiver = null;
  
  private long mSensingStartTime;

  public BluetoothDeviceSensor(Context context, Handler handler, SensorDbHelper dbHelper,
      int sensorIndex) {
    mContext = context;
    mHandler = handler;
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    mDbHelper = dbHelper;
    mSensorIndex = sensorIndex;
    mMsgToPoller = mHandler.obtainMessage(Sensor.MSG_FROM_SENSOR, mSensorIndex);
    mMsgToPoller.arg1 = mSensorIndex;
  }

  @Override
  public void startSensor() {
    if (!mBluetoothAdapter.isEnabled()) {
      mMsgToPoller.arg2 = SENSOR_DISABLED;
      mMsgToPoller.sendToTarget();
    }

    mBluetoothDeviceFoundReceiver = new BluetoothDeviceFoundReceiver();
    mContext.registerReceiver(mBluetoothDeviceFoundReceiver, new IntentFilter(
        BluetoothDevice.ACTION_FOUND));

    if (!mBluetoothAdapter.isDiscovering()) {
      Log.i(TAG, "Starting Bluetooth discovery");
      mSensingStartTime = System.currentTimeMillis();

      if (mBluetoothAdapter.startDiscovery() == false) {
        mMsgToPoller.arg2 = SENSING_NOT_INITIATED;
        mMsgToPoller.sendToTarget();
      }
    }

  }

  @Override
  public void stopSensor() {
    if (mBluetoothDeviceFoundReceiver != null) {
      mContext.unregisterReceiver(mBluetoothDeviceFoundReceiver);
    }
  }

  @Override
  public long getTimeoutValue() {
    return DEFAULT_TIMEOUT;
  }

  private class BluetoothDeviceFoundReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
      Log.i(TAG, "Received Bluetooth device found broadcast");
      int result = SENSING_SUCCEEDED;
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
        result = SENSING_FAILED;
        Log.e(TAG, "Database operation failed.", e);
      }

      mMsgToPoller.arg2 = result;
      mMsgToPoller.sendToTarget();
    }
  };

}
