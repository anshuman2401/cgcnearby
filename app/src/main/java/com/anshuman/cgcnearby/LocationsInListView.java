package com.anshuman.cgcnearby;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LocationsInListView extends AppCompatActivity {

    ListView listView;
    private ArrayList<LocationModel> locationList;
    ArrayAdapter arrayAdapter;
    final private static String show_location = "http://www.anshumankaushik.in/cgcnearby/showLocationName.php";
    final private static String search_location = "http://www.anshumankaushik.in/cgcnearby/searchLocation.php";
    EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations_in_list_view);

        input = (EditText)findViewById(R.id.editText);

        listView = (ListView)findViewById(R.id.listview);

        locationList = new ArrayList<LocationModel>();

        arrayAdapter = new CustomAdapter(LocationsInListView.this,locationList);

        getLocationNames();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

               LocationModel locationModel = locationList.get(position);

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr="+locationModel.getLatitude()+","+locationModel.getLongitude()));
                startActivity(intent);

            }
        });

        input.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                 final ProgressDialog dialog = ProgressDialog.show(LocationsInListView.this,"","Searching...",false,false);
                StringRequest request = new StringRequest(Request.Method.POST, search_location, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dialog.dismiss();

                        try {

                            locationList.clear();

                            JSONArray array = new JSONArray(response);

                            for (int i=0;i<array.length();i++) {

                                JSONObject object = array.getJSONObject(i);

                                LocationModel locationModel = new LocationModel();

                                locationModel.setId(object.getString("id"));

                                locationModel.setName(object.getString("name"));

                                locationModel.setLatitude(object.getString("latitude"));

                                locationModel.setLongitude(object.getString("longitude"));

                                locationList.add(locationModel);
                            }

                            arrayAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {

                            e.printStackTrace();

                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        dialog.dismiss();
                        Toast.makeText(LocationsInListView.this, "Failed to retrive data", Toast.LENGTH_SHORT).show();

                    }
                }){

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {

                        Map<String,String> params = new HashMap<String, String>();
                        params.put("search_name",input.getText().toString());

                        return params;
                    }
                };

                MySingleton.getInstance(getApplicationContext()).addRequsetQueue(request);

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {


            }

            public void afterTextChanged(Editable s) {


            }
        });


    }

    //Getting all locatons names from database and showing them in a list using custom adapter
    public void getLocationNames(){

        final ProgressDialog dialog = ProgressDialog.show(LocationsInListView.this,"","Loading...",false,false);

        final StringRequest request = new StringRequest(Request.Method.GET, show_location, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                dialog.dismiss();

                try {

                    locationList.clear();

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
