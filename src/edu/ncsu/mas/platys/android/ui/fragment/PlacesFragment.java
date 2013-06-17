package edu.ncsu.mas.platys.android.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import edu.ncsu.mas.platys.android.R;

public class PlacesFragment extends Fragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_places, container, false);
    ExpandableListView elv = (ExpandableListView) view
        .findViewById(R.id.fragmentPlacesSuggestionsExpList);
    elv.setAdapter(new SavedTabsListAdapter());
    return view;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.places_fragment, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.menu_places_fragment_add:
      // Handle fragment menu item
      return true;
    default:
      // Not one of ours. Perform default menu processing
      return super.onOptionsItemSelected(item);
    }
  }

  public class SavedTabsListAdapter extends BaseExpandableListAdapter {

    private final String[] groups = { "People Names", "Dog Names", "Cat Names", "Fish Names" };

    private final String[][] children = { { "Arnold", "Barry", "Chuck", "David" },
        { "Ace", "Bandit", "Cha-Cha", "Deuce" }, { "Fluffy", "Snuggles" }, { "Goldy", "Bubbles" } };

    @Override
    public int getGroupCount() {
      return groups.length;
    }

    @Override
    public int getChildrenCount(int i) {
      return children[i].length;
    }

    @Override
    public Object getGroup(int i) {
      return groups[i];
    }

    @Override
    public Object getChild(int i, int i1) {
      return children[i][i1];
    }

    @Override
    public long getGroupId(int i) {
      return i;
    }

    @Override
    public long getChildId(int i, int i1) {
      return i1;
    }

    @Override
    public boolean hasStableIds() {
      return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
      TextView textView = new TextView(PlacesFragment.this.getActivity());
      textView.setText(getGroup(i).toString());
      return textView;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
      TextView textView = new TextView(PlacesFragment.this.getActivity());
      textView.setText(getChild(i, i1).toString());
      return textView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
      return true;
    }

  }
}
