package com.anshuman.cgcnearby;

import android.*;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Information extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbarLayout=null;
    String latitude,longitude,title;
    TextView infoText,infoText1;
    ImageView imageView;
    Bitmap decodedByte;
    final private static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    final private static String get_info = "http://www.anshumankaushik.in/cgcnearby/getinfo.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        imageView = (ImageView)findViewById(R.id.imageView);

        infoText = (TextView)findViewById(R.id.infoText);

        infoText1 = (TextView)findViewById(R.id.infoText1);

        Intent i = getIntent();
        latitude = i.getStringExtra("latitude");
        longitude = i.getStringExtra("longitude");
        title = i.getStringExtra("name");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(title);

        dynamicToolbarColor();
        toolbarTextAppernce();

        //when image is clicked check for permissions
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                permissionManager();


            }
        });

        //Getting info about marker
        getInfo();

    }

    private void dynamicToolbarColor() {

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.cgclogo);
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {

            @Override
            public void onGenerated(Palette palette) {
                collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary));
                collapsingToolbarLayout.setStatusBarScrimColor(getResources().getColor(R.color.colorPrimary));
            }
        });
    }

    private void toolbarTextAppernce() {
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedappbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expandedappbar);
    }

    public void getInfo(){

        final ProgressDialog dialog = ProgressDialog.show(Information.this,"","Getting details...",false,false);

        StringRequest request = new StringRequest(Request.Method.POST, get_info, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                dialog.dismiss();

                try {

                    JSONArray array = new JSONArray(response);

                    JSONObject object = array.getJSONObject(0);

                    String image = object.getString("image");

                    //converting base64 into image
                    byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                    decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    //showing image in imageview
                    imageView.setImageBitmap(decodedByte);

                    infoText1.setText("Additional Information:");

                    infoText.setText(object.getString("information"));

                } catch (JSONException e) {

                    e.printStackTrace();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                dialog.dismiss();

                Toast.makeText(Information.this, "Can't connect to Internet", Toast.LENGTH_SHORT).show();

            }
        }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String,String> params = new HashMap<String, String>();
                params.put("latitude",latitude);
                params.put("longitude",longitude);
                return params;
            }
        };

        MySingleton.getInstance(getApplicationContext()).addRequsetQueue(request);
    }

    public void showImage(){

        //making output stream for making folder etc
        OutputStream fOut = null;

        //uri will contain address
        Uri outputFileUri;
        try {
            //root will be equal to sdcard/cgcnearby
            File root = new File(Environment.getExternalStorageDirectory()
                    + File.separator + "CGCNearby" + File.separator);
            //make folder cgcnearby
            root.mkdirs();

            //make file image.jpg in root folder or sdcard/cgcnearby
            File sdImageMainDirectory = new File(root, "image.jpg");
            outputFileUri = Uri.fromFile(sdImageMainDirectory);
            fOut = new FileOutputStream(sdImageMainDirectory);

        } catch (Exception e) {
            Toast.makeText(Information.this, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
        }
        try {
            decodedByte.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
        }

        //showing image in gallery
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + "/sdcard/CGCNearby/image.jpg"), "image/*");
        startActivity(intent);

    }

    public void permissionManager() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(Information.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Information.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);


            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Information.this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(Information.this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

            }
        }else {

            //if permission is granted showImage
            showImage();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    showImage();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(Information.this, "For Viewing images Permission Required", Toast.LENGTH_SHORT).show();
                    permissionManager();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent myIntent = new Intent(getApplicationContext(), LocationsInListView.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}


