package edu.ncsu.mas.platys.android.ui;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;
import edu.ncsu.mas.platys.android.R;
import edu.ncsu.mas.platys.android.ui.fragment.AppsFragment;
import edu.ncsu.mas.platys.android.ui.fragment.PlacesFragment;
import edu.ncsu.mas.platys.android.ui.fragment.SensorsFragment;

public class PlatysActivity extends Activity {

  public static final String PLATYS_PREFS = "platys_prefs";

  public static final String PREFS_KEY_SERVER_MODE = "server_mode";
  public static final String PREFS_KEY_USERNAME = "username";

  private SharedPreferences mPreferences;

  private ActionBar mActionBar;

  private ServerModeChooserActivity.ServerMode mServerMode;
  private String mUsername;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPreferences = getSharedPreferences(PLATYS_PREFS, Context.MODE_PRIVATE);

    try {
      mServerMode = ServerModeChooserActivity.ServerMode.valueOf(mPreferences.getString(
          PREFS_KEY_SERVER_MODE, ""));
      mUsername = mPreferences.getString(PREFS_KEY_USERNAME, "");
    } catch (IllegalArgumentException e) {
      mServerMode = null;
    }

    if (mServerMode == null || mUsername.length() == 0) {
      startActivity(new Intent(this, ServerModeChooserActivity.class));
    }

    mActionBar = getActionBar();
    mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    mActionBar.setTitle(getString(R.string.app_name));
    mActionBar.setSubtitle(mUsername);

    mActionBar.addTab(mActionBar.newTab().setText("Places")
        .setTabListener(new TabListener<PlacesFragment>(this, "places", PlacesFragment.class)));

    mActionBar.addTab(mActionBar.newTab().setText("Sensors")
        .setTabListener(new TabListener<SensorsFragment>(this, "sensors", SensorsFragment.class)));

    mActionBar.addTab(mActionBar.newTab().setText("Apps")
        .setTabListener(new TabListener<AppsFragment>(this, "apps", AppsFragment.class)));
  }

  @Override
  protected void onResume() {
    super.onResume();

    try {
      mServerMode = ServerModeChooserActivity.ServerMode.valueOf(mPreferences.getString(
          PREFS_KEY_SERVER_MODE, ""));
      mUsername = mPreferences.getString(PREFS_KEY_USERNAME, "");
    } catch (IllegalArgumentException e) {
      mServerMode = null;
    }

    if (mServerMode == null || mUsername.length() == 0) {
      startActivity(new Intent(this, ServerModeChooserActivity.class));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.platys_home, menu);
    return true;
  }

  public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
    private final Activity mActivity;
    private final String mTag;
    private final Class<T> mClass;
    private final Bundle mArgs;
    private Fragment mFragment;

    public TabListener(Activity activity, String tag, Class<T> clz) {
      this(activity, tag, clz, null);
    }

    public TabListener(Activity activity, String tag, Class<T> clz, Bundle args) {
      mActivity = activity;
      mTag = tag;
      mClass = clz;
      mArgs = args;

      mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
      if (mFragment != null && !mFragment.isDetached()) {
        FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
        ft.detach(mFragment);
        ft.commit();
      }
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
      if (mFragment == null) {
        mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
        ft.add(android.R.id.content, mFragment, mTag);
      } else {
        ft.attach(mFragment);
      }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
      if (mFragment != null) {
        ft.detach(mFragment);
      }
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
      Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show();
    }
  }
}
