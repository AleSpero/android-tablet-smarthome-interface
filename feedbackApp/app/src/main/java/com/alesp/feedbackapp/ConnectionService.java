package com.alesp.feedbackapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by alesp on 10/03/2017.
 */

public class ConnectionService extends Service {

    //ConnectionService è il servizio adibito alla connessione TCP con il raspberry.
    //Esso si occuperà di inviare e ricevere dati dal raspberry. le varie activity usufruiranno
    //di questo servizio.


    //Creo il binder, che mi permetterà di gestire la comunicazione con le activity
    private final IBinder binder = new ConnectionBinder();

    Client client;
    private String ip = "159.149.152.242";
    private int port = 1808;

    boolean connected = false;

    //Creo arraylist che conterrà i dati ricevuti dal raspberry
    ArrayList<String> data = new ArrayList<String>();
    JSONArray jsonValues = new JSONArray();


    @Override
    public void onCreate(){
        super.onCreate();

       //Eseguo connessione
        connect();

    }

    @Override
    public IBinder onBind(Intent intent){
        return binder;
    }


    //Definisco metodi custom che le activity chiameranno

    //Forse questo non serve
    public boolean isConnected(){
        return connected;
    }

    public String getData(){

        //Questo metodo è da riguardare: per ora, dato un arraylist che funziona a mo
        //di buffer, restituisce il primo elemento all'activity, che verrà poi cancellato.
        //(è importante cancellarlo? vedi)

        if(jsonValues.length()!=0){
            return jsonValues.toString();
        }
        else{
            return null;
        }
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
                    Log.v("ConnectionService", "Ricevuto: " + message);

                    try {
                        jsonValues.put(new JSONObject(message));
                    }
                    catch(JSONException e){
                        Log.e("onMessage",e.toString());
                    }
                   // data.add(message);
                }

                @Override
                public void onConnect(Socket socket) {
                    Log.v("ConnectionService", "Connected");
                    connected = true;
                }

                @Override
                public void onDisconnect(Socket socket, String message) {
                    Log.v("ConnectionService", "Disconnected");
                    connected = false;
                }

                @Override
                public void onConnectError(Socket socket, String message) {
                    Log.e("ConnectionService", "Connection Error: " + message);
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

    @Override
    public void onDestroy(){
        Log.d("ConnectionService","Servizio Chiuso");
    }




    //Creo classe utilizzata per il binder. tramite questa classe potrò "interfacciarmi" tra il service
    //e una o più activity. questa classe conntiene solo un metodo, Getservice, che ritorna un'istanza di
    //ConnectionService in modo che essa possa essere utilizzata da una activity.
    public class ConnectionBinder extends Binder {

        ConnectionService getService() {
            //In questo metodo ritorno un'istanza di Connectionservice in modo che le activity possano chiamare
            //i relativi metodi

            return ConnectionService.this;
        }

    }

    }



