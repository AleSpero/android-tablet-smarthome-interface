package com.alesp.feedbackapp;

import android.Manifest;
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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Locale;

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
                                    startButton.setImageResource(R.drawable.ic_stop_button_colorato);
                                    serviceStatus.setText(R.string.listening);


                                } else {
                                    //creo alertdialog e faccio terminare il servizio
                                    new AlertDialog.Builder(IdleActivity.this)
                                            .setTitle("Connection not available")
                                            .setMessage("Couldn't connect to the server. Please try later")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    disconnectService();
                                                }
                                            })
                                            .show();


                                }
                            }
                        }, 300);

                    }

                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.v("onServiceDisonnected","Service WakeUpService disconnesso!");
                    wakeupBoundToActivity = false;
                }
            };






    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.idle_activity);

        //Faccio collegamenti
        startButton = (ImageButton) findViewById(R.id.start_service);
        settings = (ImageButton) findViewById(R.id.settings);
        serviceStatus = (TextView) findViewById(R.id.service_running);

        startButton.setImageResource(R.drawable.ic_play_button_sing_colorato);
        settings.setImageResource(R.drawable.ic_settings);



        //controllo permission
        if (ContextCompat.checkSelfPermission(IdleActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(IdleActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    0);

        }
        //se nega il permesso è



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

        //Setto listener per il tasto impostazioni
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Faccio partire settingsActivity
                startActivity(new Intent(IdleActivity.this,SettingsActivity.class));
                //overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

            }
        });



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
                .setContentTitle("Service Running")
                .setContentText("The Activity Recognition service is now running.")
                .setSmallIcon(R.drawable.ic_play_button_sing_colorato)
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
        startButton.setImageResource(R.drawable.ic_play_button_sing_colorato);
        //aggiorno scritta in alto
        serviceStatus.setText(R.string.notActive);

        //tolgo notification
        notificationmanager.cancel(NOTIFICATION_SERVICE_RUNNING_ID);
    }

}
