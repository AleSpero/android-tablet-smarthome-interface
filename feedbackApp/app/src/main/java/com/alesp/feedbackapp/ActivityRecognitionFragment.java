package com.alesp.feedbackapp;

import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RunnableFuture;

/**
 * Created by alessandro on 04.05.17.
 */

public class ActivityRecognitionFragment extends Fragment {

    //Inizializzo variabili per l'update della UI
    ImageButton currentActivity;
    ImageButton back;
    TextView currentActivityText;

    AVLoadingIndicatorView avi;

    //Gestisco "scomparsa" dell'activity dopo tot secondi
    TimerTask refreshView;

    //definisco receiver per ricevere dati da wakeupservice
    BroadcastReceiver receiver;
    boolean firstDataReceived = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_activityrecognition, container, false);

        //attivo il flag per cui lo schermo non si spegne
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //Setto collegamenti vari con bottoni e textview
        currentActivity = (ImageButton) view.findViewById(R.id.first_activity);
        currentActivityText = (TextView) view.findViewById(R.id.currentActivity);

        //da cambiare: recupera lo stato del fragment sharedpref
        avi = (AVLoadingIndicatorView) view.findViewById(R.id.avi);
        avi.show();

        //inizializzo receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //Controllo prima di tutto se ho ricevuto un avviso di connessione persa:
                //se si termino tutto.

                Log.d("ActRecFrag","ricevuto broadcast da service");

                if(intent.getBooleanExtra("CONNECTION_LOST",false)){
                    //creo alertdialog e faccio terminare il servizio
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Lost Connection")
                            .setMessage("Lost connection to the server.\nPlease try later.")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //restarto l'activity
                                    getActivity().recreate();

                                }
                            })
                            .show()
                            .setCanceledOnTouchOutside(false);
                }
                else {

                    //Ricevo dati dal service ed aggiorno l'UI
                    String result = intent.getStringExtra("CURRENT_ACTIVITY");
                    JSONObject obj;

                    //Cancello eventuale timertask, se sta aspettando di essere eseguito
                    if(refreshView!=null){
                        refreshView.cancel();
                    }


                    //controllo se sono i primi dati ricevuti (se si rimuovo scritte e robe varie)
                    if (firstDataReceived) {
                        //rimuovo cose varie e aggiungo altre cose
                        view.findViewById(R.id.currently).setVisibility(View.VISIBLE);
                        currentActivity.setVisibility(View.VISIBLE);
                        avi.setVisibility(View.GONE);

                        //diminuisco margin di currentactivitytext
                        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        llp.setMargins(0, 20, 0, 0); // llp.setMargins(left, top, right, bottom);
                        currentActivityText.setLayoutParams(llp);

                        firstDataReceived = false;
                    }

                    //Faccio partire timer che, dopo 10 secondi, toglie l'activity corrente e ritorna in listening for data
                     refreshView = new TimerTask() {
                        @Override
                        public void run() {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //Faccio ritornare la view come all'apertura del fragment
                                    view.findViewById(R.id.currently).setVisibility(View.GONE);
                                    currentActivity.setVisibility(View.GONE);
                                    avi.setVisibility(View.VISIBLE);

                                    //diminuisco margin di currentactivitytext
                                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    llp.setMargins(0, 20, 0, 0); // llp.setMargins(left, top, right, bottom);
                                    currentActivityText.setLayoutParams(llp);
                                    currentActivityText.setText("Listening for data...");

                                    firstDataReceived = true;
                                }
                            });

                        }
                    };
                    //faccio partire il timertask
                    new Timer().schedule(refreshView,10000);

                    try {
                        obj = new JSONObject(result);
                        //cambio testo textview
                        currentActivityText.setText(obj.getString("activity"));
                        //Devo anche cambiare immagine nel bottone

                        switch(obj.getString("activity")){

                            case "Making Breakfast":
                                currentActivity.setImageResource(R.drawable.ic_cereal);
                                break;


                            case "Making Lunch":
                                currentActivity.setImageResource(R.drawable.ic_lunch);
                                break;


                            case "Take Medicine":
                                currentActivity.setImageResource(R.drawable.ic_tablets);
                                break;


                            case "Eating":
                                currentActivity.setImageResource(R.drawable.ic_restaurant);
                                break;


                            case "Setting Up The Table":
                                currentActivity.setImageResource(R.drawable.ic_table);
                                break;

                            case "Clearing The Table":
                                //currentActivity.setImageResource(R.drawable.ic_cereal);
                                break;
                        }

                    } catch (JSONException e) {
                        Log.e("ActivityRecognition", e.toString());
                    }
                }
            }
        };

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((receiver),
                new IntentFilter("NOTIFY_ACTIVITY")
        );
        Log.d("Actrecfrag","onStart");
    }

    @Override
    public void onDestroy() {
        Log.d("Actrecfrag","onDestroy");
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);

    }

    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((receiver),
                new IntentFilter("NOTIFY_ACTIVITY")
        );
        Log.d("Actrecfrag","onResume");
    }

}
