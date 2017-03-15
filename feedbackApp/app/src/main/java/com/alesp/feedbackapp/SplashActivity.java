package com.alesp.feedbackapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.wang.avi.AVLoadingIndicatorView;


public class SplashActivity extends Activity {

    //NB: forse invece ddi usare il serviceconnectionlistener famo tutto nel metodo onserviceconnected?

    //Creo listener customizzato per quando avviene la connessione
    public interface ConnectionListener {
        void onConnected();
        void onReceivedMessage(String message);
    }


    //Definisco variabili
    TextView title;
    TextView connection;
    AVLoadingIndicatorView avi;
    static int SPLASH_TIME_OUT = 3000;

    //Definisco il mio service e il boolean boundtoactivity per indicare se il processo
    // è collegato all'activity
    private ConnectionService connService;
    private boolean boundToActivity = false;

    //Definisco ServiceConnection, che a quanto pare definisce i callback per il binding
    //tra service ed activity.

    private ServiceConnection servConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v("onServiceConnected","Service connesso!");

            //Setto il flag boundtoprocess = true
            boundToActivity = true;

            //Effettuo il collegamento (giusto?)
            ConnectionService.ConnectionBinder binder = (ConnectionService.ConnectionBinder) service;
            connService = binder.getService();

            avi.setVisibility(View.VISIBLE);

            //faccio prima le animazioni carine carine

            YoYo.with(Techniques.FadeIn)
                    .duration(700)
                    .repeat(1)
                    .playOn(findViewById(R.id.avi));

            connection.setVisibility(View.VISIBLE);

            YoYo.with(Techniques.FadeIn)
                    .duration(700)
                    .repeat(1)
                    .playOn(findViewById(R.id.connection));

          // listener.onConnected();

            //Metto un attimo in pausa il thread, in modo che il service possa ricevere tutti i dati
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    //Controllo che il service sia connesso all'activity, e una volta fatto ciò connetto e ricevo i dati
                    if(boundToActivity) {

                        if (connService.isConnected() && connService.getData() != null) {
                            //Se le seguenti condizioni sono verificate, allora sono stati ricevuti correttamente i dati


                            //Faccio partire la nuova activity (Il servizio si unbinda da solo, una volta messa in pausa l'activity)
                            Intent intent = new Intent(SplashActivity.this, QueryUser.class);
                            intent.putExtra("DATA_ARRAY", connService.getData());
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_out, R.anim.fade_in);


                        } else {
                            //connessione non riuscita: mando un errore e chiudo l'app
                            new AlertDialog.Builder(SplashActivity.this)
                                    .setTitle("Connessione non riuscita")
                                    .setMessage("Impossibile connettersi al server.\n Riprova in un altro momento.")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //chiudo l'app
                                            android.os.Process.killProcess(android.os.Process.myPid());
                                            System.exit(1);
                                        }
                                    })
                                    .show();


                        }
                    }
                }
            }, SPLASH_TIME_OUT);


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v("onServiceDisonnected","Service disconnesso!");
            boundToActivity = false;
        }
    };
    private ServiceConnectionListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        //Collego cose
        title = (TextView) findViewById(R.id.title);
        connection = (TextView) findViewById(R.id.connection);
        avi = (AVLoadingIndicatorView) findViewById(R.id.avi);


    }

    @Override
    protected void onStart(){
        super.onStart();

        //Collego l'activity al service
        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent,servConnection,Context.BIND_AUTO_CREATE);
        boundToActivity = true;
    }

    @Override
    protected void onStop(){
        super.onStop();

        //Scollego l'activity al service
        if(boundToActivity){
            unbindService(servConnection);
            boundToActivity=false;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();

        //Scollego l'activity al service
        if(boundToActivity){
            unbindService(servConnection);
            boundToActivity=false;
        }
    }

    public void setServiceConnectionListener(ServiceConnectionListener listener){
        this.listener = listener;
    }

}
