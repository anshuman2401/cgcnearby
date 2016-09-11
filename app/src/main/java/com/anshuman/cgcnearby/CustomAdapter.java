package com.anshuman.cgcnearby;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.anshuman.cgcnearby.LocationModel;
import com.anshuman.cgcnearby.MySingleton;
import com.anshuman.cgcnearby.R;
import com.google.android.gms.appdatasearch.DocumentId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anshuman on 05-09-2016.
 */
public class CustomAdapter extends ArrayAdapter {

    private ArrayList<LocationModel> locationList;
    private static String delete_location =  "http://www.anshumankaushik.in/cgcnearby/deletelocation.php";
    String currentLat,currentLon;
    View bottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    public CustomAdapter(Context context, ArrayList<LocationModel> locationList) {
        super(context, R.layout.custom_row, locationList);

        this.locationList = locationList;

        //Getting current postion for subtracting and calculate distance for every location
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("currentLocation",Context.MODE_PRIVATE);
        currentLat= sharedPreferences.getString("latitude","");
        currentLon = sharedPreferences.getString("longitude","");

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

            //Bottom sheet on which info will be displayed
            bottomSheet = row.findViewById(R.id.bottom_sheet);

            //Bottom sheet behavior is checking for bottomsheet
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

            //starting height for bottom sheet
            bottomSheetBehavior.setPeekHeight(0);

            //Starting state of bottom sheet
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        bottomSheetBehavior.setPeekHeight(0);
                        //if bottom sheet is swipped down then make its height to 0
                    }
                }
                @Override
                public void onSlide(View bottomSheet, float slideOffset) {
                }
            });

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

                //Showing bottom sheet when more info button is clicked
                BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheet();
                bottomSheetDialogFragment.show(((AppCompatActivity)getContext()).getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

                //sending location of marker to fragment
                Bundle bundle = new Bundle();
                bundle.putString("latitude", String.valueOf(locationModel.getLatitude()));
                bundle.putString("longitude", String.valueOf(locationModel.getLongitude()));
                bottomSheetDialogFragment.setArguments(bundle);

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
