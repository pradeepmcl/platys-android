package edu.ncsu.mas.platys.android.ui.adapter;

import java.util.List;

import edu.ncsu.mas.platys.android.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class PlaceSuggestionArrayAdapter extends ArrayAdapter<Model> {

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
