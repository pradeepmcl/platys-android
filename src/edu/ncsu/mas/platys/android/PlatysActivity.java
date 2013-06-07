package edu.ncsu.mas.platys.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.dropbox.sync.android.DbxAccountManager;


public class PlatysActivity extends Activity {

  private static final int REQUEST_LINK_TO_DBX = 0;

  private static final String mDbxAppKey = "x6cj580qfc2zjxu";
  private static final String mDbxAppSecret = "5bpxqwkyym3zwol";

  private DbxAccountManager mDbxAcctMgr;
  private Button mLinkToDropboxButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_platys);
    // startService(new Intent(this, PlatysService.class));
    Intent platysIntent = new Intent(this, PlatysReceiver.class);
    platysIntent.setAction("platys.intent.action.SENSE_ALL");
    sendBroadcast(platysIntent);

    mLinkToDropboxButton = (Button) findViewById(R.id.linkToDbxButton);
    mLinkToDropboxButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        onClickLinkToDropbox();
      }
    });

    mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), mDbxAppKey, mDbxAppSecret);
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (mDbxAcctMgr.hasLinkedAccount()) {
      mLinkToDropboxButton.setVisibility(View.GONE);
    } else {
      mLinkToDropboxButton.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.home, menu);
    return true;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_LINK_TO_DBX) {
      if (resultCode == Activity.RESULT_OK) {
        Log.i("Pradeep", "Linked to Dropbox");
      } else {
        Log.i("Pradeep", "Link to Dropbox failed or was cancelled.");
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  private void onClickLinkToDropbox() {
    mDbxAcctMgr.startLink(this, REQUEST_LINK_TO_DBX);
  }

}
