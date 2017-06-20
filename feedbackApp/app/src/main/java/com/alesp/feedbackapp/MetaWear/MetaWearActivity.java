package com.alesp.feedbackapp.MetaWear;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stefano.myapplication.R;
import com.example.stefano.myapplication.Utils;
import com.example.stefano.myapplication.Weka;
import com.example.stefano.myapplication.stickers.ClassifiedManipulation;
import com.example.stefano.myapplication.stickers.MyMovement;
import com.example.stefano.myapplication.stickers.Statistics;
import com.example.stefano.myapplication.stickers.beanInfoSticker.Sticker;
import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.data.CartesianShort;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.MultiChannelTemperature;
import com.mbientlab.metawear.module.MultiChannelTemperature.MetaWearRChannel;
import com.mbientlab.metawear.module.MultiChannelTemperature.Source;

import java.util.List;
import java.util.Map;

import weka.classifiers.Classifier;

/**
 * Created by civi on 06/09/16.
 */
public class MetaWearActivity extends Activity implements ServiceConnection  {

    private String MAC;

    private final static double segmentThresholdOnStop = 25.0;
    private final static double segmentThresholdOnMoving =1.5;
    private final static double timeThreshold = 300.;

    private volatile String model = "medicine";

    private MetaWearBoard metaWearBoard;
    private MetaWearBleService.LocalBinder serviceBinder;
    private ProgressDialog progress;
    private Map<String, Sticker> mapSticker;
    private Map<String, Classifier> mapModel;


    private boolean sendToServer = false;

    private  MultiChannelTemperature temperatureModule = null;
    List<Source> tempSources = null;

    final Handler handler = new Handler();

    final Runnable readTemperature = new Runnable() {
        @Override
        public void run() {

            //Log.d("MetaWearScanning", "Scanning temperature...");
            temperatureModule.readTemperature(tempSources.get(MetaWearRChannel.NRF_DIE));

            handler.postDelayed(readTemperature, 1000);

        }
    };

    private final MetaWearBoard.ConnectionStateHandler stateHandler= new MetaWearBoard.ConnectionStateHandler() {
        @Override
        public void connected() {
            progress.dismiss();
            Toast.makeText(getApplicationContext(), "Connesso!", Toast.LENGTH_SHORT).show();
            Log.i("MainActivity", "Connected");
            accelerometerSampling();
            temperatureSampling();
        }

        @Override
        public void disconnected() {
            Log.i("MainActivity", "Connected Lost");
        }

        @Override
        public void failure(int status, Throwable error) {
            Log.e("MainActivity", "Error connecting", error);
            //progress.dismiss();


            connectBoard();

            //Toast.makeText(getApplicationContext(), "Impossible to connect to the board...", Toast.LENGTH_SHORT).show();
        }
    };

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meta_wear);


        MAC = getIntent().getStringExtra("device");
        TextView textView = (TextView) findViewById(R.id.metawear_device);
        switch (MAC){
            case "DF:DF:C1:8A:6D:4A" : textView.setText("Device: Metawear 1");
                break;
            case "EB:0F:36:60:42:BF" : textView.setText("Device: Metawear 2");
                break;
        }



        Spinner modelSPinner = (Spinner) findViewById(R.id.modelsSpinner);


        modelSPinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                model = parent.getItemAtPosition(position).toString();
                Log.i("MetaWear", "Model switched to: "+model);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //model = "medicine";
            }
        });


        progress = new ProgressDialog(this);


        progress.setTitle("Connecting to "+MAC);
        progress.setMessage("Please wait (have faith)");




        Switch toggle = (Switch) findViewById(R.id.switchServer);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendToServer = isChecked;
            }
        });


        //final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        //btManager.getAdapter().disable();


        //this.mapSticker = SingletonMapSticker.getInstance().getMapSticker(); //deve essere attivo
        //this.mapModel = SingletonMapModel.getInstance().getMapModel(); //deve essere attivo

        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class), this, Context.BIND_AUTO_CREATE);



    }

    public void accelerometerSampling() {

        try {
            final Accelerometer accelModule = metaWearBoard.getModule(Accelerometer.class);

            accelModule.enableAxisSampling();
            accelModule.setOutputDataRate(10.f);
            accelModule.start();

            metaWearBoard.getModule(Accelerometer.class).routeData().fromAxes().stream("accel_stream_key")
                    .commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {

                @Override
                public void success(RouteManager result) {
                    result.subscribe("accel_stream_key", new RouteManager.MessageHandler() {

                        AccelerationData previous = null;
                        AccelerationData current;
                        AccelerationData lastManipulation = null;
                        boolean isStarted = true;
                        boolean isMoving = false;
                        long duration = 0;

                        MetaWearManipulation manipulation = new MetaWearManipulation(MAC);

                        @Override
                        public void process(Message msg) {

                            /*This If statement tell us the streaming data is started*/
                            if(isStarted) {
                                System.out.println("Sto ricevendo dati accelerometro");
                                isStarted = false;
                            }
                            /*This part saves the first acceleration data coming from metaWear board*/
                            if(previous == null) {
                                System.out.println("Prelevo il primo pacchetto di dati");
                                previous = new AccelerationData(msg.getData(CartesianShort.class), msg.getTimestamp());
                            }
                            else
                            {
                                current = new AccelerationData(msg.getData(CartesianShort.class), msg.getTimestamp());
                                if(isMoving) {/*In this IF part the board is moving*/
                                    if (current.euclideanDistance(previous) >segmentThresholdOnMoving || current.checkIfMoved(previous, segmentThresholdOnMoving)) {
                                        /*If the sensor moved consistently we track the movement duration*/
                                        manipulation.addAccelerationData(current);
                                        duration += current.getTimestampDifference(previous);

                                    }else{
                                        /*IF the movement stops, we save last acceleration data with the timestamp*/
                                        manipulation.addAccelerationData(lastManipulation);
                                        lastManipulation = current;
                                        isMoving = false;

                                    }
                                }else{/*In this IF part the sensor is NOT moving*/
                                    if(current.checkMovement(previous,segmentThresholdOnStop )  ){
                                        /*In this IF statment the sensor starts moving so we check how much time passed from last stop*/
                                        if(lastManipulation != null) {
                                            double timeStampDifference = lastManipulation.getTimestampDifference(current);

                                            if (timeStampDifference < timeThreshold) {
                                                /*This IF statement means didn't take too long from last stop*/
                                                manipulation.addAccelerationData(current);
                                                isMoving = true;
                                            }
                                            else
                                            {
                                                isMoving = true;
                                                manipulation.addAccelerationData(current);
                                                System.out.println("Movement STARTED");
                                            }
                                        }
                                        else
                                        {
                                            isMoving = true;
                                            manipulation.addAccelerationData(current);
                                            System.out.println("Movement STARTED");
                                        }
                                    }
                                    else
                                    {
                                        if (lastManipulation != null) {
                                            if(current.getTimestampDifference(lastManipulation) > timeThreshold) {
                                                System.out.println("Movement ENDED, duration: " + manipulation.getDuration() + "ms");
                                                System.out.println(manipulation.toString());
                                                // if statment prova a catturare movimenti rilevati ma non voluti
                                                System.out.println("X: "+Math.abs(current.getAxesX() - previous.getAxesX()));
                                                System.out.println("Y: "+Math.abs(current.getAxesY() - previous.getAxesY()));
                                                System.out.println("Z: "+Math.abs(current.getAxesZ() - previous.getAxesZ()));
                                                if(manipulation.getDuration() < 500 && current.checkRange(previous,20)){
                                                    System.out.println("Movimento non registrato");
                                                }else{
                                                    //sendMovement(manipulation);
                                                }
                                                lastManipulation = null;
                                                manipulation.clearManipulation();

                                            }
                                        }
                                                        /*else
                                                        {
                                                            lastManipulation = null;
                                                        }*/
                                    }
                                }
                                previous = current;
                            }
                        }
                    });
                    accelModule.enableAxisSampling();
                    accelModule.start();
                }
            });

        } catch (UnsupportedModuleException e) {
            Log.e("MetaWear", "No accelerometer");
        }

    }

    public void temperatureSampling(){
        try {

            temperatureModule = metaWearBoard.getModule(MultiChannelTemperature.class);

            tempSources = temperatureModule.getSources();

            temperatureModule.routeData().fromSource(tempSources.get(MultiChannelTemperature.MetaWearRChannel.NRF_DIE)).stream("temperature_stream").commit().
                    onComplete(new AsyncOperation.CompletionHandler<RouteManager>(){

                        @Override
                        public void success(RouteManager result){


                            result.subscribe("temperature_stream", new RouteManager.MessageHandler(){


                                @Override
                                public void process(Message msg){

                                    TextView tempView = (TextView) findViewById(R.id.temperature);
                                    tempView.setText("Temperature: "+(msg.getData(Float.class)-9.)+"°");

                                }

                            });

                        }

                    });

            handler.post(readTemperature);

        } catch (UnsupportedModuleException e) {
            e.printStackTrace();
        }



    }

    public void onDestroy(){
        super.onDestroy();

        getApplicationContext().unbindService(this);
    }

    public void retrieveBoard(){

        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MAC);

        metaWearBoard = serviceBinder.getMetaWearBoard(remoteDevice);

    }

    public void connectBoard(){

        progress.show();
        Log.i("MetaWear", metaWearBoard.getMacAddress());
        Toast.makeText(getApplicationContext(), "Connected to "+metaWearBoard.getMacAddress(),Toast.LENGTH_SHORT);
        metaWearBoard.setConnectionStateHandler(stateHandler);
        metaWearBoard.connect();

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        serviceBinder = (MetaWearBleService.LocalBinder) service;

        serviceBinder.executeOnUiThread();

        retrieveBoard();


        Log.i("MetaWear","board retrieved");

        connectBoard();



    }


    @Override
    public void onServiceDisconnected(ComponentName name) {
        metaWearBoard.disconnect();
    }



    private void sendMovement(MetaWearManipulation m) {
        long duration = m.getDuration();
        Statistics stat = new Statistics(m.getListX(), m.getListY(), m.getListZ(), duration);
        MyMovement movement = new MyMovement(stat, MAC, duration, m.getListX().size(), null);


        //MovementList ml = SpezzaMovimento(movement);
        movement = classify(movement);
        if(sendToServer) {
            send(movement);
        }

        Toast.makeText(getApplicationContext(),"Recognized: "+movement.getAction(), Toast.LENGTH_SHORT).show();
    }

    private String send(MyMovement movement) {
        Log.i("MetaWear", "Sending to server...");
        ClassifiedManipulation classifiedManipulation = new ClassifiedManipulation(movement);
        String r = Utils.sendObject(classifiedManipulation, "sticker/classifiedmanipulation");
        //numSent++;
        return r;
    }


    private MyMovement classify(MyMovement ml) {
        String prediction = "";

        // classify from .model
        /*******************************************************************
         * STEFANO: Weka.classify(classifier, Weka.getTest(), trainingSet)
         * ******************************************************************/
        String identifier = fixMacAddress(ml.getIdentifier());
        // prediction = Weka.classify(mapModel.get(mapSticker.get(identifier).getType()), Weka.getTest(ml, mapSticker.get(identifier).getType()));

        Log.i("MetaWear", "Predicting using model "+model);

        prediction = Weka.classify(mapModel.get(model), Weka.getTest(ml, model));

        ml.setAction(prediction);
        return ml;
    }


    public String fixMacAddress(String macAddress){
        String[] tmp = macAddress.split(":");

        String newAddress = "";
        for(int i = 0; i<tmp.length; i++){
            newAddress += tmp[i];
        }

        return newAddress;

    }

}
