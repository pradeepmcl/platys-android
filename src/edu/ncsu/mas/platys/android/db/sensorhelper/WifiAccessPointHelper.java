package edu.ncsu.mas.platys.android.db.sensorhelper;

import java.sql.SQLException;

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

  private static final int DATABASE_VERSION = 1;

  private Dao<WifiAccessPointDao, Integer> wifiApDao = null;

  public WifiAccessPointHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
    // Do nothing for now.
  }

  public Dao<WifiAccessPointDao, Integer> getWifiAccessPointDao() throws SQLException {
    if (wifiApDao == null) {
      wifiApDao = getDao(WifiAccessPointDao.class);
    }
    return wifiApDao;
  }

  @Override
  public void close() {
    super.close();
    wifiApDao = null;
  }
}
