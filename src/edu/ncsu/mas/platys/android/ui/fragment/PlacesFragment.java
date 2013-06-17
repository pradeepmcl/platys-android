package edu.ncsu.mas.platys.android.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
    // case R.id.menu_places_fragment_add:
      // Handle fragment menu item
      // return true;
    default:
      // Not one of ours. Perform default menu processing
      return super.onOptionsItemSelected(item);
    }
  }

  private List<Model> getModel() {
    List<Model> list = new ArrayList<Model>();
    list.add(get("Linux"));
    list.add(get("Windows7"));
    list.add(get("Suse"));
    list.add(get("Eclipse"));
    list.add(get("Ubuntu"));
    list.add(get("Solaris"));
    list.add(get("Android"));
    list.add(get("iPhone"));
    // Initially select one of the items
    list.get(1).setSelected(true);
    return list;
  }

  private Model get(String s) {
    return new Model(s);
  }

}
