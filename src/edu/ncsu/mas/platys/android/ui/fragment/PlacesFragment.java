package edu.ncsu.mas.platys.android.ui.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import edu.ncsu.mas.platys.android.PlatysReceiver;
import edu.ncsu.mas.platys.android.R;
import edu.ncsu.mas.platys.android.ui.adapter.PlaceSuggestionArrayAdapter;
import edu.ncsu.mas.platys.common.sensordata.PlaceLabelData;
import edu.ncsu.mas.platys.common.sensordata.PlaceLabelData.LabelType;

public class PlacesFragment extends Fragment {

  private EditText timeEt = null;
  private EditText placeIncludeEt = null;
  private EditText placeExcludeEt = null;

  private long labelingTime;
  private final List<PlaceLabelData> placeLabelList = new ArrayList<PlaceLabelData>();
  private ArrayAdapter<PlaceLabelData> placeLabelListAdapter = null;

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

    placeLabelList.clear();
    placeLabelList.addAll(getPlaceLabelSuggestions());
    placeLabelListAdapter = new PlaceSuggestionArrayAdapter(getActivity(), placeLabelList);

    // Place suggestion list
    ListView placeSuggestionLv = (ListView) view.findViewById(R.id.lvSuggestionsPlacesFragment);
    placeSuggestionLv.setAdapter(placeLabelListAdapter);

    // New label button
    ImageButton newLabelButton = (ImageButton) view.findViewById(R.id.ibAddPlacePlacesFragment);
    newLabelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        showPlaceInputAlertDialog();
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
      initiateSensing();
      saveData();
      initiateSyncing();
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

  public void onSuggestion(PlaceLabelData labelData) {
    EditText addToEt;
    EditText removeFromEt;

    switch (labelData.getLabelType()) {
    case ACCEPTED_SUGGESTION:
      addToEt = placeIncludeEt;
      removeFromEt = placeExcludeEt;
      break;
    case NEW_LABEL:
      addToEt = placeIncludeEt;
      removeFromEt = placeExcludeEt;
      break;
    case REJECTED_SUGGESTION:
      addToEt = placeExcludeEt;
      removeFromEt = placeIncludeEt;
      break;
    default:
      return;
    }

    String addToListList = addToEt.getText().toString();
    String removeFromList = removeFromEt.getText().toString();
    String curLabel = labelData.getLabel();

    if (removeFromList.contains(curLabel)) {
      if (removeFromList.contains(curLabel + ", ")) {
        removeFromList = removeFromList.replace(curLabel + ", ", "");
      } else if (removeFromList.contains(", " + curLabel)) {
        removeFromList = removeFromList.replace(", " + curLabel, "");
      } else {
        removeFromList = removeFromList.replace(curLabel, "");
      }
      removeFromEt.setText(removeFromList);
    }

    if (!addToListList.contains(curLabel)) {
      if (addToListList.length() == 0) {
        addToEt.setText(curLabel);
      } else {
        addToEt.setText(addToListList + ", " + curLabel);
      }
      placeLabelListAdapter.notifyDataSetChanged();
    }
  }

  private void showPlaceInputAlertDialog() {
    LayoutInflater inflater = LayoutInflater.from(getActivity());
    final View inputView = inflater.inflate(R.layout.view_add_place_alert, null);
    final AlertDialog.Builder addPlaceAlert = new AlertDialog.Builder(getActivity())
        .setTitle(R.string.PlacesFragment_add_place_alert_title)
        .setMessage(R.string.PlacesFragment_add_place_alert_message).setView(inputView)
        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int whichButton) {
            AutoCompleteTextView inputAtv = (AutoCompleteTextView) inputView
                .findViewById(R.id.atvAddPlaceAlert);
            String input = inputAtv.getText().toString().trim();
            if (input != null && input.length() != 0) {
              PlaceLabelData labelData = getPlaceLabelData(input);
              if (labelData.getLabelType() == LabelType.NEW_LABEL) {
                placeLabelList.add(labelData);
              }

              onSuggestion(labelData);
            }
          }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int whichButton) {
            // Do nothing.
          }
        });

    addPlaceAlert.show();
  }

  private PlaceLabelData getPlaceLabelData(String newLabel) {
    PlaceLabelData newPlaceLabelData;
    for (PlaceLabelData labelData : placeLabelList) {
      if (labelData.getLabel().equalsIgnoreCase(newLabel)) {
        labelData.setLabelType(LabelType.ACCEPTED_SUGGESTION);
        return labelData;
      }
    }

    newPlaceLabelData = new PlaceLabelData();
    newPlaceLabelData.setLabel(newLabel);
    newPlaceLabelData.setLabelType(LabelType.NEW_LABEL);
    return newPlaceLabelData;
  }

  private List<PlaceLabelData> getPlaceLabelSuggestions() {
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

    for (PlaceLabelData label : placeLabelList) {
      label.setLabelType(LabelType.IGNORED_SUGGESTION);
    }
    placeLabelListAdapter.notifyDataSetChanged();
  }

  private void saveData() {
    long curTime = System.currentTimeMillis();
    ArrayList<String> labelList = new ArrayList<String>();
    ArrayList<LabelType> labelTypeList = new ArrayList<LabelType>();

    for (PlaceLabelData label : placeLabelList) {
      labelList.add(label.getLabel());
      labelTypeList.add(label.getLabelType());
    }

    Intent platysSaveLabelsIntent = new Intent(getActivity(), PlatysReceiver.class);
    platysSaveLabelsIntent.setAction(PlatysReceiver.ACTION_ONE_TIME);

    platysSaveLabelsIntent.putExtra(PlatysReceiver.EXTRA_TASK,
        PlatysReceiver.PlatysTask.PLATYS_TASK_SAVE_LABELS);
    platysSaveLabelsIntent.putExtra(PlatysReceiver.EXTRA_LABELING_START_TIME, curTime);
    platysSaveLabelsIntent.putExtra(PlatysReceiver.EXTRA_LABELING_END_TIME, labelingTime);
    platysSaveLabelsIntent.putExtra(PlatysReceiver.EXTRA_LABELS_LIST, labelList);
    platysSaveLabelsIntent.putExtra(PlatysReceiver.EXTRA_LABEL_TYPES_LIST, labelTypeList);

    getActivity().sendBroadcast(platysSaveLabelsIntent);
  }

  private void initiateSensing() {
    Intent platysSenseIntent = new Intent(getActivity(), PlatysReceiver.class);
    platysSenseIntent.setAction(PlatysReceiver.ACTION_ONE_TIME);

    platysSenseIntent.putExtra(PlatysReceiver.EXTRA_TASK,
        PlatysReceiver.PlatysTask.PLATYS_TASK_SENSE);

    getActivity().sendBroadcast(platysSenseIntent);
  }

  private void initiateSyncing() {
    Intent platysSenseIntent = new Intent(getActivity(), PlatysReceiver.class);
    platysSenseIntent.setAction(PlatysReceiver.ACTION_ONE_TIME);

    platysSenseIntent.putExtra(PlatysReceiver.EXTRA_TASK,
        PlatysReceiver.PlatysTask.PLATYS_TASK_SYNC);

    getActivity().sendBroadcast(platysSenseIntent);
  }

  private static String getFormattedTime(Calendar cal) {
    return (cal.get(Calendar.HOUR) < 10 ? "0" + cal.get(Calendar.HOUR) : cal.get(Calendar.HOUR))
        + ":"
        + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal
            .get(Calendar.MINUTE)) + " " + (cal.get(Calendar.AM_PM) == 0 ? "AM" : "PM");
  }

}
