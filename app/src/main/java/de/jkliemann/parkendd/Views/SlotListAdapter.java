package de.jkliemann.parkendd.Views;

import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.jkliemann.parkendd.Model.ParkingSpot;
import de.jkliemann.parkendd.ParkenDD;
import de.jkliemann.parkendd.R;
import de.jkliemann.parkendd.Utilities.Error;
import de.jkliemann.parkendd.Utilities.Util;


public class SlotListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final ParkingSpot[] spots;
    private final Location currentLocation;
    private static final String CLOSED = "closed";
    private static final String NODATA = "nodata";
    private static final Map<String, Integer> typeMap;
    static {
        Map<String, Integer> initMap = new HashMap<>();
        initMap.put("Tiefgarage", R.string.Tiefgarage);
        initMap.put("Parkplatz", R.string.Parkplatz);
        initMap.put("Parkhaus", R.string.Parkhaus);
        typeMap = Collections.unmodifiableMap(initMap);
    }

    public SlotListAdapter(Context context, ParkingSpot[] spots){
        this.context = context;
        this.spots = spots;
        currentLocation = ((ParkenDD) ((Activity)context).getApplication()).location();
    }

    @Override
    public int getGroupCount() {
        return spots.length;
    }

    @Override
    public int getChildrenCount(int i) {
        return 1;
    }

    @Override
    public Object getGroup(int i) {
        return null;
    }

    @Override
    public Object getChild(int i, int i1) {
        return null;
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public long getChildId(int i, int i1) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int position, boolean isExpanded, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View slotView = inflater.inflate(R.layout.slot_list_adapter, parent, false);
        TextView countView = (TextView)slotView.findViewById(R.id.countView);
        TextView percentView = (TextView)slotView.findViewById(R.id.percentView);
        TextView nameView = (TextView)slotView.findViewById(R.id.nameView);
        TextView distanceView = (TextView)slotView.findViewById(R.id.distanceView);
        ParkingSpot spot = spots[position];
        nameView.setText(spot.name());
        if(spot.state().equals(CLOSED)) {
            countView.setText(context.getString(R.string.closed));
            slotView.setBackgroundColor(context.getResources().getColor(R.color.parkingNoData));
        }else if(spot.state().equals(NODATA)){
            countView.setText(context.getString(R.string.nodata) + " (" + Integer.toString(spot.count()) + ")");
            slotView.setBackgroundColor(context.getResources().getColor(R.color.parkingNoData));
        }else{
            countView.setText(Integer.toString(spot.free()));
            float percentageFreePlaces = (float)spot.free() / (float)spot.count();
            int percentageFreePlacesFormatted = (int) (percentageFreePlaces * 100);
            percentView.setText(context.getResources().getString(R.string.free,percentageFreePlacesFormatted));


            ArgbEvaluator colorEvaluator = new ArgbEvaluator();

            slotView.setBackgroundColor((int)colorEvaluator.evaluate(percentageFreePlaces,
                    context.getResources().getColor(R.color.parkingFull),
                    context.getResources().getColor(R.color.parkingFree)));
        }
        if(currentLocation != null && spot.location() != null){
            distanceView.setText(Util.getViewDistance(Util.getDistance(currentLocation, spot.location())));
        }else {
            distanceView.setText("");
        }
        return slotView;
    }

    @Override
    public View getChildView(int groupId, int childId, boolean isLastChild, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        final View detailView = inflater.inflate(R.layout.list_detail, null);
        final ParkingSpot child = spots[groupId];
        TextView type = (TextView)detailView.findViewById(R.id.type);
        try{
            type.setText(context.getString(typeMap.get(child.type())));
        }catch (NullPointerException e){
            e.printStackTrace();
            type.setText(child.type());
        }
        TextView address = (TextView)detailView.findViewById(R.id.address);
        address.setText(child.address());
        TextView region = (TextView)detailView.findViewById(R.id.region);
        region.setText(child.category());
        ImageButton mapButton = (ImageButton)detailView.findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri geouri = child.geoUri();
                try{
                    Intent map = new Intent(Intent.ACTION_VIEW, geouri);
                    String city = ((ParkenDD) ((Activity)context).getApplication()).currentCity().name();
                    ((ParkenDD) ((Activity) context).getApplication()).getTracker().trackEvent(city, child.name());
                    context.startActivity(map);
                }catch (ActivityNotFoundException e){
                    e.printStackTrace();
                    Error.showLongErrorToast(context, context.getString(R.string.intent_error));
                }
            }
        });
        return detailView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public void onGroupExpanded(int position){
        super.onGroupExpanded(position);
    }
}