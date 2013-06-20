package edu.ncsu.mas.platys.android.ui.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import edu.ncsu.mas.platys.android.R;
import edu.ncsu.mas.platys.android.ui.fragment.PlacesFragment.SuggestionClickListener;
import edu.ncsu.mas.platys.common.sensordata.PlaceLabelData;
import edu.ncsu.mas.platys.common.sensordata.PlaceLabelData.LabelType;

public class PlaceSuggestionArrayAdapter extends ArrayAdapter<PlaceLabelData> {

  private final List<PlaceLabelData> list;
  private final Activity context;

  public PlaceSuggestionArrayAdapter(Activity context, List<PlaceLabelData> list) {
    super(context, R.layout.row_place_suggestion, list);
    this.context = context;
    this.list = list;
  }

  static class ViewHolder {
    protected TextView item;
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
          PlaceLabelData element = (PlaceLabelData) viewHolder.acceptButton.getTag();
          element.setLabelType(LabelType.ACCEPTED_SUGGESTION);
          ((SuggestionClickListener) context).onSuggestionClick(element);
        }
      });

      viewHolder.rejectButton = (ImageButton) view
          .findViewById(R.id.ibCancelSuggestionPlaceSuggestionRow);
      viewHolder.rejectButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          PlaceLabelData element = (PlaceLabelData) viewHolder.acceptButton.getTag();
          element.setLabelType(LabelType.REJECTED_SUGGESTION);
          ((SuggestionClickListener) context).onSuggestionClick(element);
        }
      });

      view.setTag(viewHolder);
      viewHolder.acceptButton.setTag(list.get(position));

    } else {
      view = convertView;
      ((ViewHolder) view.getTag()).acceptButton.setTag(list.get(position));
    }

    ViewHolder holder = (ViewHolder) view.getTag();
    holder.item.setText(list.get(position).getLabel());

    return view;
  }

}
