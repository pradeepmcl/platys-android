package edu.ncsu.mas.platys.android.sensor;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import edu.ncsu.mas.platys.common.constasnts.PlatysSensorEnum;
import edu.ncsu.mas.platys.common.sensordata.PlaceLabelData;
import edu.ncsu.mas.platys.common.sensordata.SensorDbInstanceInfo;

public class SensorDbHelper extends OrmLiteSqliteOpenHelper {

  private static final String DATABASE_NAME = "sensor.db";

  private static final int DATABASE_VERSION = 1;

  public SensorDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
    try {
      for (PlatysSensorEnum sensor : PlatysSensorEnum.values()) {
        TableUtils.createTableIfNotExists(connectionSource, sensor.getDataClass());
      }
      TableUtils.createTableIfNotExists(connectionSource, PlaceLabelData.class);
      
      addDbInstanceInfo();
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
    super.close();
  }

  private void addDbInstanceInfo() throws SQLException {
    Dao<SensorDbInstanceInfo, ?> instanceInfoDao = getDao(PlatysSensorEnum.SENSOR_DB_INSTANCE_INFO
        .getDataClass());
    if (instanceInfoDao.countOf() == 0) {
      SensorDbInstanceInfo instanceInfo = new SensorDbInstanceInfo();
      instanceInfo.setCreationTime(System.currentTimeMillis());
      instanceInfo.setHostMacAddr(0L); // TODO
      instanceInfoDao.create(instanceInfo);
    }
  }

}
