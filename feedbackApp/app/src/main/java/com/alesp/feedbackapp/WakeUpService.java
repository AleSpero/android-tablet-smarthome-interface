package com.alesp.feedbackapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alesp on 20/03/2017.
 */

public class WakeUpService extends Service {

    //WakeUpService si occupa di chiamare, con un criterio non ancora deciso, l'attività QueryUser, per interrogarlo sull'attività
    //che sta facendo. Questo servizio girerà esclusivamente su IdleActivity.

    //Array JSON contenente i valori
    JSONArray jsonArray;

    //intervallo tra un wakeup e l'altro
    long timeout = 20000;

    //Definisco il task che viene fatto ad intervalli di tempo regolari
    TimerTask wakeUpTask = new TimerTask() {

        private int i=0;

        @Override
        public void run(){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            //Se l'activity QueryUser non è attiva, falla partire
            if (!prefs.getBoolean("active", false) && i<jsonArray.length()) {


                //faccio partire activity QueryUser
                Intent intent = new Intent("android.intent.category.LAUNCHER");

                try {
                    //Aggiungo all'intent, per ogni iterazione, ogni elemento del JSONarray
                    intent.putExtra("Data",jsonArray.getJSONObject(i).toString());
                }
                catch(Exception e){
                    e.toString();
                }

                intent.setClassName("com.alesp.feedbackapp", "com.alesp.feedbackapp.QueryUser");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //getApplicationContext().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

                i++;
            }

        }

    };


    private final IBinder binder = new WakeUpBinder();

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent){

        int i=0;

        //Prendo l'array JSON da IdleActivity
        try {
            jsonArray = new JSONArray(intent.getExtras().getString("receivedData"));

            //In questa parte di codice, ogni minuto apro l'activity per chiedere all'utente l'attività che sta eseguendo.
            //se l'activity queryuser è attiva, allora "salto" e attendo un altro minuto, e così via. continuerò così finchè
            //non avrò chiesto il feedback per tutte le attività presenti nel JSONArray.


            //Il timer ripeterà una data operazione ogni tot tempo
            new Timer().schedule(wakeUpTask,timeout,timeout);

            } catch (JSONException e){
            Log.e("onBind WakeupServ",e.toString());
        }

        return binder;
    }

    public class WakeUpBinder extends Binder {

        WakeUpService getService() {
            //In questo metodo ritorno un'istanza di Connectionservice in modo che le activity possano chiamare
            //i relativi metodi

            return WakeUpService.this;
        }

    }


    @Override
    public void onDestroy(){
        Log.d("WakeUpService","Servizio Chiuso");

        //fermo il timerTask
        wakeUpTask.cancel();
    }

}
