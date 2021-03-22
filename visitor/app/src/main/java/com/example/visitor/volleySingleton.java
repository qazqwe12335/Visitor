package com.example.visitor;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class volleySingleton {
    private static volleySingleton minstance;
    private RequestQueue mrequestqueue;

    private volleySingleton(Context context){
        mrequestqueue = Volley.newRequestQueue(context.getApplicationContext());
    }
    public static  synchronized volleySingleton getInstance(Context context){
        if (minstance == null){
            minstance = new volleySingleton(context);
        }
        return minstance;
    }
    public RequestQueue getrequestqueue(){
        return mrequestqueue;
    }
}
