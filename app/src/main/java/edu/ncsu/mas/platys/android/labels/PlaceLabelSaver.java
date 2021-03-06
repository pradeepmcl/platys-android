package edu.ncsu.mas.platys.android.labels;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import edu.ncsu.mas.platys.android.PlatysReceiver;
import edu.ncsu.mas.platys.android.PlatysService.PlatysTask;
import edu.ncsu.mas.platys.android.PlatysTaskHandler;
import edu.ncsu.mas.platys.android.sensor.PlatysSensor.SensorMsg;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;
import edu.ncsu.mas.platys.common.sensor.PlatysCommonSensor;
import edu.ncsu.mas.platys.common.sensor.datatypes.PlaceLabelData;
import edu.ncsu.mas.platys.common.sensor.datatypes.PlaceLabelData.LabelType;
import edu.ncsu.mas.platys.common.sensor.datatypes.SensorData;

public class PlaceLabelSaver implements Runnable, PlatysTaskHandler {
  private static final String TAG = "Platys" + PlaceLabelSaver.class.getSimpleName();

  private final Handler mServiceHandler;
  private final SensorDbHelper mSensorDbHelper;
  private final Intent mDetailsIntent;

  private Thread mLabelSaverThread = null;

  public PlaceLabelSaver(Handler serviceHandler, SensorDbHelper sensorDbHelper, Intent intent) {
    Log.i(TAG, "Creating PlaceLabelSaver.");
    mServiceHandler = serviceHandler;
    mSensorDbHelper = sensorDbHelper;
    mDetailsIntent = intent;
  }

  @Override
  public void startTask() {
    mLabelSaverThread = new Thread(this);
    mLabelSaverThread.start();
  }

  @Override
  public void run() {
    Log.i(TAG, "Running PlaceLabelSaver.");
    int result = SensorMsg.SENSING_SUCCEEDED.ordinal();
    try {
      final long sensingStartTime = mDetailsIntent.getLongExtra(
          PlatysReceiver.EXTRA_LABELING_START_TIME, 0);
      final long sensingEndTime = mDetailsIntent.getLongExtra(
          PlatysReceiver.EXTRA_LABELING_END_TIME, System.currentTimeMillis());
      final ArrayList<String> labelList = mDetailsIntent
          .getStringArrayListExtra(PlatysReceiver.EXTRA_LABELS_LIST);
      @SuppressWarnings("unchecked")
      final ArrayList<LabelType> labelTypeList = (ArrayList<LabelType>) mDetailsIntent
          .getSerializableExtra(PlatysReceiver.EXTRA_LABEL_TYPES_LIST);

      final Dao<SensorData, ?> sensorDao = mSensorDbHelper
          .getDao(PlatysCommonSensor.PLACE_LABEL_SENSOR.getDataClass());

      sensorDao.callBatchTasks(new Callable<Void>() {
        @Override
        public Void call() throws SQLException {
          PlaceLabelData placeLabelData = new PlaceLabelData();
          placeLabelData.setSensingEndTime(sensingStartTime);
          placeLabelData.setSensingEndTime(sensingEndTime);
          for (int i = 0; i < labelList.size(); i++) {
            placeLabelData.setLabel(labelList.get(i));
            placeLabelData.setLabelType(labelTypeList.get(i));
            sensorDao.create(placeLabelData);
          }

          return null;
        }
      });
    } catch (SQLException e) {
      result = SensorMsg.SENSING_FAILED.ordinal();
      Log.e(TAG, "Database operation failed.", e);
    } catch (Exception e) {
      result = SensorMsg.SENSING_FAILED.ordinal();
      Log.e(TAG, "Unknown error", e);
    } finally {
      Message msgToService = mServiceHandler.obtainMessage(PlatysTask.SAVE_LABELS
          .ordinal());
      msgToService.arg1 = result;
      msgToService.sendToTarget();
    }
  }

  @Override
  public void stopTask() {
    // TODO Auto-generated method stub

  }
}
