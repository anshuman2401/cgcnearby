package com.anshuman.cgcnearby;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Anshuman on 05-09-2016.
 */
public class CustomAdapter extends ArrayAdapter {

    private ArrayList<LocationModel> locationList;
    String currentLat,currentLon;
    private Context context;
    private LayoutInflater mInflater;

    public CustomAdapter(Context context, ArrayList<LocationModel> locationList) {
        super(context, R.layout.custom_row, locationList);

        this.locationList = locationList;

        //Getting current postion for subtracting and calculate distance for every location
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("currentLocation",Context.MODE_PRIVATE);
        currentLat= sharedPreferences.getString("latitude","");
        currentLon = sharedPreferences.getString("longitude","");
        this.context = context;
        mInflater = LayoutInflater.from(context);

    }

    class ViewHolder{

        TextView locations,distance;
        ImageView moreInfo;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;

        final ViewHolder holder;

        if(row==null) {

            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            row = inflater.inflate(R.layout.custom_row,null);

            holder = new ViewHolder();

            holder.locations = (TextView)row.findViewById(R.id.locationTextView);

            holder.distance = (TextView) row.findViewById(R.id.distanceTextView);

            holder.moreInfo = (ImageView)row.findViewById(R.id.moreInfo);

            row.setTag(holder);

        }else {

            holder = (ViewHolder)row.getTag();
        }

        final LocationModel locationModel = locationList.get(position);

        //Setting name
        holder.locations.setText(locationModel.getName());

        //latitude of locations saved in database
        Double newLat = Double.valueOf(locationModel.getLatitude());

        //longitude.....
        Double newLon = Double.valueOf(locationModel.getLongitude());

        //calculating distance
        String dis = String.valueOf(distance(Double.parseDouble(currentLat),Double.parseDouble(currentLon),newLat,newLon));

        //showing only first 5 numbers
        String subDis = dis.substring(0,5);

        //setting distance text
        holder.distance.setText(subDis+"Km");

        holder.moreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(context,Information.class);
                i.putExtra("latitude",locationModel.getLatitude());
                i.putExtra("longitude",locationModel.getLongitude());
                i.putExtra("name",locationModel.getName());
                context.startActivity(i);
            }
        });

        return row;
    }

    //Calculation or algo for distance measurements
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

}


