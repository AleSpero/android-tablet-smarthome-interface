package com.alesp.feedbackapp.MetaWear;
import com.alesp.feedbackapp.R;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.stefano.myapplication.R;
import com.example.stefano.myapplication.Utils;
import com.example.stefano.myapplication.Weka;
import com.example.stefano.myapplication.stickers.ClassifiedManipulation;
import com.example.stefano.myapplication.stickers.MyMovement;
import com.example.stefano.myapplication.stickers.beanInfoSticker.SingletonMapModel;
import com.example.stefano.myapplication.stickers.beanInfoSticker.SingletonMapSticker;
import com.example.stefano.myapplication.stickers.beanInfoSticker.Sticker;
import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.data.CartesianShort;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.Switch;

import java.util.Map;

import weka.classifiers.Classifier;


public class MetaWear extends AppCompatActivity implements ServiceConnection{
    private final static String MAC_ADDRESS= "EB:0F:36:60:42:BF";

    private MetaWearBoard mwBoard;
    private double segmentThreshold;
    private double timeThreshold;
    private Map<String, Sticker> mapSticker;
    private Map<String, Classifier> mapModel;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mwBoard.disconnect();
        this.getApplicationContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, final IBinder service) {
        MetaWearBleService.LocalBinder binder = (MetaWearBleService.LocalBinder) service;

        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice btDevice = btManager.getAdapter().getRemoteDevice(MAC_ADDRESS);


        binder.executeOnUiThread();

        setContentView(R.layout.activity_meta_wear);

        Intent intent = getIntent();
        segmentThreshold = Double.parseDouble(intent.getStringExtra("SegmentTreshold"));
        timeThreshold = Double.parseDouble(intent.getStringExtra("TimeTreshold"));

        this.mapSticker = SingletonMapSticker.getInstance().getMapSticker();
        this.mapModel = SingletonMapModel.getInstance().getMapModel();


        mwBoard = binder.getMetaWearBoard(btDevice);
        mwBoard.setConnectionStateHandler(new MetaWearBoard.ConnectionStateHandler() {
            @Override
            public void connected() {
                try {
                    mwBoard.getModule(Switch.class).routeData().fromSensor().stream("switch_stream").commit()
                            .onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                                @Override
                                public void success(RouteManager result) {
                                    result.subscribe("switch_stream", new RouteManager.MessageHandler() {
                                        @Override
                                        public void process(Message msg) {
                                        }
                                    });
                                }
                            });
                    final Accelerometer accelModule = mwBoard.getModule(Accelerometer.class);


                    accelModule.setOutputDataRate(10);
                    accelModule.routeData().fromAxes().stream("acceleration_stream").commit()
                            .onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                                @Override
                                public void success(RouteManager result) {
                                    result.subscribe("acceleration_stream", new RouteManager.MessageHandler() {
                                        AccelerationData previous = null;
                                        AccelerationData current;
                                        AccelerationData lastManipulation = null;
                                        boolean isStarted = true;
                                        boolean isMoving = false;
                                        long duration = 0;

                                        MetaWearManipulation manipulation = new MetaWearManipulation(MAC_ADDRESS);
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
                                                    if (current.euclideanDistance(previous) > segmentThreshold || current.checkIfMoved(previous, segmentThreshold)) {
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
                                                    if(current.euclideanDistance(previous) > segmentThreshold || current.checkIfMoved(previous, segmentThreshold)){
                                                        /*In this IF statment the sensor starts moving so we check how much time passed from last stop*/
                                                        if(lastManipulation != null) {
                                                            double timeStampDifference = lastManipulation.getTimestampDifference(current);

                                                            if (timeStampDifference < timeThreshold) {
                                                            /*This IF statement means didn't take too long from last stop*/
                                                                System.out.println(manipulation);
                                                                manipulation.addAccelerationData(current);
                                                                isMoving = true;
                                                            }
                                                            else
                                                            {
                                                                System.out.println(manipulation);
                                                                isMoving = true;
                                                                manipulation.addAccelerationData(current);
                                                                System.out.println("Movement started");
                                                            }
                                                        }
                                                        else
                                                        {
                                                            isMoving = true;
                                                            System.out.println(manipulation);
                                                            manipulation.addAccelerationData(current);
                                                            System.out.println("Movement started");
                                                        }
                                                    }
                                                    else
                                                    {
                                                        if (lastManipulation != null) {
                                                            if(current.getTimestampDifference(lastManipulation) > timeThreshold) {
                                                                System.out.println("Movement ended, duration: " + manipulation.getDuration() + "ms");
                                                                lastManipulation = null;
                                                                sendMovement(manipulation);
                                                                //manipulation.clearManipulation();
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
                    //Snackbar.make(getActivity().findViewById(R.id.activity_main_layout), e.getLocalizedMessage(), Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void disconnected() {
                mwBoard.disconnect();
            }

            @Override
            public void failure(int status, Throwable error) {
                mwBoard.disconnect();
            }
        });
        mwBoard.connect();

/*
        findViewById(R.id.disconnectButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mwBoard.disconnect();
                System.out.println("Disconnesso dalla board");
                Intent intent = new Intent(MetaWear.this, MainActivity.class);
                startActivity(intent);
            }
        });*/
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mwBoard.disconnect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mwBoard.disconnect();
    }

    private void sendMovement(MetaWearManipulation m) {
        /*long duration = m.getDuration();
        Statistics stat = new Statistics(m.getListX(), m.getListY(), m.getListZ(), duration);
        MyMovement movement = new MyMovement(stat, MAC_ADDRESS, duration, m.getListX().size(), null);


        //MovementList ml = SpezzaMovimento(movement);
        movement = classify(movement);
        String ret = send(movement);*/
    }

    private String send(MyMovement movement) {
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
        prediction = Weka.classify(mapModel.get(mapSticker.get(identifier).getType()), Weka.getTest(ml, mapSticker.get(identifier).getType()));

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
