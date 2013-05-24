package edu.ncsu.mas.platys.android.sensor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import edu.ncsu.mas.platys.common.constasnts.PlatysSensorEnum;

public class SensorDbHelper extends OrmLiteSqliteOpenHelper {

  private static final String DATABASE_NAME = "sensor.db";

  private static final int DATABASE_VERSION = 1;

  private static final String DATABASE_PATH = "//data//data//edu.ncsu.mas.platys.android//databases//"
      + DATABASE_NAME;

  private static final String BACKUP_PATH = "PlatysBackup";

  public SensorDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
    super.close();
  }

  public void backup() {
    Log.i("Pradeep", "Backing up");
    File sd = Environment.getExternalStorageDirectory();
    File data = Environment.getDataDirectory();

    FileChannel src = null;
    FileChannel dst = null;

    if (sd.canWrite()) {
      try {
        File currentDB = new File(data, DATABASE_PATH);
        File backupDB = new File(sd, BACKUP_PATH);
        if (currentDB.exists()) {
          src = new FileInputStream(currentDB).getChannel();
          dst = new FileOutputStream(backupDB).getChannel();
          Log.i("Pradeep","About to transfer");
          dst.transferFrom(src, 0, src.size());
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (src != null) {
          try {
            src.close();
          } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
          }
        }
        if (dst != null) {
          try {
            dst.close();
          } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
          }
        }
      }
    }
  }
}
