package com.alesp.feedbackapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alesp on 14/03/2017.
 */

public class QueryUser extends Activity {

    //Definisco variabili per la UI
    Button firstactivity;
    Button secondactivity;
    Button thirdactivity;
    Button otheractivity;

    //definisco altre variabili
    JSONObject receivedData;
    JSONObject maxObj;
    JSONArray probActivities;
    JSONArray sortedActivities = new JSONArray();
    int maxIndex;
    //Questa variabile serve per tenere traccia del bottone other cliccato. se esso è stato cliccato, vuole dire che si stanno
    //guardando le attività 4-5-6 e quindi bisognerà riferirsi a quegli indici. altrimenti le attività interessate saranno la 1-2-3.
    boolean otherbuttonPressed = false;

    //Questa variabile serve per ottenere i dati dal service
    Bundle datafromService;

    //Definisco il mio service e il boolean boundtoactivity per indicare se il processo
    // è collegato all'activity
    ConnectionService connService;
    boolean boundToActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_user);

        //faccio collegamenti vari
        firstactivity = (Button) findViewById(R.id.first_activity);
        secondactivity = (Button) findViewById(R.id.second_activity);
        thirdactivity = (Button) findViewById(R.id.third_activity);
        otheractivity = (Button) findViewById(R.id.other);

            //Prendo il dato ricevuto dal service e lo trasformo in un oggetto JSON
            try {
                datafromService = getIntent().getExtras();
               // probActivities = new JSONArray(datafromService.getString("Data")).getJSONArray(1);
                receivedData = new JSONObject(datafromService.getString("Data"));
                probActivities = (JSONArray) receivedData.get("data");
                maxObj = new JSONObject("{'activity':'lol','probability':0.0}");

                //Costruisco un nuovo JSONArray con le attività ordinate in modo descrescente per la probabilità
                while(probActivities.length()!=0){

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

                //Aggiorno i bottoni con le attività più probabili
                firstactivity.setText((String)sortedActivities.getJSONObject(0).get("activity"));
                secondactivity.setText((String)sortedActivities.getJSONObject(1).get("activity"));
                thirdactivity.setText((String)sortedActivities.getJSONObject(2).get("activity"));


                //Setto gli onclick listener per il bottone other (che cambia i primi 3 bottoni) e gli altri bottoni
                otheractivity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //se "altro" è già stato premuto, allora esso fungerà da tasto "back". e quindi displayerà le prime 3 attività.
                        //altrimenti, mostra le attività 4-5-6
                        if(otherbuttonPressed){
                            try {
                                firstactivity.setText((String) sortedActivities.getJSONObject(0).get("activity"));
                                secondactivity.setText((String) sortedActivities.getJSONObject(1).get("activity"));
                                thirdactivity.setText((String) sortedActivities.getJSONObject(2).get("activity"));

                                otheractivity.setText("Altro");

                                //Faccio animazioni carine
                                YoYo.with(Techniques.FadeIn)
                                        .duration(500)
                                        .playOn(findViewById(R.id.first_activity));

                                YoYo.with(Techniques.FadeIn)
                                        .duration(500)
                                        .playOn(findViewById(R.id.second_activity));

                                YoYo.with(Techniques.FadeIn)
                                        .duration(500)
                                        .playOn(findViewById(R.id.third_activity));

                                //setto otherbuttonpressed
                                otherbuttonPressed = false;
                            }
                            catch(JSONException e){
                                Log.e("QueryUser,OnClick",e.toString());
                            }
                        }
                        else{
                            try {
                                firstactivity.setText((String) sortedActivities.getJSONObject(3).get("activity"));
                                secondactivity.setText((String) sortedActivities.getJSONObject(4).get("activity"));
                                thirdactivity.setText((String) sortedActivities.getJSONObject(5).get("activity"));

                                otheractivity.setText("Indietro");

                                //Faccio animazioni carine
                                YoYo.with(Techniques.FadeIn)
                                        .duration(800)
                                        .playOn(findViewById(R.id.first_activity));

                                YoYo.with(Techniques.FadeIn)
                                        .duration(800)
                                        .playOn(findViewById(R.id.second_activity));

                                YoYo.with(Techniques.FadeIn)
                                        .duration(800)
                                        .playOn(findViewById(R.id.third_activity));

                                //setto otherbuttonpressed
                                otherbuttonPressed = true;
                            }
                            catch(JSONException e){
                                Log.e("QueryUser,OnClick",e.toString());
                            }
                        }
                    }
                });


            }
            catch(JSONException e){
                Log.e("onServiceConnected",e.toString());
            }

    }

    @Override
    protected void onStart(){
        super.onStart();

        //Inserisco nelle sharedpref che l'activity sta andando (mi serve nel wakeup service)
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", true);
        ed.commit();

        //Faccio partire suono notifica
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", false);
        ed.commit();
    }
}
