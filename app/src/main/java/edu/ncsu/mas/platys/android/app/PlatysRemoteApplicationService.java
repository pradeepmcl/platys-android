package edu.ncsu.mas.platys.android.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PlatysRemoteApplicationService extends Service {
  private final IPlatysRemoteApplicationService.Stub mBinder = new PlatysRemoteApplicationBinder();

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }
}
