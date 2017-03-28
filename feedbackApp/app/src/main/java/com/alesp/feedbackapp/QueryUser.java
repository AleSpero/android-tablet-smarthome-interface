package com.alesp.feedbackapp;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.zagum.speechrecognitionview.RecognitionBar;
import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by alesp on 14/03/2017.
 */

public class QueryUser extends Activity implements RecognitionListener{

    //Nota bene: implemento l'interfaccia RecognitionListener e definisco i vari metodi per la speechRecognition.

    //Definisco variabili per la UI
    Button firstactivity;
    Button secondactivity;
    Button thirdactivity;
    Button otheractivity;
    RecognitionProgressView progress;

    //creo variabile per text to speech
    TextToSpeech textToSpeech;

    //definisco altre variabili
    JSONObject receivedData;
    JSONObject maxObj;
    JSONObject tempObj;
    JSONObject selectedObj;

    JSONArray probActivities;
    JSONArray sortedActivities = new JSONArray();
    int maxIndex;

    //definisco variabile per il riconoscimento vocale
    SpeechRecognizer recognizer;
    Intent recognitionIntent;

    //Questa variabile serve per tenere traccia del bottone other cliccato. se esso è stato cliccato, vuole dire che si stanno
    //guardando le attività 4-5-6 e quindi bisognerà riferirsi a quegli indici. altrimenti le attività interessate saranno la 1-2-3.
    boolean otherbuttonPressed = false;

    //Questa variabile serve per ottenere i dati dal service
    Bundle datafromService;

    //Definisco il mio service e il boolean boundtoactivity per indicare se il processo
    // è collegato all'activity
    private WakeUpService wakeService;
    private boolean wakeupBoundToActivity = false;

    private ServiceConnection wakeupConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("WakeUpServ_QueryUs","Servizio connesso");

            //Setto il flag boundtoprocess = true
            wakeupBoundToActivity = true;

            //Effettuo il collegamento (giusto?)
            WakeUpService.WakeUpBinder binder = (WakeUpService.WakeUpBinder) service;
            wakeService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("WakeUpServ_QueryUs","Servizio disconnesso");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_user);

        //faccio collegamenti vari
        firstactivity = (Button) findViewById(R.id.first_activity);
        secondactivity = (Button) findViewById(R.id.second_activity);
        thirdactivity = (Button) findViewById(R.id.third_activity);
        otheractivity = (Button) findViewById(R.id.other);
        progress = (RecognitionProgressView) findViewById(R.id.progress);

        //Effettuo il binding con WakeUpService
        Intent servIntent = new Intent(QueryUser.this, WakeUpService.class);
        bindService(servIntent,wakeupConnection, Context.BIND_AUTO_CREATE);
        wakeupBoundToActivity = true;

        //Setto intent per lo speechrecognizer
        recognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());


        //Inizializzo lo speech recognizer e setto il RecognitionprogressView
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(this);
        progress.setSpeechRecognizer(recognizer);
        progress.setRecognitionListener(this);

        //Inizializo grafica speechrecognizerview
        int[] colors = {
                ContextCompat.getColor(this, R.color.color1),
                ContextCompat.getColor(this, R.color.color2),
                ContextCompat.getColor(this, R.color.color3),
                ContextCompat.getColor(this, R.color.color4),
                ContextCompat.getColor(this, R.color.color5)
        };

        int[] heights = {60, 76, 58, 80, 55};

        progress.setColors(colors);
        progress.setBarMaxHeightsInDp(heights);
        progress.play();


        //Inizializzo il TTS
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                //qui posso cambiare impostazioni, come il locale e altro
                textToSpeech.setLanguage(Locale.getDefault());
                Log.v("QueryUser","TTS inizializzato");

                //Faccio partire la vocina
                textToSpeech.speak(getString(R.string.whichActivity),TextToSpeech.QUEUE_FLUSH, null,"WHICH_ACTIVITY");

                //Faccio partire listener una volta che il TTS finisce di parlare
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {

                    }

                    @Override
                    public void onDone(String utteranceId) {
                        //Quando finisco di pronunciare la frase, inizio con il listening

                        //NOTA BENE: per qualche motivo strano questo codice è eseguito in un altro thread. mi tocca fare un runonuithread per far
                        //andare lo speech recognizer.


                        switch (utteranceId){
                            case "WHICH_ACTIVITY":
                            case "RETRY":
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        //faccio partire il listening
                                        startRecognition();

                                        progress.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startRecognition();
                                            }
                                        }, 50);
                                    }
                                });

                                break;

                        }
                    }

                    @Override
                    public void onError(String utteranceId) {

                    }
                });
            }
        });

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

                                otheractivity.setText(R.string.other);

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

                                otheractivity.setText(R.string.back);

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


                //setto listener per bottoni.

                firstactivity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //questa variabile è utilizzata per controllare se è stato premuto il tasto "altro", qunidi se sono da
                        //selezionare le prime 3 attività o le ultime 3.

                        int index = !otherbuttonPressed ? 0 : 3;

                        //Creo il JSON, e chiamo il servizio WakeUpService per inviarlo al server.
                        try{
                            //Prendo l'oggetto JSON dell'attività selezionata
                            selectedObj = sortedActivities.getJSONObject(index);

                            //Calcolo quanto tempo è passato dall'invio della richiesta all'input dell'utente.
                            long offset = System.currentTimeMillis()-receivedData.getLong("time");

                            tempObj = new JSONObject("{'requestId'="+receivedData.get("requestId")+", 'offset'="+offset+",'result'='"+selectedObj.get("activity")+"'}");

                            Log.d("Nuovo JSON",tempObj.toString());
                        }
                        catch (JSONException e){
                            Log.e("WakeUpService",e.toString());
                        }


                        //Procedo con l'invio, segnalo la conferma del feedback e chiudo l'activity
                        wakeService.sendData(tempObj.toString());

                        animate();

                    }
                });

                secondactivity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //questa variabile è utilizzata per controllare se è stato premuto il tasto "altro", qunidi se sono da
                        //selezionare le prime 3 attività o le ultime 3.

                        int index = !otherbuttonPressed ? 1 : 4;

                        //Creo il JSON, e chiamo il servizio WakeUpService per inviarlo al server.
                        try{
                            //Prendo l'oggetto JSON dell'attività selezionata
                            selectedObj = sortedActivities.getJSONObject(index);

                            //Calcolo quanto tempo è passato dall'invio della richiesta all'input dell'utente.
                            long offset = System.currentTimeMillis()-receivedData.getLong("time");

                            tempObj = new JSONObject("{'requestId'="+receivedData.get("requestId")+", 'offset'="+offset+",'result'='"+selectedObj.get("activity")+"'}");

                            Log.d("Nuovo JSON",tempObj.toString());
                        }
                        catch (JSONException e){
                            Log.e("WakeUpService",e.toString());
                        }

                        //Procedo con l'invio, segnalo la conferma del feedback e chiudo l'activity
                        wakeService.sendData(tempObj.toString());

                        animate();
                    }
                });

                thirdactivity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //questa variabile è utilizzata per controllare se è stato premuto il tasto "altro", qunidi se sono da
                        //selezionare le prime 3 attività o le ultime 3.

                        int index = !otherbuttonPressed ? 2 : 5;

                        //Creo il JSON, e chiamo il servizio WakeUpService per inviarlo al server.
                        try{
                            //Prendo l'oggetto JSON dell'attività selezionata
                            selectedObj = sortedActivities.getJSONObject(index);

                            //Calcolo quanto tempo è passato dall'invio della richiesta all'input dell'utente.
                            long offset = System.currentTimeMillis()-receivedData.getLong("time");

                            tempObj = new JSONObject("{'requestId'="+receivedData.get("requestId")+", 'offset'="+offset+",'result'='"+selectedObj.get("activity")+"'}");

                            Log.d("Nuovo JSON",tempObj.toString());
                        }
                        catch (JSONException e){
                            Log.e("WakeUpService",e.toString());
                        }

                        //Procedo con l'invio, segnalo la conferma del feedback e chiudo l'activity
                        wakeService.sendData(tempObj.toString());

                        animate();


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
    protected void onStop(){
        super.onStop();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", false);
        ed.commit();

        //Stoppo TTS
        if (textToSpeech!=null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        if(wakeupBoundToActivity){
            unbindService(wakeupConnection);
            wakeupBoundToActivity=false;
        }

        //Stoppo speechrecognition
        if (recognizer != null) {
            recognizer.destroy();
            Log.v("QueryUser","Recognizer stoppato");
        }
    }

    public void animate(){
        //Questo metodo contiene tutte le animazioni che vengono effettuate una volta toccata l'attività corrispondente.

        //Faccio animazioni dei bottoni, ecc, che scompaiono, e poi li rimuovo

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(firstactivity);

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(secondactivity);

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(thirdactivity);

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(findViewById(R.id.other));

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(findViewById(R.id.title));

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(findViewById(R.id.titleDescr));

        //Aspetto la fine dell'animazione, e rimuovo tutto
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //rimuovo i vari elementi dalla view, per fare posto alla scritta con "feedback ricevuto"
                firstactivity.setVisibility(View.GONE);
                secondactivity.setVisibility(View.GONE);
                thirdactivity.setVisibility(View.GONE);
                otheractivity.setVisibility(View.GONE);
                findViewById(R.id.title).setVisibility(View.GONE);
                findViewById(R.id.titleDescr).setVisibility(View.GONE);

                //faccio comparire la scritta di feedback ricevuto
                findViewById(R.id.feedbackReceived).setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn)
                        .duration(700)
                        .playOn(findViewById(R.id.feedbackReceived));

                /*Faccio partire suono notifica
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play(); TROVA MODO DI CAMBIARE SUONERIA*/


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //faccio passsare un secondo (o poco meno) e termino l'activity
                        finish();
                        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    }
                },1200);

            }
        }, 700);




    }



    //INIZIO METODI PER LA SPEECH RECOGNITION

    //in questo metodo faccio partire il listening, imposto l'icona e faccio partire il suono
    public void startRecognition(){
        recognizer.startListening(recognitionIntent);
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i("QueryUser", "onBeginningOfSpeech");

    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i("QueryUser", "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i("QueryUser", "onEndOfSpeech");;
    }

    @Override
    public void onError(int errorCode) {
        String message = "No Input";

        //controllo i vari codici di errore ed agisco di conseguenza
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                //Se non ho la permission, la chiedo all'utente
                //Controllo se l'app ha il permesso di utilizzare il microfono, altrimenti lo chiedo.
                //Ciò è indispenabile per il riconoscimento vocale (altrimento non funziona

                //controllo permission
                if (ContextCompat.checkSelfPermission(QueryUser.this,
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(QueryUser.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                0);
                    recognizer.stopListening();
                    startRecognition();

                }

                message = "Insufficient permissions";
                break;

            case SpeechRecognizer.ERROR_NETWORK:
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network error";
                //esco da queryUser
                new AlertDialog.Builder(QueryUser.this)
                        .setTitle("Connessione non riuscita")
                        .setMessage("Impossibile connettersi al server.\n Riprova in un altro momento.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            default:
                //Dico all'utente che il tablet "non ha capito" e riprovo
                textToSpeech.speak(getString(R.string.retry),TextToSpeech.QUEUE_ADD,null,"RETRY");
                progress.stop();
                progress.play();
                break;
        }
        Log.e("QueryUser", "FAILED " + message);

    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i("QueryUser", "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i("QueryUser", "onPartialResults");
        //qui gestisco i risultati.
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i("QueryUser", "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.i("QueryUser", "onResults");
        //qui gestisco i risultati.
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.d("QueryUser",matches.toString());
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.i("QueryUser", "onRmsChanged: " + rmsdB);

    }

}
