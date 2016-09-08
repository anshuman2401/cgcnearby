package com.anshuman.cgcnearby;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Anshuman on 05-07-2016.
 */
public class MySingleton {

    private static MySingleton myInstance;
    private RequestQueue requestQueue;
    private static Context context;


    private MySingleton(Context c){

        context = c;

        requestQueue = getRequestQueue();

    }

    public RequestQueue getRequestQueue(){

        if(requestQueue==null){

            requestQueue = Volley.newRequestQueue(context.getApplicationContext());

        }

        return requestQueue;

    }

    public static synchronized MySingleton getInstance(Context context){

        if(myInstance == null){

            myInstance = new MySingleton(context);

        }

        return myInstance;

    }

    public<T> void addRequsetQueue(Request<T> request){

        requestQueue.add(request);

    }

}
