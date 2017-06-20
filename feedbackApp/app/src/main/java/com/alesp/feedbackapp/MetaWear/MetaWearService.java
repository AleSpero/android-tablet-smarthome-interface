package com.alesp.feedbackapp.MetaWear;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.data.CartesianShort;
import com.mbientlab.metawear.module.Accelerometer;


public class MetaWearService extends AsyncTask<Void, Void, Void> implements ServiceConnection {

    private MetaWearBoard metaWearBoard;
    private MetaWearBleService.LocalBinder serviceBinder;

    private String MAC;
    private boolean sendToServer;
    private volatile String model = "medicine";
    private Context context;

    //final Handler handler = new Handler();

    private final static double segmentThresholdOnStop = 25.0;
    private final static double segmentThresholdOnMoving =1.5;
    private final static double timeThreshold = 300.;


    public MetaWearService(Context context, String device, String model, boolean send) {
        MAC = device;
        model = model;
        sendToServer= send;
        context = context;
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {

        serviceBinder = (MetaWearBleService.LocalBinder) service;

        serviceBinder.executeOnUiThread();

        retrieveBoard();


        Log.i("MetaWear","board retrieved");

        connectBoard();


    }

    public void connectBoard(){

        Log.i("MetaWear", metaWearBoard.getMacAddress());
        Toast.makeText(context, "Connected to "+metaWearBoard.getMacAddress(),Toast.LENGTH_SHORT);
        metaWearBoard.setConnectionStateHandler(stateHandler);
        metaWearBoard.connect();

    }



    public void retrieveBoard(){

        final BluetoothManager btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MAC);

        metaWearBoard = serviceBinder.getMetaWearBoard(remoteDevice);

    }

    private final MetaWearBoard.ConnectionStateHandler stateHandler= new MetaWearBoard.ConnectionStateHandler() {
        @Override
        public void connected() {
            Toast.makeText(context, "Connesso!", Toast.LENGTH_SHORT).show();
            Log.i("MetawearService", "Connected");

        }

        @Override
        public void disconnected() {
            Log.i("MetawearService", "Connected Lost");
        }

        @Override
        public void failure(int status, Throwable error) {
            Log.e("MetawearService", "Error connecting", error);

            connectBoard();

            //Toast.makeText(getApplicationContext(), "Impossible to connect to the board...", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        metaWearBoard.disconnect();
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

    /*private void sendMovement(MetaWearManipulation m) {
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
    }*/


    @Override
    protected void onPreExecute() {
        final BluetoothManager btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MAC);

        serviceBinder.executeOnUiThread();
        // Create a MetaWear board object for the Bluetooth Device
        metaWearBoard= serviceBinder.getMetaWearBoard(remoteDevice);
        metaWearBoard.connect();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        accelerometerSampling();
        return null;
    }
}
