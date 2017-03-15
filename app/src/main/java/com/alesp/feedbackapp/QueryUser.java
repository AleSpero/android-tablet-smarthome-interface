package com.alesp.feedbackapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by alesp on 14/03/2017.
 */

public class QueryUser extends Activity {

    //Definisco il mio service e il boolean boundtoactivity per indicare se il processo
    // è collegato all'activity
    ConnectionService connService;
    boolean boundToActivity = false;

    private ServiceConnection servConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v("onServiceConnected","Service connesso!");

            //Setto il flag boundtoprocess = true
            boundToActivity = true;

            //Effettuo il collegamento (giusto?)
            ConnectionService.ConnectionBinder binder = (ConnectionService.ConnectionBinder) service;
            connService = binder.getService();

            listener.onConnected();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v("onServiceDisonnected","Service disconnesso!");
            boundToActivity = false;
        }
    };
    private ServiceConnectionListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);


    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }
}
