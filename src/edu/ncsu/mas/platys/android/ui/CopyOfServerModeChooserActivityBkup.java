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

import com.dropbox.sync.android.DbxAccountManager;

import edu.ncsu.mas.platys.android.R;
import edu.ncsu.mas.platys.common.constasnts.SyncConstants;

public class CopyOfServerModeChooserActivityBkup extends Activity {

  public enum ServerMode {
    DROPBOX, PLATYS_SEVER;
  }

  public static final String TAG = "Platys" + CopyOfServerModeChooserActivityBkup.class.getName();

  private ServerMode mServerMode;
  private String mUsername;

  private RadioGroup mModesRadioGroup;
  private TextView mDropboxDesc;
  private TextView mPlatysServerDesc;

  private DbxAccountManager mDbxAcctMgr;
  private static final int REQUEST_LINK_TO_DBX = 0;

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
            mServerMode = ServerMode.DROPBOX;
          } else if (checkedButton.getText().equals(getString(R.string.platys_server))) {
            mServerMode = ServerMode.PLATYS_SEVER;
          }
          setDescriptionViewVisibility(mServerMode, View.VISIBLE);
        }
      }
    });
  }

  private void setDescriptionViewVisibility(ServerMode mode, int visibility) {
    TextView modeDescView;
    switch (mode) {
    case DROPBOX:
      modeDescView = mDropboxDesc;
      break;
    case PLATYS_SEVER:
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

  private void handleServerChoice(ServerMode mode) {
    switch (mode) {
    case DROPBOX:
      mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(),
          SyncConstants.getDbxappkey(), SyncConstants.getDbxappsecret());
      if (mDbxAcctMgr.hasLinkedAccount()) {
        mUsername = mDbxAcctMgr.getLinkedAccount().getAccountInfo().displayName;
        storeServerDetails();
        finish();
      } else {
        mDbxAcctMgr.startLink(this, REQUEST_LINK_TO_DBX);
      }
      break;
    case PLATYS_SEVER:
      // TODO
      break;
    default:
      return;
    }
  }

  private void storeServerDetails() {
    SharedPreferences.Editor mPreferencesEditor = getSharedPreferences(PlatysActivity.PLATYS_PREFS,
        Context.MODE_PRIVATE).edit();
    mPreferencesEditor.putString(PlatysActivity.PREFS_KEY_SERVER_MODE, mServerMode.name());
    mPreferencesEditor.putString(PlatysActivity.PREFS_KEY_USERNAME, mUsername);
    mPreferencesEditor.commit();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_LINK_TO_DBX) {
      if (resultCode == Activity.RESULT_OK) {
        handleServerChoice(ServerMode.DROPBOX);
      } else {
        Log.e(TAG, "Link to Dropbox failed or was cancelled.");
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }
}
