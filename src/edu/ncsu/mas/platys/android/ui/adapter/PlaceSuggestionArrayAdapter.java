package edu.ncsu.mas.platys.android.ui.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import edu.ncsu.mas.platys.android.R;
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
          
          EditText placeIncludeEt = (EditText) context
              .findViewById(R.id.etIncludeListPlacesFragment);
          EditText placeExcludeEt = (EditText) context
              .findViewById(R.id.etExcludeListPlacesFragment);
          if (placeIncludeEt != null && placeExcludeEt != null) {
            String placeInclueList = placeIncludeEt.getText().toString();
            String placeExcludeList = placeExcludeEt.getText().toString();
            String place = element.getLabel();

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
                placeIncludeEt.setText(element.getLabel());
              } else {
                placeIncludeEt.setText(placeInclueList + ", " + element.getLabel());
              }
            }
          }
        }
      });
      
      // TODO: The OnClickListener for the reject button duplicates code. There
      // is an opportunity to make it concise.

      viewHolder.rejectButton = (ImageButton) view
          .findViewById(R.id.ibCancelSuggestionPlaceSuggestionRow);
      viewHolder.rejectButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          PlaceLabelData element = (PlaceLabelData) viewHolder.acceptButton.getTag();
          element.setLabelType(LabelType.REJECTED_SUGGESTION);
          
          EditText placeIncludeEt = (EditText) context
              .findViewById(R.id.etIncludeListPlacesFragment);
          EditText placeExcludeEt = (EditText) context
              .findViewById(R.id.etExcludeListPlacesFragment);
          if (placeIncludeEt != null && placeExcludeEt != null) {
            String placeIncludeList = placeIncludeEt.getText().toString();
            String placeExcludeList = placeExcludeEt.getText().toString();
            String place = element.getLabel();

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
                placeExcludeEt.setText(element.getLabel());
              } else {
                placeExcludeEt.setText(placeExcludeList + ", " + element.getLabel());
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
    holder.item.setText(list.get(position).getLabel());
    // holder.checkbox.setChecked(list.get(position).isSelected());
    return view;
  }
  
}
