package edu.ncsu.mas.platys.android.db.sensorhelper;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import edu.ncsu.mas.platys.common.sensordao.WifiAccessPointDao;

public class WifiAccessPointHelper extends OrmLiteSqliteOpenHelper {

  private static final String DATABASE_NAME = "wifi_scan_log.db";

  private static final int DATABASE_VERSION = 3;

  private Dao<WifiAccessPointDao, Integer> wifiApDao = null;
  private static final AtomicInteger usageCounter = new AtomicInteger(0);

  private static WifiAccessPointHelper helper = null;

  private WifiAccessPointHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  public static synchronized WifiAccessPointHelper getHelper(Context context) {
    if (helper == null) {
      helper = new WifiAccessPointHelper(context);
    }
    usageCounter.incrementAndGet();
    return helper;
  }

  @Override
  public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
    try {
      Log.i(WifiAccessPointHelper.class.getName(), "onCreate");
      TableUtils.createTable(connectionSource, WifiAccessPointDao.class);
    } catch (SQLException e) {
      Log.e(WifiAccessPointHelper.class.getName(), "Can't create database", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion,
      int newVersion) {
    try {
      Log.i(WifiAccessPointHelper.class.getName(), "onUpgrade " + oldVersion + " to " + newVersion);
      if (oldVersion == 1 && newVersion == 2) {
        wifiApDao.executeRaw("ALTER TABLE wifi_scan_log_wfi ADD is_connected_wfi BOOLEAN ");
        Log.i(WifiAccessPointHelper.class.getName(), "added a column is_connected_wfi");
      } else if (oldVersion < 3 && newVersion == 3) {
        TableUtils.dropTable(connectionSource, WifiAccessPointDao.class, true);
        onCreate(db, connectionSource);
      }
    } catch (SQLException e) {
      Log.e(WifiAccessPointHelper.class.getName(), "Can't upgrade database", e);
      throw new RuntimeException(e);
    }
  }

  public Dao<WifiAccessPointDao, Integer> getWifiAccessPointDao() throws SQLException {
    if (wifiApDao == null) {
      wifiApDao = getDao(WifiAccessPointDao.class);
    }
    return wifiApDao;
  }

  @Override
  public void close() {
    if (usageCounter.decrementAndGet() == 0) {
      super.close();
      wifiApDao = null;
      helper = null;
    }
  }
}
