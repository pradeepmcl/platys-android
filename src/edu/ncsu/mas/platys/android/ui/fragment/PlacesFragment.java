package edu.ncsu.mas.platys.android.ui.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
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
import android.widget.TimePicker;
import edu.ncsu.mas.platys.android.R;
import edu.ncsu.mas.platys.android.ui.adapter.Model;
import edu.ncsu.mas.platys.android.ui.adapter.PlaceSuggestionArrayAdapter;

public class PlacesFragment extends Fragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_places, container, false);

    EditText timeEt = (EditText) view.findViewById(R.id.etTimePlacesFragment);
    timeEt.setText(Calendar.getInstance().getTime().toString());

    ImageButton timePicker = (ImageButton) view.findViewById(R.id.ibTimePickPlacesFragment);
    timePicker.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        showTimePickerDialog(arg0);
      }
    });

    View listViewHeader = inflater.inflate(R.layout.header_place_suggestion, null);
    ListView lv = (ListView) view.findViewById(R.id.lvSuggestionsPlacesFragment);
    lv.addHeaderView(listViewHeader);

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
      return true;
    case R.id.menuDiscardPlacesFragment:
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
      final Calendar c = Calendar.getInstance();
      c.set(Calendar.HOUR_OF_DAY, hourOfDay);
      c.set(Calendar.MINUTE, minute);
      // displayView.setText(c.getTime().toString());
      EditText timeEt = (EditText) getActivity().findViewById(R.id.etTimePlacesFragment);
      timeEt.setText(c.getTime().toString());
      Animation anim = new AlphaAnimation(0.3f, 1.0f);
      anim.setDuration(1000);
      timeEt.startAnimation(anim);
      Log.i("Pradeep", c.getTime().toString());
    }
  }

  public void showTimePickerDialog(View v) {
    DialogFragment newFragment = new TimePickerFragment();
    newFragment.show(getFragmentManager(), "timePicker");
  }

  private List<Model> getModel() {
    List<Model> list = new ArrayList<Model>();
    list.add(get("Home"));
    list.add(get("Lab"));
    list.add(get("Colleagues"));
    list.add(get("Dining"));
    list.add(get("Jogging"));
    list.add(get("Starbucks at mission valley"));
    // Initially select one of the items
    list.get(1).setSelected(true);
    return list;
  }

  private Model get(String s) {
    return new Model(s);
  }

}
