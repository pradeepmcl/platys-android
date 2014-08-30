package edu.ncsu.mas.platys.android.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PlatysAppService extends Service {
  private final IPlatysAppService.Stub mBinder = new PlatysAppBinder();

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }
}
