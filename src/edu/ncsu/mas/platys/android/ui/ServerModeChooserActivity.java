package edu.ncsu.mas.platys.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Account;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

import edu.ncsu.mas.platys.android.R;
import edu.ncsu.mas.platys.common.sensordata.PlatysInstanceInfo.ServerType;
import edu.ncsu.mas.platys.common.constasnts.SyncConstants;

public class ServerModeChooserActivity extends Activity {

  public static final String PLATYS_SERVER_PREFS = "platys_server_prefs";

  public static final String PREFS_KEY_SERVER_MODE = "server_mode";

  public static final String PREFS_SERVER_USER_NAME = "server_user_name";
  public static final String PREFS_SERVER_USER_ID = "server_user_id";
  public static final String PREFS_SERVER_USER_EMAIL = "server_user_email";

  public static final String PREFS_DBX_ACCESS_TYPE = "dbx_access_type";
  public static final String PREFS_DBX_ACCESS_KEY_NAME = "dbx_access_key_name";
  public static final String PREFS_DBX_ACCESS_KEY_SECRET = "dbx_access_key_secret";

  private static final String TAG = "Platys" + ServerModeChooserActivity.class.getName();

  private ServerType mServerMode;

  private AccessType mDbxAccessType;

  private RadioGroup mModesRadioGroup;
  private TextView mDropboxDesc;
  private TextView mPlatysServerDesc;

  private DropboxAPI<AndroidAuthSession> mDBApi = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

    setContentView(R.layout.activity_server_mode_chooser);

    setProgressBarIndeterminateVisibility(false);

    mDropboxDesc = (TextView) findViewById(R.id.tvDbxAppFolderModeDescServerModeChooser);
    mPlatysServerDesc = (TextView) findViewById(R.id.tvDbxSharableFolderModeDescServerModeChooser);
    for (ServerType _mode : ServerType.values()) {
      setDescriptionViewVisibility(_mode, View.GONE);
    }

    mModesRadioGroup = (RadioGroup) findViewById(R.id.rgServerModesServerModelChooser);
    mModesRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton checkedButton = (RadioButton) group.findViewById(checkedId);
        if (checkedButton.isChecked()) {
          for (ServerType _mode : ServerType.values()) {
            setDescriptionViewVisibility(_mode, View.GONE);
          }
          if (checkedButton.getText().equals(
              getString(R.string.activity_server_mode_chooser_dropbox_app_folder))) {
            mServerMode = ServerType.DROPBOX_APP_FOLDER;
            mDbxAccessType = AccessType.APP_FOLDER;
          } else if (checkedButton.getText().equals(
              getString(R.string.activity_server_mode_chooser_dropbox_sharable_folder))) {
            mServerMode = ServerType.DROPBOX_SHAREABLE_FOLDER;
            mDbxAccessType = AccessType.DROPBOX;
          }
          setDescriptionViewVisibility(mServerMode, View.VISIBLE);
        }
      }
    });
  }

  private void setDescriptionViewVisibility(ServerType mode, int visibility) {
    TextView modeDescView;
    switch (mode) {
    case DROPBOX_APP_FOLDER:
      modeDescView = mDropboxDesc;
      break;
    case DROPBOX_SHAREABLE_FOLDER:
      modeDescView = mPlatysServerDesc;
      break;
    default:
      return;
    }

    modeDescView.setVisibility(visibility);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.server_mode_chooser, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.menu_server_mode_next:
      handleServerChoice(mServerMode);
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (mDBApi != null) {
      if (mDBApi.getSession().authenticationSuccessful()) {
        try {
          mDBApi.getSession().finishAuthentication();
          storeServerDetails();
        } catch (IllegalStateException e) {
          Log.i("DbAuthLog", "Error authenticating", e);
        }
      }
    }
  }

  private void handleServerChoice(ServerType mode) {
    switch (mode) {
    case DROPBOX_APP_FOLDER:
      // Continue to DROPBOX_SHAREABLE_FOLDER.

    case DROPBOX_SHAREABLE_FOLDER:
      AppKeyPair appKeys = new AppKeyPair(SyncConstants.getDbxappkey(),
          SyncConstants.getDbxappsecret());
      AndroidAuthSession session = new AndroidAuthSession(appKeys, mDbxAccessType);
      mDBApi = new DropboxAPI<AndroidAuthSession>(session);
      mDBApi.getSession().startAuthentication(ServerModeChooserActivity.this);
      break;

    default:
      return;
    }
  }

  private void storeServerDetails() {
    SharedPreferences.Editor mPreferencesEditor = getSharedPreferences(PLATYS_SERVER_PREFS,
        Context.MODE_PRIVATE).edit();
    mPreferencesEditor.putString(PREFS_KEY_SERVER_MODE, mServerMode.name());

    switch (mServerMode) {
    case DROPBOX_APP_FOLDER:
      // Continue to DROPBOX_SHAREABLE_FOLDER

    case DROPBOX_SHAREABLE_FOLDER:
      mPreferencesEditor.putString(PREFS_DBX_ACCESS_TYPE, mDbxAccessType.name());
      AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();
      mPreferencesEditor.putString(PREFS_DBX_ACCESS_KEY_NAME, tokens.key);
      mPreferencesEditor.putString(PREFS_DBX_ACCESS_KEY_SECRET, tokens.secret);
      new DbxUserAccountFetcherTask().execute();
      break;

    default:
      return;
    }

    mPreferencesEditor.commit();
  }

  public static ServerType getServerMode(Context context) {
    SharedPreferences mPreferences = context.getSharedPreferences(PLATYS_SERVER_PREFS,
        Context.MODE_PRIVATE);
    String serverModeString = mPreferences.getString(PREFS_KEY_SERVER_MODE, "");

    if (serverModeString.length() == 0) {
      return ServerType.UNKNOWN;
    }
    return ServerType.valueOf(serverModeString);
  }

  public static String getUserId(Context context) {
    SharedPreferences mPreferences = context.getSharedPreferences(PLATYS_SERVER_PREFS,
        Context.MODE_PRIVATE);
    return mPreferences.getString(PREFS_SERVER_USER_ID, "");
  }
  
  public static String getUsername(Context context) {
    SharedPreferences mPreferences = context.getSharedPreferences(PLATYS_SERVER_PREFS,
        Context.MODE_PRIVATE);
    return mPreferences.getString(PREFS_SERVER_USER_NAME, "");
  }

  public static AndroidAuthSession getDbxSession(Context context) {
    AppKeyPair appKeys = new AppKeyPair(SyncConstants.getDbxappkey(),
        SyncConstants.getDbxappsecret());
    
    SharedPreferences mPreferences = context.getSharedPreferences(PLATYS_SERVER_PREFS,
        Context.MODE_PRIVATE);
    String accessKey = mPreferences.getString(PREFS_DBX_ACCESS_KEY_NAME, "");
    String accessSecret = mPreferences.getString(PREFS_DBX_ACCESS_KEY_SECRET, "");
    String accessTypeStr = mPreferences.getString(PREFS_DBX_ACCESS_TYPE, "");
    
    if (accessKey.length() == 0 || accessSecret.length() == 0 || accessTypeStr.length() == 0) {
      throw new IllegalStateException("One of the cached values is empty (" + accessKey.length()
          + ", " + accessSecret.length() + ", " + accessTypeStr.length() + ")");
    }

    return (new AndroidAuthSession(appKeys, AccessType.valueOf(accessTypeStr), new AccessTokenPair(
        accessKey, accessSecret)));
  }
  
  public static AccessTokenPair getDbxAccessTokenPair(Context context) {
    SharedPreferences mPreferences = context.getSharedPreferences(PLATYS_SERVER_PREFS,
        Context.MODE_PRIVATE);
    String accessKey = mPreferences.getString(PREFS_DBX_ACCESS_KEY_NAME, "");
    String accessSecret = mPreferences.getString(PREFS_DBX_ACCESS_KEY_SECRET, "");

    return (new AccessTokenPair(accessKey, accessSecret));
  }

  private class DbxUserAccountFetcherTask extends AsyncTask<Void, Void, Account> {
    @Override
    protected void onPreExecute() {
      ServerModeChooserActivity.this.setProgressBarIndeterminateVisibility(true);
    }
    @Override
    protected Account doInBackground(Void... arg0) {
      Account retAccount = null;
      try {
        retAccount = mDBApi.accountInfo();
      } catch (DropboxException e) {
        Log.e(TAG, "Can't fetch account info", e);
      }
      return retAccount;
    }

    @Override
    protected void onPostExecute(Account account) {
      if (account != null) {
        SharedPreferences.Editor mPreferencesEditor = getSharedPreferences(PLATYS_SERVER_PREFS,
            Context.MODE_PRIVATE).edit();
        mPreferencesEditor.putString(PREFS_SERVER_USER_ID, Long.toString(account.uid));
        mPreferencesEditor.putString(PREFS_SERVER_USER_NAME, account.displayName);
        mPreferencesEditor.commit();

        ServerModeChooserActivity.this.setProgressBarIndeterminateVisibility(false);

        ServerModeChooserActivity.this.finish();
      }
    }
  }

}
