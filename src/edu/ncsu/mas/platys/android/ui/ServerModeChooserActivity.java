package edu.ncsu.mas.platys.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
//import com.dropbox.sync.android.DbxAccountManager;

import edu.ncsu.mas.platys.android.R;
//import edu.ncsu.mas.platys.common.constasnts.SyncConstants;

public class ServerModeChooserActivity extends Activity {

  public static final String PLATYS_SERVER_PREFS = "platys_server_prefs";

  public static final String PREFS_KEY_SERVER_MODE = "server_mode";
  public static final String PREFS_KEY_USER_EMAIL = "user_email";
  public static final String PREFS_KEY_USER_NAME = "user_name";
  public static final String PREFS_DBX_ACCESS_KEY_NAME = "dbx_access_key_name";
  public static final String PREFS_DBX_ACCESS_KEY_SECRET = "dbx_access_key_secret";

  public enum ServerMode {
    DROPBOX_APP_FOLDER, DROPBOX_SHAREABLE_FOLDER;
  }

  public static final String TAG = "Platys" + ServerModeChooserActivity.class.getName();

  private ServerMode mServerMode;

  private RadioGroup mModesRadioGroup;
  private TextView mDropboxDesc;
  private TextView mPlatysServerDesc;

  // private DbxAccountManager mDbxAcctMgr;
  private static final int REQUEST_LINK_TO_DBX = 0;

  private DropboxAPI<AndroidAuthSession> mDBApi = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_server_mode_chooser);

    mDropboxDesc = (TextView) findViewById(R.id.dropboxModeDesc);
    mPlatysServerDesc = (TextView) findViewById(R.id.serverModeDesc);
    for (ServerMode _mode : ServerMode.values()) {
      setDescriptionViewVisibility(_mode, View.GONE);
    }

    mModesRadioGroup = (RadioGroup) findViewById(R.id.modesRadioGroup);
    mModesRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton checkedButton = (RadioButton) group.findViewById(checkedId);
        if (checkedButton.isChecked()) {
          for (ServerMode _mode : ServerMode.values()) {
            setDescriptionViewVisibility(_mode, View.GONE);
          }
          if (checkedButton.getText().equals(getString(R.string.dropbox))) {
            mServerMode = ServerMode.DROPBOX_APP_FOLDER;
          } else if (checkedButton.getText().equals(getString(R.string.platys_server))) {
            mServerMode = ServerMode.DROPBOX_SHAREABLE_FOLDER;
          }
          setDescriptionViewVisibility(mServerMode, View.VISIBLE);
        }
      }
    });
  }

  private void setDescriptionViewVisibility(ServerMode mode, int visibility) {
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
          finish();
        } catch (IllegalStateException e) {
          Log.i("DbAuthLog", "Error authenticating", e);
        }
      }
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_LINK_TO_DBX) {
      if (resultCode == Activity.RESULT_OK) {
        handleServerChoice(ServerMode.DROPBOX_APP_FOLDER);
      } else {
        Log.e(TAG, "Link to Dropbox failed or was cancelled.");
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  public static String getUsername(Context context) {
    SharedPreferences mPreferences = context.getSharedPreferences(PLATYS_SERVER_PREFS,
        Context.MODE_PRIVATE);
    String serverModeString = mPreferences.getString(PREFS_KEY_SERVER_MODE, "");
    if (serverModeString.equals("")) {
      return "";
    }

    return mPreferences.getString(PREFS_KEY_USER_NAME, "");
  }

  private void handleServerChoice(ServerMode mode) {
    switch (mode) {
    case DROPBOX_APP_FOLDER:
      /*mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(),
          SyncConstants.getDbxappkey(), SyncConstants.getDbxappsecret());
      if (mDbxAcctMgr.hasLinkedAccount()) {
        storeServerDetails();
        finish();
      } else {
        mDbxAcctMgr.startLink(this, REQUEST_LINK_TO_DBX);
      }*/
      break;
    case DROPBOX_SHAREABLE_FOLDER:
      AppKeyPair appKeys = new AppKeyPair("x0qfnpdsd16e1kw", "f85z9yjy3x8fem1");
      AndroidAuthSession session = new AndroidAuthSession(appKeys, AccessType.APP_FOLDER);
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
      /*mPreferencesEditor.putString(PREFS_KEY_USER_NAME, mDbxAcctMgr.getLinkedAccount()
          .getAccountInfo().displayName);*/
      break;
    case DROPBOX_SHAREABLE_FOLDER:
      AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();
      mPreferencesEditor.putString(PREFS_DBX_ACCESS_KEY_NAME, tokens.key);
      mPreferencesEditor.putString(PREFS_DBX_ACCESS_KEY_SECRET, tokens.secret);
      /*try {
        mPreferencesEditor.putString(PREFS_KEY_USER_NAME, mDBApi.accountInfo().displayName);
      } catch (DropboxException e) {
        Log.e(TAG, "Error retrieving username", e);
        return;
      }*/
      mPreferencesEditor.putString(PREFS_KEY_USER_NAME, "Dbx");
      break;
    default:
      return;
    }

    mPreferencesEditor.commit();
  }

}
