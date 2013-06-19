package edu.ncsu.mas.platys.android.ui.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import edu.ncsu.mas.platys.android.R;
import edu.ncsu.mas.platys.android.ui.adapter.PlaceSuggestionArrayAdapter;
import edu.ncsu.mas.platys.android.ui.fragment.TimePickerFragment.OnTimeSetPlatysListener;
import edu.ncsu.mas.platys.common.sensordata.PlaceLabelData;
import edu.ncsu.mas.platys.common.sensordata.PlaceLabelData.LabelType;

public class PlacesFragment extends Fragment implements OnTimeSetPlatysListener {

  private EditText placeIncludeEt = null;
  private EditText placeExcludeEt = null;
  private EditText timeEt = null;
  
  private long labelingTime;
  private final List<PlaceLabelData> labelList = new ArrayList<PlaceLabelData>();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_places, container, false);
    
    Calendar cal = Calendar.getInstance();
    labelingTime = cal.getTimeInMillis();
    
    // Time display
    timeEt = (EditText) view.findViewById(R.id.etTimePlacesFragment);
    timeEt.setText(getFormattedTime(cal));
     

    // Time picker button
    ImageButton timePicker = (ImageButton) view.findViewById(R.id.ibTimePickPlacesFragment);
    timePicker.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        new TimePickerFragment().show(getFragmentManager(), "timePicker");
      }
    });

    // Place include list
    placeIncludeEt = (EditText) view.findViewById(R.id.etIncludeListPlacesFragment);

    // Place exclude list
    placeExcludeEt = (EditText) view.findViewById(R.id.etExcludeListPlacesFragment);

    labelList.addAll(getPlaceLabels());
    ArrayAdapter<PlaceLabelData> adapter = new PlaceSuggestionArrayAdapter(getActivity(), labelList);
    
    // Place suggestion list
    ListView lv = (ListView) view.findViewById(R.id.lvSuggestionsPlacesFragment);
    lv.setAdapter(adapter);

    // New label button
    ImageButton newLabelButton = (ImageButton) view.findViewById(R.id.ibAddPlacePlacesFragment);
    newLabelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        final EditText etAddLabel = new EditText(getActivity());
        final AlertDialog.Builder addPlaceAlert = new AlertDialog.Builder(getActivity())
            .setTitle("Enter a Place Label")
            .setMessage("A place cane be space, activity, or social circle.")
            .setView(etAddLabel)
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
                Log.i("Pradeep", etAddLabel.getText().toString());
              }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
              }
            });
        
        addPlaceAlert.show();
      }
    });

    return view;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.places_fragment, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.menuSavePlacesFragment:
      saveData();
      discardAllData();
      return true;
    case R.id.menuDiscardPlacesFragment:
      discardAllData();
      return true;
    case R.id.menuUndoPlacesFragment:
      return true;
    default:
      // Not one of ours. Perform default menu processing
      return super.onOptionsItemSelected(item);
    }
  }
  
  @Override
  public void onTimeSet(int hourOfDay, int minute) {
    if (timeEt != null) {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
      cal.set(Calendar.MINUTE, minute);
      cal.set(Calendar.SECOND, 0);
      
      labelingTime = cal.getTimeInMillis();
      
      timeEt.setText(getFormattedTime(cal));
      Animation anim = new AlphaAnimation(1.0f, 0.0f);
      anim.setDuration(1000);
      timeEt.startAnimation(anim);
    }
  }

  private List<PlaceLabelData> getPlaceLabels() {
    List<PlaceLabelData> list = new ArrayList<PlaceLabelData>();
    list.add(get("Home"));
    list.add(get("Colleagues"));
    list.add(get("Jogging"));
    list.add(get("Happy"));
    list.add(get("Making slow progress at work"));
    list.add(get("Starbucks at mission valley"));
    
    return list;
  }

  private PlaceLabelData get(String s) {
    PlaceLabelData labelData = new PlaceLabelData();
    labelData.setLabel(s);
    labelData.setLabelType(LabelType.IGNORED_SUGGESTION);
    return labelData;
  }

  private void discardAllData() {
    if (placeIncludeEt != null) {
      placeIncludeEt.setText("");
    }

    if (placeExcludeEt != null) {
      placeExcludeEt.setText("");
    }
    
    if (timeEt != null) {
      Calendar cal = Calendar.getInstance();
      labelingTime = cal.getTimeInMillis();
      timeEt.setText(getFormattedTime(cal));
    }
  }
  
  private void saveData() {
    long curTime = System.currentTimeMillis();
    for (PlaceLabelData label : labelList) {
      label.setSensingStartTime(labelingTime);
      label.setSensingEndTime(curTime);
      Log.i("Pradeep", label.getLabel() + ", " + label.getLabelType().toString()); 
    }
  }

  private static String getFormattedTime(Calendar cal) {
    return (cal.get(Calendar.HOUR) < 10 ? "0" + cal.get(Calendar.HOUR) : cal.get(Calendar.HOUR))
        + ":"
        + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal
            .get(Calendar.MINUTE)) + " " + (cal.get(Calendar.AM_PM) == 0 ? "AM" : "PM");
  }
  
}
