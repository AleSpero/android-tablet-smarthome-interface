package com.alesp.feedbackapp;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by alesp on 20/03/2017.
 */

public class ActivityRecognition extends Activity {

    //Inizializzo variabili per l'update della UI
    ImageButton currentActivity;
    ImageButton back;
    TextView currentActivityText;

    AVLoadingIndicatorView avi;

    //Definisco il mio service e il boolean boundtoactivity per indicare se il processo
    // è collegato all'activity
    private WakeUpService wakeService;
    private boolean wakeupBoundToActivity = false;

    //variabili per notifiche
    NotificationManager notificationmanager;
    Notification notifica;
    int NOTIFICATION_SERVICE_RUNNING_ID = 0;

    //definisco receiver per ricevere dati da wakeupservice
    BroadcastReceiver receiver;
    boolean firstDataReceived = true;

    private ServiceConnection wakeupConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v("onServiceConnected", "Service WakeUpService connesso!");


            Toast.makeText(ActivityRecognition.this, "Activity recognition service started!", Toast.LENGTH_SHORT).show();

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


                        if (!wakeService.isConnected()) {
                            //creo alertdialog e faccio terminare il servizio
                            new AlertDialog.Builder(ActivityRecognition.this)
                                    .setTitle("Connection not available")
                                    .setMessage("Couldn't connect to the server.\nPlease try later.")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            disconnectService();
                                            finish();
                                        }
                                    })
                                    .show()
                                    .setCanceledOnTouchOutside(false);


                        }
                    }
                }, 300);

            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v("onServiceDisonnected", "Service WakeUpService disconnesso!");
            wakeupBoundToActivity = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Se faccio partire l'activity in modo silent, non mostro il layout (e controllo dopo in onresume)
        //if(getIntent().getExtras().getBoolean("isSilent",false)) {
        setContentView(R.layout.activity_recognition);

        //Setto collegamenti vari con bottoni e textview
        back = (ImageButton) findViewById(R.id.back);
        currentActivity = (ImageButton) findViewById(R.id.first_activity);
        currentActivityText = (TextView) findViewById(R.id.currentActivity);

        back.setImageResource(R.drawable.ic_action_back);

        avi = (AVLoadingIndicatorView) findViewById(R.id.avi);
        avi.show();

        // }

        //inizializzo receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //Controllo prima di tutto se ho ricevuto un avviso di connessione persa:
                //se si termino tutto.

                if(intent.getBooleanExtra("CONNECTION_LOST",false)){
                    //creo alertdialog e faccio terminare il servizio
                    new AlertDialog.Builder(ActivityRecognition.this)
                            .setTitle("Lost Connection")
                            .setMessage("Lost connection to the server.\nPlease try later.")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    disconnectService();
                                    finish();
                                }
                            })
                            .show()
                            .setCanceledOnTouchOutside(false);
                }
                else {

                    //Ricevo dati dal service ed aggiorno l'UI
                    String result = intent.getStringExtra("CURRENT_ACTIVITY");
                    JSONObject obj;

                    //controllo se sono i primi dati ricevuti (se si rimuovo scritte e robe varie)
                    if (firstDataReceived) {
                        //rimuovo cose varie e aggiungo altre cose
                        findViewById(R.id.currently).setVisibility(View.VISIBLE);
                        currentActivity.setVisibility(View.VISIBLE);
                        avi.setVisibility(View.GONE);

                        //diminuisco margin di currentactivitytext
                        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        llp.setMargins(0, 20, 0, 0); // llp.setMargins(left, top, right, bottom);
                        currentActivityText.setLayoutParams(llp);

                        firstDataReceived = false;
                    }

                    try {
                        obj = new JSONObject(result);
                        //scrivi in database qui o nel service? boh
                        //cambio testo textview
                        currentActivityText.setText(obj.getString("activity"));
                        //Devo anche cambiare immagine nel bottone

                    } catch (JSONException e) {
                        Log.e("ActivityRecognition", e.toString());
                    }
                }
            }
        };

        //Faccio partire il service
        Intent servIntent = new Intent(ActivityRecognition.this, WakeUpService.class);
        bindService(servIntent, wakeupConnection, Context.BIND_AUTO_CREATE);
        wakeupBoundToActivity = true;

        //Setto comportamento back
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wakeupBoundToActivity) {
            unbindService(wakeupConnection);
            wakeupBoundToActivity = false;
            notificationmanager.cancel(NOTIFICATION_SERVICE_RUNNING_ID);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter("NOTIFY_ACTIVITY")
        );
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public void onBackPressed(){
        //migliora e metti a posto (deve solo cosare l'activity, non tutta l'app)
        moveTaskToBack(true);
    }

    //Metodi custom

    static public JSONArray sortActivities(JSONObject dataFromService){
        //Prendo il dato ricevuto dal service e lo trasformo in un oggetto JSON
        JSONObject receivedData;
        JSONObject maxObj;
        JSONArray probActivities;
        JSONArray sortedActivities = new JSONArray();

        int maxIndex = 0;



        try {
            receivedData = dataFromService;
            probActivities = (JSONArray) receivedData.get("data");
            maxObj = new JSONObject("{'activity':'lol','probability':0.0}");

            //Costruisco un nuovo JSONArray con le attività ordinate in modo descrescente per la probabilità
            while (probActivities.length() != 0) {

                for (int i = 0; i < probActivities.length(); i++) {
                    if (probActivities.getJSONObject(i).getDouble("probability") > maxObj.getDouble("probability")) {
                        maxObj = probActivities.getJSONObject(i);
                        maxIndex = i;
                    }
                }

                //Calcolo il max dell'array, e una volta inserito nel nuovo array, lo cancello dal vecchio
                sortedActivities.put(maxObj);
                maxObj = new JSONObject("{'activity':'lol','probability':0.0}");
                probActivities.remove(maxIndex);

            }

            //Infine, ritorno l'array
            return sortedActivities;

             } catch (JSONException e) {
                    Log.e("onServiceConnected", e.toString());
                    return null;
                }
        }

    private void showNotification() {
        //creo notifica che rimarrà finchè sarà attivo il service

        /* Qui vi sono le istruzioni per fare in modo che cliccando sulla notifica si riapre l'activity*/
        Intent notificationIntent = new Intent(getApplicationContext(), ActivityRecognition.class);

        notificationIntent.setAction("android.intent.action.MAIN");
        notificationIntent.addCategory("android.intent.category.LAUNCHER");

        PendingIntent pendingIntent = PendingIntent.getActivity(ActivityRecognition.this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Qui inizia la costruzionee della notifica vera e propria

        notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifica = new Notification.Builder(this)
                .setContentTitle("Service Running")
                .setContentText("The Activity Recognition service is now running.")
                .setSmallIcon(R.drawable.ic_play_button_sing_colorato)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build();

        notificationmanager.notify(NOTIFICATION_SERVICE_RUNNING_ID, notifica);
    }

    public void disconnectService() {
        //Scollego wakeupservice
        if (wakeupBoundToActivity) {
            unbindService(wakeupConnection);
            wakeupBoundToActivity = false;
            wakeService.stopService(new Intent(ActivityRecognition.this, WakeUpService.class));
        }


        //tolgo notification
        notificationmanager.cancel(NOTIFICATION_SERVICE_RUNNING_ID);
    }

}
