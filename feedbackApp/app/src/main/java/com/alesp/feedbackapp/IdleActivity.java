package com.alesp.feedbackapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by alesp on 20/03/2017.
 */

public class IdleActivity extends Activity {

    //Inizializzo variabili per l'update della UI
    ImageButton startButton;
    ImageButton settings;
    TextView serviceStatus;

    //ArrayList che contiene i dati scaricati
    String data;

    //Definisco il mio service e il boolean boundtoactivity per indicare se il processo
    // è collegato all'activity
    private WakeUpService wakeService;

    private boolean wakeupBoundToActivity = false;

    //variabili per notifiche
    NotificationManager notificationmanager;
    Notification notifica;
    int NOTIFICATION_SERVICE_RUNNING_ID = 0;

    private ServiceConnection wakeupConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.v("onServiceConnected", "Service WakeUpService connesso!");

                    //creo notifica permanente
                    showNotification();

                    //Setto il flag boundtoprocess = true
                    wakeupBoundToActivity = true;

                    //Effettuo il collegamento (giusto?)
                    WakeUpService.WakeUpBinder binder = (WakeUpService.WakeUpBinder) service;
                    wakeService = binder.getService();

                    //Controllo che il service sia connesso all'activity, e una volta fatto ciò connetto e ricevo i dati
                    if (wakeupBoundToActivity) {

                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {


                                if (wakeService.isConnected()) {

                                    //Aggiorno icona del bottone
                                    startButton.setImageResource(R.drawable.ic_pause_button);
                                    serviceStatus.setText("Servizio attivo");

                                } else {
                                    //creo alertdialog e faccio terminare il servizio
                                    new AlertDialog.Builder(IdleActivity.this)
                                            .setTitle("Connessione non riuscita")
                                            .setMessage("Impossibile connettersi al server.\n Riprova in un altro momento.")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    disconnectService();
                                                }
                                            })
                                            .show();


                                }
                            }
                        }, 500);

                    }

                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.v("onServiceDisonnected","Service WakeUpService disconnesso!");
                    wakeupBoundToActivity = false;
                }
            };

    //Definisco listener per la connessione
    private ServiceConnectionListener listener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.idle_activity);

        //Faccio collegamenti
        startButton = (ImageButton) findViewById(R.id.start_service);
        settings = (ImageButton) findViewById(R.id.settings);
        serviceStatus = (TextView) findViewById(R.id.service_running);

        startButton.setImageResource(R.drawable.ic_play_button_sing);
        settings.setImageResource(R.drawable.ic_settings);


        //Setto listener per far partire/fermare il servizio
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!(wakeupBoundToActivity)){
                    //Faccio partire il service
                    Intent servIntent = new Intent(IdleActivity.this, WakeUpService.class);
                    bindService(servIntent,wakeupConnection, Context.BIND_AUTO_CREATE);
                    wakeupBoundToActivity = true;
                }
                else{
                   disconnectService();
                }


            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();

        /*//riprendo wakeupservice, se c'era già
        if(!wakeupBoundToActivity){
            //Faccio partire Wake Up Service
            Intent wakeupIntent = new Intent(IdleActivity.this, WakeUpService.class);

            //Aggiungo dati da passare a WakeUpService
            wakeupIntent.putExtra("receivedData",data);

            bindService(wakeupIntent,wakeupConnection, Context.BIND_AUTO_CREATE);
            wakeupBoundToActivity = true;
        }*/
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(wakeupBoundToActivity){
            unbindService(wakeupConnection);
            wakeupBoundToActivity=false;
            notificationmanager.cancel(NOTIFICATION_SERVICE_RUNNING_ID);
        }
    }

    //Gestisco pressione del bottone, per evitare che involontariamente venga chiusa l'app

    @Override
    public void onBackPressed(){

        new AlertDialog.Builder(IdleActivity.this)
                .setTitle("Chiusura app")
                .setMessage("Sei sicuro di voler uscire dall'app?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //chiudo activity
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    } //forse devi toglierlo

    private void showNotification(){
        //creo notifica che rimarrà finchè sarà attivo il service

        /* Qui vi sono le istruzioni per fare in modo che cliccando sulla notifica si riapre l'activity*/
        Intent notificationIntent = new Intent(getApplicationContext(), IdleActivity.class);

        notificationIntent.setAction("android.intent.action.MAIN");
        notificationIntent.addCategory("android.intent.category.LAUNCHER");

        PendingIntent pendingIntent = PendingIntent.getActivity(IdleActivity.this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Qui inizia la costruzionee della notifica vera e propria

        notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifica  = new Notification.Builder(this)
                .setContentTitle("Servizio Attivo")
                .setContentText("Il servizio per il riconoscimento delle attività è attivo.")
                .setSmallIcon(R.drawable.ic_play_button_sing)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build();

        notificationmanager.notify(NOTIFICATION_SERVICE_RUNNING_ID, notifica);
    }

    public void disconnectService(){
        //Scollego wakeupservice
        if(wakeupBoundToActivity){
            unbindService(wakeupConnection);
            wakeupBoundToActivity=false;
            wakeService.stopService(new Intent(IdleActivity.this,WakeUpService.class));
        }

        //Aggiorno icona del bottone
        //NB: per qualche motivo oscuro, se faccio questo nel callback del servizio disconnesso, non funziona
        startButton.setImageResource(R.drawable.ic_play_button_sing);
        //aggiorno scritta in alto
        serviceStatus.setText("Servizio non attivo");

        //tolgo notification
        notificationmanager.cancel(NOTIFICATION_SERVICE_RUNNING_ID);
    }

}
