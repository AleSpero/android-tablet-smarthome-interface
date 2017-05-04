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
import android.widget.TextView;

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

public class QueryUser extends Activity implements RecognitionListener {

    //Nota bene: implemento l'interfaccia RecognitionListener e definisco i vari metodi per la speechRecognition.

    //Definisco variabili per la UI
    Button firstactivity;
    Button secondactivity;
    Button thirdactivity;
    Button otheractivity;
    RecognitionProgressView progress;
    TextView userinput;
    TextView firstactivityText;
    TextView secondactivityText;

    //creo variabile per text to speech
    TextToSpeech textToSpeech;

    //Creo costanti per l'attività selezionata
    static int FIRST_ACTIVITY = 0;
    static int SECOND_ACTIVITY = 1;

    //definisco altre variabili
    JSONObject receivedData;
    JSONObject maxObj;
    JSONObject tempObj;
    JSONObject selectedObj;

    JSONArray probActivities;
    JSONArray sortedActivities = new JSONArray();
    int maxIndex;

    SharedPreferences sp;

    //definisco variabile per il riconoscimento vocale
    SpeechRecognizer recognizer;
    SpeechRecognizer yesornorecognizer;
    Intent recognitionIntent;
    RecognitionListener yesornolistener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

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

                    new AlertDialog.Builder(QueryUser.this)
                            .setTitle(getString(R.string.insuffpermtitle))
                            .setMessage(getString(R.string.insuffpermdescr))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();

                    //FAI ALERT DIALOG CHIEDENDO PERMISSIONS
                    message = "Insufficient permissions";
                    break;

                case SpeechRecognizer.ERROR_NETWORK:
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "Network error";
                    //esco da queryUser
                    new AlertDialog.Builder(QueryUser.this)
                            .setTitle("Connection not available")
                            .setMessage("Couldn't connect to the server. Please try later.")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show()
                            .setCanceledOnTouchOutside(false);
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RecognitionService busy";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "error from server";
                    break;
                default:
                    textToSpeech.speak(getString(R.string.retry),TextToSpeech.QUEUE_ADD,null,"RETRY");
                    progress.stop();
                    progress.play();
                    break;
            }
            Log.e("QueryUser", "FAILED " + message);
        }

        @Override
        public void onResults(Bundle results) {
            //qui gestisco i risultati.
            ArrayList<String> matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String result = matches.get(0);

            userinput.setText(result);
            Log.i("QueryUser","onResults");
            //qui gestisco i risultati.

            if(result.contains("yes")){
                textToSpeech.speak("Ok",TextToSpeech.QUEUE_ADD,null,null);
                yesornorecognizer.destroy();
                finish();
            }
            else if(result.contains("no")){
                progress.stop();
                progress.setSpeechRecognizer(recognizer);
                progress.setRecognitionListener(QueryUser.this);
                progress.play();
                yesornorecognizer.stopListening();
                yesornorecognizer.destroy();
                textToSpeech.speak(getString(R.string.whichActivity), TextToSpeech.QUEUE_FLUSH, null, "WHICH_ACTIVITY");

            }
            else{

            }



        }


        @Override
        public void onPartialResults(Bundle partialResults) {
            //qui gestisco i risultati.
            ArrayList<String> matches = partialResults
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            userinput.setText(matches.get(0));
            Log.i("QueryUser","onPartialResults");
            //qui gestisco i risultati.
        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

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
            Log.d("WakeUpServ_QueryUs", "Servizio connesso");

            //Setto il flag boundtoprocess = true
            wakeupBoundToActivity = true;

            //Effettuo il collegamento (giusto?)
            WakeUpService.WakeUpBinder binder = (WakeUpService.WakeUpBinder) service;
            wakeService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("WakeUpServ_QueryUs", "Servizio disconnesso");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_user);

        //faccio collegamenti vari
        firstactivity = (Button) findViewById(R.id.first_activity);
        secondactivity = (Button) findViewById(R.id.second_activity);
        // thirdactivity = (Button) findViewById(R.id.third_activity);
        // otheractivity = (Button) findViewById(R.id.other);
        progress = (RecognitionProgressView) findViewById(R.id.progress);
        userinput = (TextView) findViewById(R.id.userinput);
        firstactivityText = (TextView) findViewById(R.id.firstactivity_text);
        secondactivityText = (TextView) findViewById(R.id.secondactivity_text);

        //Effettuo il binding con WakeUpService
        Intent servIntent = new Intent(QueryUser.this, WakeUpService.class);
        bindService(servIntent, wakeupConnection, Context.BIND_AUTO_CREATE);
        wakeupBoundToActivity = true;

        //Prendo sharedpreferences
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        //Se l'opzione per lo speechrecognizer è attiva
        if (sp.getBoolean("voiceEnabled", true)) {
            //Setto intent per lo speechrecognizer
            recognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            recognitionIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());


            //Inizializzo lo speech recognizer e setto il RecognitionprogressView
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(this);
            progress.setSpeechRecognizer(recognizer);
            progress.setRecognitionListener(this);

            progress.setVisibility(View.VISIBLE);

            //Inizializzo speech recognizer per il yes or no
            yesornorecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            yesornorecognizer.setRecognitionListener(yesornolistener);



        }


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


        //Inizializzo il TTS (Se lla voce è attiva nelle impostazioni)
        if (sp.getBoolean("voiceEnabled", true)) {
            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    //qui posso cambiare impostazioni, come il locale e altro
                    textToSpeech.setLanguage(Locale.getDefault());
                    Log.v("QueryUser", "TTS inizializzato");

                    //Faccio partire la vocina
                    textToSpeech.speak(getString(R.string.whichActivity), TextToSpeech.QUEUE_FLUSH, null, "WHICH_ACTIVITY");

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


                            switch (utteranceId) {
                                case "WHICH_ACTIVITY":
                                    textToSpeech.speak("Are you " + firstactivityText.getText().toString()
                                            + " ?", TextToSpeech.QUEUE_ADD, null, null);
                                    textToSpeech.speak("Or are you" + secondactivityText.getText().toString() + " ?", TextToSpeech.QUEUE_ADD, null, "SPECIFIC");
                                    break;

                                case "SPECIFIC":
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            //faccio partire il listening
                                            //startRecognition();

                                            progress.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    startRecognition();
                                                }
                                            }, 50);
                                        }
                                    });
                                    break;

                                case "RETRY":

                                    //qua è da modificare
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

                                case "MISTAKE":
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //fermo progressbar e recognizer, faccio partire yesornorecognizer
                                            userinput.setText("");
                                            recognizer.stopListening();
                                            recognizer.destroy();
                                            progress.stop();
                                            progress.setSpeechRecognizer(yesornorecognizer);
                                            progress.setRecognitionListener(yesornolistener);
                                            progress.play();
                                            progress.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    yesornorecognizer.startListening(recognitionIntent);
                                                }
                                            }, 50);
                                        }
                                    });


                                    break;

                                case "FEEDBACKRECEIVED":
                                    //faccio passsare un secondo (o poco meno) e termino l'activity
                                    finish();
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                break;
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {

                        }
                    });
                }
            });
        }


        //Prendo il dato ricevuto dal service e lo trasformo in un oggetto JSON
        try {
            datafromService = getIntent().getExtras();
            receivedData = new JSONObject(datafromService.getString("Data"));

            //Chiamo il metodo sortactivities, che prende in input l'oggetto json ricevuto e restituisce un
            //Array JSON ordinato
            sortedActivities = ActivityRecognition.sortActivities(receivedData);

            //Aggiorno i bottoni con le attività più probabili
            firstactivityText.setText((String) sortedActivities.getJSONObject(0).get("activity"));
            secondactivityText.setText((String) sortedActivities.getJSONObject(1).get("activity"));


            //thirdactivity.setText((String)sortedActivities.getJSONObject(2).get("activity"));


            //setto listener per bottoni.

            firstactivity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //chiamo processFeedback, che creerà l'oggetto JSON da inviare e procederà ad inviarlo (e ad eseguire l'animazione)
                    processFeedback(FIRST_ACTIVITY);
                }
            });

            secondactivity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //chiamo processFeedback, che creerà l'oggetto JSON da inviare e procederà ad inviarlo (e ad eseguire l'animazione)
                    processFeedback(SECOND_ACTIVITY);
                }
            });

        } catch (JSONException e) {
            Log.e("onServiceConnected", e.toString());
        }


    }

    @Override
    protected void onStart() {
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
    protected void onStop() {
        super.onStop();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", false);
        ed.commit();

        //Stoppo TTS
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        if (wakeupBoundToActivity) {
            unbindService(wakeupConnection);
            wakeupBoundToActivity = false;
        }

        //Stoppo speechrecognition
        if (recognizer != null) {
            recognizer.destroy();
            Log.v("QueryUser", "Recognizer stoppato");
        }
    }

    public void animate() {
        //Questo metodo contiene tutte le animazioni che vengono effettuate una volta toccata l'attività corrispondente.

        if (sp.getBoolean("voiceEnabled", true)) {

            YoYo.with(Techniques.FadeOut)
                    .duration(700)
                    .playOn(progress);

            //Stoppo speechview e recognizer
            progress.stop();
            recognizer.destroy();
        }

        //Faccio animazioni dei bottoni, ecc, che scompaiono, e poi li rimuovo

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(firstactivity);

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(secondactivity);

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(firstactivityText);

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(secondactivityText);

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(findViewById(R.id.titleDescr));

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(findViewById(R.id.userinput));

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(findViewById(R.id.title));

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(findViewById(R.id.userinput));


        //Aspetto la fine dell'animazione, e rimuovo tutto
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //rimuovo i vari elementi dalla view, per fare posto alla scritta con "feedback ricevuto"
                firstactivity.setVisibility(View.GONE);
                secondactivity.setVisibility(View.GONE);
                firstactivityText.setVisibility(View.GONE);
                secondactivityText.setVisibility(View.GONE);
                //thirdactivity.setVisibility(View.GONE);
                findViewById(R.id.titleDescr).setVisibility(View.GONE);
                findViewById(R.id.title).setVisibility(View.GONE);
                findViewById(R.id.userinput).setVisibility(View.GONE);
                progress.setVisibility(View.GONE);

                //faccio comparire la scritta di feedback ricevuto
                findViewById(R.id.feedbackReceived).setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn)
                        .duration(700)
                        .playOn(findViewById(R.id.feedbackReceived));

                /*Faccio partire suono notifica
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play(); TROVA MODO DI CAMBIARE SUONERIA*/

                if(sp.getBoolean("voiceEnabled",true)) {
                    textToSpeech.speak(getString(R.string.feedbackReceived), TextToSpeech.QUEUE_ADD, null, "FEEDBACKRECEIVED");
                }
                else{
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //faccio passsare un secondo (o poco meno) e termino l'activity
                            finish();
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                    },1200);
                }
                    }

        }, 700);


    }

    public void processFeedback(int activityId) {

        //Questo metodo riceve un intero, che identifica quale attività è stata scelta, tra quella più probabile e quella meno probabile.
        //Successivamente, ottengo l'oggetto JSON che si riferisce ad una determinata attività, estrapolo l'oggetto, e creo l'oggetto json da inviare
        //questo oggetto sarà composto da un id della richiesta, da l'offset ("quanto ci mette l'utente a rispondere") e dalla scelta dell'utente.

        //stoppo il text to speech se sta parlando
        textToSpeech.stop();

        //Creo il JSON, e chiamo il servizio WakeUpService per inviarlo al server.
        try {
            //Prendo l'oggetto JSON dell'attività selezionata
            selectedObj = sortedActivities.getJSONObject(activityId);

            //Calcolo quanto tempo è passato dall'invio della richiesta all'input dell'utente.
            long offset = System.currentTimeMillis() - receivedData.getLong("time");

            tempObj = new JSONObject("{'requestId'=" + receivedData.get("requestId") + ", 'offset'=" + offset + ",'result'='" + selectedObj.get("activity") + "'}");

            Log.d("Nuovo JSON", tempObj.toString());
        } catch (JSONException e) {
            Log.e("WakeUpService", e.toString());
        }


        //Procedo con l'invio, segnalo la conferma del feedback e chiudo l'activity
        wakeService.sendData(tempObj.toString());

        animate();

    }





    //INIZIO METODI PER LA SPEECH RECOGNITION

    //in questo metodo faccio partire il listening, imposto l'icona e faccio partire il suono
    public void startRecognition(){
        recognizer.startListening(recognitionIntent);
        userinput.setText("");
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

                new AlertDialog.Builder(QueryUser.this)
                        .setTitle(getString(R.string.insuffpermtitle))
                        .setMessage(getString(R.string.insuffpermdescr))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show()
                        .setCanceledOnTouchOutside(false);

               //FAI ALERT DIALOG CHIEDENDO PERMISSIONS
                message = "Insufficient permissions";
                break;

            case SpeechRecognizer.ERROR_NETWORK:
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network error";
                //esco da queryUser
                new AlertDialog.Builder(QueryUser.this)
                        .setTitle("Connection not available")
                        .setMessage("Couldn't connect to the server. Please try later.")
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
                textToSpeech.speak(getString(R.string.retry),TextToSpeech.QUEUE_ADD,null,"RETRY");
                progress.stop();
                progress.play();
                break;
        }
        Log.e("QueryUser", "FAILED " + message);

    }


    @Override
    public void onPartialResults(Bundle arg0) {
        //qui gestisco i risultati.
        ArrayList<String> matches = arg0
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        userinput.setText(matches.get(0));
        Log.i("QueryUser","onPartialResults");
        //qui gestisco i risultati.
    }



    @Override
    public void onResults(Bundle results) {

        //In questo metodo gestisco i risultati del riconoscimento vocale.
        //Prima di tutto, ottengo i risultati e li stampo. successivamente, verifico se nella stringa ottenuta è presente
        //una delle due parole (NON TUTTE E DUE!). Successivamente chiamerò processFeedback.

        //MANCA gestire il fatto che l'utente potrebbe dire un'altra attività
        //magari chiedere che attività sta facendo e controllare se è presente nell'array? FAI COME COSA IN PIU' E CHIEDI NEL DOCUMENTO

        //NB: metto il lower case, se no non mi riconosce le stringhe

        //Prendo il primo risultato dell'arraylist (la stringa riconosciuta più probabile)
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String result;
        boolean found = false;
        int i=0;


        String first = firstactivityText.getText().toString().toLowerCase();
        String second = secondactivityText.getText().toString().toLowerCase();


        do{
            result = matches.get(i).toLowerCase();

            if(result.contains(first) && !result.contains(second)){
                //Allora è stata detta l'attività del primo bottone: chiamo processfeedback passanto l'activityId 0
                found = true;
                userinput.setText(result);
                processFeedback(FIRST_ACTIVITY);
            }
            else if(result.contains(second) && !result.contains(first)){
                //Allora è stata detta l'attività del secondo bottone
                found = true;
                userinput.setText(result);
                processFeedback(SECOND_ACTIVITY);
            }

            i++;
        }while(!found && i<matches.size());



       //Ora, gestisco se l'utente non ha detto nessuna delle due attività:
        //chiederò inizialmente all'utente se ho sbagliato, e in caso di risposta affermativa, chiudo
        //NB: qui ci sarebbe da fare quella cosa opzionale di chiedere che attività sta facendo per controllare l'array
        if(!found){

            Log.d("QueryUser","Nessuna delle due");


            //Chiedo all'utente se ho sbagliato
            textToSpeech.speak(getString(R.string.mistake),TextToSpeech.QUEUE_ADD,null,"MISTAKE");

        }





        Log.d("QueryUser",matches.toString());
        Log.i("QueryUser", "onResults");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.i("QueryUser", "onRmsChanged: " + rmsdB);
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i("QueryUser", "onReadyForSpeech");
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i("QueryUser", "onEvent");
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

}
