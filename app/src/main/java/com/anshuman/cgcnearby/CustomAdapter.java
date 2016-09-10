package com.anshuman.cgcnearby;

import android.content.Context;
import android.content.SharedPreferences;
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

    public CustomAdapter(Context context, ArrayList<LocationModel> locationList) {
        super(context, R.layout.custom_row, locationList);

        this.locationList = locationList;

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("currentLocation",Context.MODE_PRIVATE);
        currentLat= sharedPreferences.getString("latitude","");
        currentLon = sharedPreferences.getString("longitude","");

    }

    class ViewHolder{

        TextView locations,distance;

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

            row.setTag(holder);

        }else {

            holder = (ViewHolder)row.getTag();
        }

        LocationModel locationModel = locationList.get(position);

        holder.locations.setText(locationModel.getName());

        Double newLat = Double.valueOf(locationModel.getLatitude());

        Double newLon = Double.valueOf(locationModel.getLongitude());

        String dis = String.valueOf(distance(Double.parseDouble(currentLat),Double.parseDouble(currentLon),newLat,newLon));

        String subDis = dis.substring(0,5);

        holder.distance.setText(subDis+"Km");

        return row;
    }

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
