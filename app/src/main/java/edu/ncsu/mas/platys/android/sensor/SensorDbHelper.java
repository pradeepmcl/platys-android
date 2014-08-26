package edu.ncsu.mas.platys.android.sensor;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import edu.ncsu.mas.platys.android.ui.ServerModeChooserActivity;
import edu.ncsu.mas.platys.common.sensor.PlatysCommonSensor;
import edu.ncsu.mas.platys.common.sensor.PlatysInstanceInfo;
import edu.ncsu.mas.platys.common.sensor.PlatysInstanceInfo.ServerType;

public class SensorDbHelper extends OrmLiteSqliteOpenHelper {

  private static final String DATABASE_NAME = "sensor.db";

  private static final int DATABASE_VERSION = 1;

  private final Context mContext;

  public SensorDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);

    mContext = context;
  }

  @Override
  public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
    try {
      addInstanceInfo(connectionSource);
      for (PlatysCommonSensor sensor : PlatysCommonSensor.values()) {
        TableUtils.createTableIfNotExists(connectionSource, sensor.getDataClass());
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
    super.close();
  }

  private void addInstanceInfo(ConnectionSource connectionSource) throws SQLException {
    Dao<PlatysInstanceInfo, ?> instanceInfoDao = getDao(PlatysInstanceInfo.class);
    TableUtils.createTableIfNotExists(connectionSource, PlatysInstanceInfo.class);

    ServerType serverType = ServerModeChooserActivity.getServerMode(mContext);
    String userId = ServerModeChooserActivity.getUserId(mContext);

    if (instanceInfoDao.countOf(instanceInfoDao.queryBuilder().setCountOf(true).where()
        .eq(PlatysInstanceInfo.SERVER_TYPE_CLM, serverType).and()
        .eq(PlatysInstanceInfo.USER_ID_CLM, userId).prepare()) == 0) {
      PlatysInstanceInfo instanceInfo = new PlatysInstanceInfo();
      instanceInfo.setCreationTime(System.currentTimeMillis());
      instanceInfo.setServerType(serverType);
      instanceInfo.setUserId(userId);
      instanceInfo.setUserDetails("Displayname:" + ServerModeChooserActivity.getUsername(mContext));
      instanceInfoDao.create(instanceInfo);
    }
  }

}
