package edu.ncsu.mas.platys.android.sensor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.SQLException;

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

  private Context mContext;

  public SensorDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    mContext = context;
  }

  @Override
  public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
    try {
      for (PlatysSensorEnum sensor : PlatysSensorEnum.values()) {
        TableUtils.createTable(connectionSource, sensor.getDataClass());
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
    mContext = null;
  }

  public void backup(FileOutputStream writeStream) {
    FileChannel src = null;
    FileChannel dst = null;

    if (true) {
      try {
        File currentDB = mContext.getDatabasePath(DATABASE_NAME);
        if (currentDB.exists()) {
          src = new FileInputStream(currentDB).getChannel();
          dst = writeStream.getChannel();
          dst.transferFrom(src, 0, src.size());
        }
      } catch (IOException e) {
        Log.e(SensorDbHelper.class.getName(), "Can't backup database", e);
        throw new RuntimeException(e);
      } finally {
        try {
          if (src != null) {
            src.close();
          }
          if (dst != null) {
            dst.close();
          }
        } catch (IOException e) {
          Log.e(SensorDbHelper.class.getName(), "Can't close backup files");
          throw new RuntimeException(e);
        }
      }
    }
  }

}
