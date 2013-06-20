package edu.ncsu.mas.platys.android.labels;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import edu.ncsu.mas.platys.android.PlatysReceiver;
import edu.ncsu.mas.platys.android.PlatysService;
import edu.ncsu.mas.platys.android.sensor.SensorDbHelper;
import edu.ncsu.mas.platys.common.sensordata.PlaceLabelData.LabelType;

public class PlaceLabelSaver implements Runnable {
  private static final String TAG = "Platys" + PlaceLabelSaver.class.getSimpleName();

  private final Context mContext;
  private final Handler mServiceHandler;
  private final SensorDbHelper mSensorDbHelper;
  private final Intent mDetailsIntent;

  public PlaceLabelSaver(Context context, Handler serviceHandler, SensorDbHelper sensorDbHelper,
      Intent intent) {
    Log.i(TAG, "Creating SyncHandler.");
    mContext = context;
    mServiceHandler = serviceHandler;
    mSensorDbHelper = sensorDbHelper;
    mDetailsIntent = intent;
  }

  @Override
  public void run() {
    Log.i(TAG, "Running PlaceLabelSaver.");

    try {
      ArrayList<String> labelList = mDetailsIntent
          .getStringArrayListExtra(PlatysReceiver.EXTRA_LABELS_LIST);
      Log.i(TAG, labelList.toString());
      ArrayList<LabelType> labelTypeList = (ArrayList<LabelType>) mDetailsIntent
          .getSerializableExtra(PlatysReceiver.EXTRA_LABEL_TYPES_LIST);
      Log.i(TAG, labelTypeList.get(0).name());
      
    } finally {
      Message msgToService = mServiceHandler
          .obtainMessage(PlatysService.PLATYS_MSG_SAVE_LABELS_FINISHED);
      msgToService.arg1 = 0;
      msgToService.sendToTarget();
    }
  }
}
