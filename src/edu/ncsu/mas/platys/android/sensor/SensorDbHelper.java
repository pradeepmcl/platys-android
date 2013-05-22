package edu.ncsu.mas.platys.android.sensor;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import edu.ncsu.mas.platys.common.constasnts.PlatysSensorEnum;

public class SensorDbHelper extends OrmLiteSqliteOpenHelper {
  private static final String DATABASE_NAME = "sensor.db";

  private static final int DATABASE_VERSION = 1;

  private static final AtomicInteger usageCounter = new AtomicInteger(0);

  private static SensorDbHelper helper = null;

  private SensorDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  public static synchronized SensorDbHelper getHelper(Context context) {
    if (helper == null) {
      helper = new SensorDbHelper(context);
    }
    usageCounter.incrementAndGet();
    return helper;
  }

  @Override
  public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
    try {
      for (PlatysSensorEnum sensor : PlatysSensorEnum.values()) {
        TableUtils.createTable(connectionSource, sensor.getClass());
      }
      
    } catch (SQLException e) {
      Log.e(SensorDbHelper.class.getName(), "Can't create database", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion,
      int newVersion) {
    // Do nothing for now.
  }

  @Override
  public void close() {
    if (usageCounter.decrementAndGet() == 0) {
      super.close();
      helper = null;
    }
  }
}
