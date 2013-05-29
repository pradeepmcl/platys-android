package edu.ncsu.mas.platys.android.sensor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import edu.ncsu.mas.platys.android.sensor.instances.BluetoothDeviceSensor;
import edu.ncsu.mas.platys.android.sensor.instances.WiFiAccessPointSensor;

public class SensorManager {

  private Context mContext = null;

  SensorDbHelper dbHelper = null;

  private WiFiAccessPointSensor mWiFiAccessPointSensor = null;
  private BluetoothDeviceSensor mBluetoothSensor = null;

  public SensorManager(Context context) {
    mContext = context;
    initSensors();
  }

  public void close() {
    stopSensors();
    
    if (dbHelper != null) {
      OpenHelperManager.releaseHelper();
      dbHelper = null;
    }

    mContext = null;
  }
  
  public synchronized boolean createSensorDbBackup(final File backupDir) {
    final ExecutorService backupTasks = Executors.newSingleThreadExecutor();
    
    backupTasks.submit(new Runnable() {
      @Override
      public void run() {
        stopSensors();
      }
    });
    
    backupTasks.submit(new Runnable() {
      @Override
      public void run() {
        try {
          getHelper().backup(backupDir);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });
    
    backupTasks.submit(new Runnable() {
      @Override
      public void run() {
        initSensors();
      }
    });
    
    return true;
  }

  private void initSensors() {
    mWiFiAccessPointSensor = new WiFiAccessPointSensor(mContext, getHelper());
    mBluetoothSensor = new BluetoothDeviceSensor(mContext, getHelper());
  }
  
  private void stopSensors() {
    if (mWiFiAccessPointSensor != null) {
      mWiFiAccessPointSensor.stopSensing();
      mWiFiAccessPointSensor = null;
    }

    if (mBluetoothSensor != null) {
      mBluetoothSensor.stopSensing();
      mBluetoothSensor = null;
    }
  }
  
  private SensorDbHelper getHelper() {
    if (dbHelper == null) {
      dbHelper = OpenHelperManager.getHelper(mContext, SensorDbHelper.class);
    }
    return dbHelper;
  }
}
