
package app.ride.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import app.ride.Model.Location;
import app.ride.R;
import app.ride.SelectLocation;

public class LocationsAdapter extends BaseAdapter {

    // Declare Variables

    Context mContext;
    LayoutInflater inflater;
    private ArrayList<Location> arraylist;

    public LocationsAdapter(Context context ) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<Location>();
        this.arraylist.addAll(SelectLocation.locList);
    }

    public class ViewHolder {
        TextView name;
    }

    @Override
    public int getCount() {
        return SelectLocation.locList.size();
    }

    @Override
    public Location getItem(int position) {
        return SelectLocation.locList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.location_list_item_layout, null);
            // Locate the TextViews in listview_item.xml
            holder.name = (TextView) view.findViewById(R.id.locationNametxt);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(SelectLocation.locList.get(position).getLocation_name());
        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        SelectLocation.locList.clear();
        if (charText.length() == 0) {
            SelectLocation.locList.addAll(arraylist);
        } else {
            for (Location wp : arraylist) {
                if (wp.getLocation_name().toLowerCase(Locale.getDefault()).contains(charText)) {
                    SelectLocation.locList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

}