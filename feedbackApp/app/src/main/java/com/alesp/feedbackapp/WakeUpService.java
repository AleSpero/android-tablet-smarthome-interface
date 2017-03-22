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
import org.json.JSONObject;

import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alesp on 20/03/2017.
 */

public class WakeUpService extends Service {

    //WakeUpService si occupa di chiamare, con un criterio non ancora deciso, l'attività QueryUser, per interrogarlo sull'attività
    //che sta facendo. Questo servizio girerà esclusivamente su IdleActivity.

    //UPDATE 22/03: WAkeupService viene integrato a connectionService: in questo modo esso gestisce anche la connessione con il server.

    //Array JSON contenente i valori
    JSONArray jsonArray;

    //intervallo tra un wakeup e l'altro
    long timeout = 20000;

    private ServiceConnectionListener listener = null;

    //Variabili utilizzate per la connessione TCP
    Client client;
    private String ip = "159.149.152.242";
    private int port = 1808;

    boolean connected = false;

    private final IBinder binder = new WakeUpBinder();

    @Override
    public void onCreate(){
        super.onCreate();

        //Eseguo connessione
        connect();
    }

    @Override
    public IBinder onBind(Intent intent){

       /* int i=0;

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
        }*/

        return binder;
    }

    public class WakeUpBinder extends Binder {

        WakeUpService getService() {
            //In questo metodo ritorno un'istanza di WakeUpService in modo che le activity possano chiamare
            //i relativi metodi

            return WakeUpService.this;
        }

    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("WakeUpService","Servizio Chiuso");
            client.disconnect();
    }

    //Definisco metodi custom che le activity chiameranno

    //Forse questo non serve
    public boolean isConnected(){
        return connected;
    }

    public String sendData(){
        return "da fare";
    }

    public boolean connect(){

        if(!connected) {

            //Effettuo connessione
            client = new Client(ip, port);

            //Setto callbacks
            client.setConnectionListener(new ConnectionListener() {
                @Override
                public void onMessage(String message) {
                    Log.v("WakeUpService", "Ricevuto: " + message);
                    connected = true;

                    //Se l'activity queryUser non è attiva, sveglio subito l'activity:
                    //altrimenti aspetto (?)

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    //Se l'activity QueryUser non è attiva, falla partire
                    if (!prefs.getBoolean("active", false)) {
                        //faccio partire activity QueryUser
                        Intent intent = new Intent("android.intent.category.LAUNCHER");

                        //Aggiungo all'intent, per ogni iterazione, ogni elemento del JSONarray
                        intent.putExtra("Data", message);

                        intent.setClassName("com.alesp.feedbackapp", "com.alesp.feedbackapp.QueryUser");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //getApplicationContext().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    }
                    else{
                        //E qui?
                    }


                }

                @Override
                public void onConnect(Socket socket) {
                    Log.v("WakeUpService", "Connected to the server");
                    connected = true;
                }

                @Override
                public void onDisconnect(Socket socket, String message) {
                    Log.v("WakeUpService", "Disconnected");
                    connected = false;
                }

                @Override
                public void onConnectError(Socket socket, String message) {
                    Log.e("WakeUpService", "Connection Error: " + message);
                    connected = false;

                }
            });

            //Connetto al Server
            client.connect();

            Log.v("Service, onCreate", "Connessione TCP iniziata");
        }

        return connected;
    }

    public void disconnect(){
        if(connected) {
            client.disconnect();
        }
    }




}
