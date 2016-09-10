package com.anshuman.cgcnearby;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LocationsInListView extends AppCompatActivity {

    ListView listView;
    private ArrayList<LocationModel> locationList;
    ArrayAdapter arrayAdapter;
    String currentLat,currentLon;
    final private static String show_location = "http://www.anshumankaushik.in/cgcnearby/showLocationName.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations_in_list_view);

        listView = (ListView)findViewById(R.id.listview);

        locationList = new ArrayList<LocationModel>();

        arrayAdapter = new CustomAdapter(this,locationList);

        Intent i = getIntent();
        currentLat = i.getStringExtra("latitude");
        currentLon = i.getStringExtra("longitude");

        getLocationNames();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                LocationModel locationModel = locationList.get(position);

                Intent intent = new Intent(LocationsInListView.this,SingleLocationMapActivity.class);
                intent.putExtra("latitude",locationModel.getLatitude());
                intent.putExtra("longitude",locationModel.getLongitude());
                intent.putExtra("name",locationModel.getName());
                startActivity(intent);

            }
        });


    }

    public void getLocationNames(){

        final ProgressDialog dialog = ProgressDialog.show(LocationsInListView.this,"","Loading...",false,false);

        final StringRequest request = new StringRequest(Request.Method.GET, show_location, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                dialog.dismiss();

                try {

                    JSONArray array = new JSONArray(response);

                    for(int i=0; i<array.length();i++) {

                        JSONObject object = array.getJSONObject(i);

                        LocationModel locationModel = new LocationModel();

                        locationModel.setId(object.getString("id"));

                        locationModel.setName(object.getString("name"));

                        locationModel.setLatitude(object.getString("latitude"));

                        locationModel.setLongitude(object.getString("longitude"));

                        locationList.add(locationModel);
                    }

                    listView.setAdapter(arrayAdapter);

                } catch (JSONException e) {

                    Toast.makeText(getApplicationContext(),"Failed to retrieve data!",Toast.LENGTH_SHORT).show();

                    e.printStackTrace();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                dialog.dismiss();

                Toast.makeText(getApplicationContext(),"Can't to Connect Internet!",Toast.LENGTH_SHORT).show();

            }
        });

        MySingleton.getInstance(getApplicationContext()).addRequsetQueue(request);

    }

}
