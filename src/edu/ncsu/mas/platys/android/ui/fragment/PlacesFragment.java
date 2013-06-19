package edu.ncsu.mas.platys.android.ui.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
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
import android.widget.TextView;
import android.widget.TimePicker;
import edu.ncsu.mas.platys.android.R;
import edu.ncsu.mas.platys.android.ui.adapter.Model;

public class PlacesFragment extends Fragment {

  private EditText placeIncludeEt;

  private EditText placeExcludeEt;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_places, container, false);

    // Time display
    Calendar cal = Calendar.getInstance();
    EditText timeEt = (EditText) view.findViewById(R.id.etTimePlacesFragment);
    timeEt.setText(formatTime(cal));

    // Time picker button
    ImageButton timePicker = (ImageButton) view.findViewById(R.id.ibTimePickPlacesFragment);
    timePicker.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        showTimePickerDialog(arg0);
      }
    });

    // Place include list
    placeIncludeEt = (EditText) view.findViewById(R.id.etIncludeListPlacesFragment);

    // Place exclude list
    placeExcludeEt = (EditText) view.findViewById(R.id.etExcludeListPlacesFragment);

    // Suggestions list
    // View listViewHeader = inflater.inflate(R.layout.header_place_suggestion, null);
    ListView lv = (ListView) view.findViewById(R.id.lvSuggestionsPlacesFragment);
    // lv.addHeaderView(listViewHeader);

    ArrayAdapter<Model> adapter = new PlaceSuggestionArrayAdapter(this.getActivity(), getModel());
    lv.setAdapter(adapter);
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
      clearPlaceLists();
      return true;
    case R.id.menuDiscardPlacesFragment:
      clearPlaceLists();
      return true;
    case R.id.menuUndoPlacesFragment:
      return true;
    default:
      // Not one of ours. Perform default menu processing
      return super.onOptionsItemSelected(item);
    }
  }

  public static class TimePickerFragment extends DialogFragment implements
  TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Calendar c = Calendar.getInstance();
      int hour = c.get(Calendar.HOUR_OF_DAY);
      int minute = c.get(Calendar.MINUTE);

      return new TimePickerDialog(getActivity(), this, hour, minute,
          DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
      final Calendar cal = Calendar.getInstance();
      cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
      cal.set(Calendar.MINUTE, minute);

      EditText timeEt = (EditText) getActivity().findViewById(R.id.etTimePlacesFragment);
      timeEt.setText(formatTime(cal));
      Animation anim = new AlphaAnimation(0.3f, 1.0f);
      anim.setDuration(1000);
      timeEt.startAnimation(anim);
    }
  }

  public void showTimePickerDialog(View v) {
    DialogFragment newFragment = new TimePickerFragment();
    newFragment.show(getFragmentManager(), "timePicker");
  }

  private List<Model> getModel() {
    List<Model> list = new ArrayList<Model>();
    list.add(get("Home"));
    list.add(get("Colleagues"));
    list.add(get("Jogging"));
    list.add(get("Happy"));
    list.add(get("Making slow progress at work"));
    list.add(get("Starbucks at mission valley"));
    // Initially select one of the items
    list.get(1).setSelected(true);
    return list;
  }

  private Model get(String s) {
    return new Model(s);
  }

  private void clearPlaceLists() {
    if (placeIncludeEt != null) {
      placeIncludeEt.setText("");
    }

    if (placeExcludeEt != null) {
      placeExcludeEt.setText("");
    }
  }

  private static String formatTime(Calendar cal) {
    return (cal.get(Calendar.HOUR) < 10 ? "0" + cal.get(Calendar.HOUR) : cal.get(Calendar.HOUR))
        + ":"
        + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE))
        + " "
        + (cal.get(Calendar.AM_PM) == 0 ? "AM" : "PM");
  }

  public static class PlaceSuggestionArrayAdapter extends ArrayAdapter<Model> {

    private final List<Model> list;
    private final Activity context;

    public PlaceSuggestionArrayAdapter(Activity context, List<Model> list) {
      super(context, R.layout.row_place_suggestion, list);
      this.context = context;
      this.list = list;
    }

    static class ViewHolder {
      protected TextView item;
      protected TextView subItem;
      protected ImageButton acceptButton;
      protected ImageButton rejectButton;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = null;
      if (convertView == null) {
        LayoutInflater inflator = context.getLayoutInflater();
        view = inflator.inflate(R.layout.row_place_suggestion, null);
        final ViewHolder viewHolder = new ViewHolder();

        viewHolder.item = (TextView) view.findViewById(R.id.tvTitlePlaceSuggestionRow);

        viewHolder.acceptButton = (ImageButton) view
            .findViewById(R.id.ibAcceptSuggestionPlaceSuggestionRow);
        viewHolder.acceptButton.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            Model element = (Model) viewHolder.acceptButton.getTag();
            element.setSelected(true);
            EditText placeIncludeEt = (EditText) context
                .findViewById(R.id.etIncludeListPlacesFragment);
            EditText placeExcludeEt = (EditText) context
                .findViewById(R.id.etExcludeListPlacesFragment);
            if (placeIncludeEt != null && placeExcludeEt != null) {
              String placeInclueList = placeIncludeEt.getText().toString();
              String placeExcludeList = placeExcludeEt.getText().toString();
              String place = element.getName();

              // Remove from exclude list if exists
              if (placeExcludeList.contains(place)) {
                if (placeExcludeList.contains(place + ", ")) {
                  placeExcludeList = placeExcludeList.replace(place + ", ", "");
                } else if (placeExcludeList.contains(", " + place)) {
                  placeExcludeList = placeExcludeList.replace(", " + place, "");
                } else {
                  placeExcludeList = placeExcludeList.replace(place, "");
                }
                placeExcludeEt.setText(placeExcludeList);
              }

              // Add to include list if doesn't exist
              if (!placeInclueList.contains(place)) {
                if (placeInclueList.length() == 0) {
                  placeIncludeEt.setText(element.getName());
                } else {
                  placeIncludeEt.setText(placeInclueList + ", " + element.getName());
                }
              }
            }
          }
        });

        viewHolder.rejectButton = (ImageButton) view
            .findViewById(R.id.ibCancelSuggestionPlaceSuggestionRow);
        viewHolder.rejectButton.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            Model element = (Model) viewHolder.acceptButton.getTag();
            element.setSelected(true);
            EditText placeIncludeEt = (EditText) context
                .findViewById(R.id.etIncludeListPlacesFragment);
            EditText placeExcludeEt = (EditText) context
                .findViewById(R.id.etExcludeListPlacesFragment);
            if (placeIncludeEt != null && placeExcludeEt != null) {
              String placeIncludeList = placeIncludeEt.getText().toString();
              String placeExcludeList = placeExcludeEt.getText().toString();
              String place = element.getName();

              // Remove from include list if exists
              if (placeIncludeList.contains(place)) {
                if (placeIncludeList.contains(place + ", ")) {
                  placeIncludeList = placeIncludeList.replace(place + ", ", "");
                } else if (placeIncludeList.contains(", " + place)) {
                  placeIncludeList = placeIncludeList.replace(", " + place, "");
                } else {
                  placeIncludeList = placeIncludeList.replace(place, "");
                }
                placeIncludeEt.setText(placeIncludeList);
              }

              // Add to exclude list if doesn't exist
              if (!placeExcludeList.contains(place)) {
                if (placeExcludeList.length() == 0) {
                  placeExcludeEt.setText(element.getName());
                } else {
                  placeExcludeEt.setText(placeExcludeList + ", " + element.getName());
                }
              }
            }
          }
        });

        view.setTag(viewHolder);
        viewHolder.acceptButton.setTag(list.get(position));

      } else {
        view = convertView;
        ((ViewHolder) view.getTag()).acceptButton.setTag(list.get(position));
      }
      ViewHolder holder = (ViewHolder) view.getTag();
      holder.item.setText(list.get(position).getName());
      // holder.checkbox.setChecked(list.get(position).isSelected());
      return view;
    }
  }
}
