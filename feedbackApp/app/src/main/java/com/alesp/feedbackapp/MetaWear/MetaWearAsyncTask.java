package com.example.stefano.myapplication.MetaWear;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stefano.myapplication.R;
import com.example.stefano.myapplication.Utils;
import com.example.stefano.myapplication.Weka;
import com.example.stefano.myapplication.stickers.ClassifiedManipulation;
import com.example.stefano.myapplication.stickers.MyMovement;
import com.example.stefano.myapplication.stickers.Statistics;
import com.example.stefano.myapplication.stickers.beanInfoSticker.SingletonMapModel;
import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.data.CartesianShort;
import com.mbientlab.metawear.module.Accelerometer;

import java.util.Map;

import weka.classifiers.Classifier;

import static com.example.stefano.myapplication.R.color.titleDialog;
import static com.example.stefano.myapplication.R.drawable;
import static com.example.stefano.myapplication.R.id;
import static com.example.stefano.myapplication.R.layout;
import static com.example.stefano.myapplication.R.style;

/**
 * Created by Sara on 21/11/2016.
 **/

/*
La classe si occupa di gestire il rilevamento del movimento per il MetaWear passato al costruttore.
Il rilevamento del movimento, la sua classificazione e l'invio al server vengono gestiti in background.
 */

public class MetaWearAsyncTask extends AsyncTask {

    private String device;
    private String model = "medicine";
    private boolean sendToServer;
    private MetaWearBoard metaWearBoard;
    private Accelerometer accelModule;
    //Map of the models
    private Map<String, Classifier> mapModel;
    private Context context;

    //Threshold controls if the moving is really stopped
    private final static double segmentThresholdOnStop = 25.0;
    //Threshold controls if the moving is continued
    private final static double segmentThresholdOnMoving =1.5;
    //Threshold controls the time passed from the last manipulation
    private final static double timeThreshold = 300.;

    private CustomDialog dialog;


    public MetaWearAsyncTask(Context mContext, String mModel, boolean send, MetaWearBoard mwBoard) {
        this.context = mContext;
        this.device = mwBoard.getMacAddress();
        this.model = mModel;
        this.sendToServer = send;
        this.metaWearBoard = mwBoard;
        dialog = new CustomDialog(mContext, style.CustomDialog);
    }


    @Override
    protected Object doInBackground(Object[] params) {
        mapModel = SingletonMapModel.getInstance().getMapModel();
        System.out.println(device + " " + model + " " + "send: "+sendToServer);
        accelerometerSampling();
        return null;
    }

    public void accelerometerSampling() {

        try {

            accelModule = metaWearBoard.getModule(Accelerometer.class);
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

                        MetaWearManipulation manipulation = new MetaWearManipulation(device);

                        @Override
                        public void process(Message msg) {

                        /*This If statement tell us the streaming data is started*/
                            if (isStarted) {
                                System.out.println("Sto ricevendo dati accelerometro");
                                isStarted = false;
                            }
                        /*This part saves the first acceleration data coming from metaWear board*/
                            if (previous == null) {
                                System.out.println("Prelevo il primo pacchetto di dati");
                                previous = new AccelerationData(msg.getData(CartesianShort.class), msg.getTimestamp());
                            } else {
                                current = new AccelerationData(msg.getData(CartesianShort.class), msg.getTimestamp());
                                if (isMoving) {/*In this IF part the board is moving*/
                                    if (current.euclideanDistance(previous) > segmentThresholdOnMoving || current.checkIfMoved(previous, segmentThresholdOnMoving)) {
                                    /*If the sensor moved consistently we track the movement duration*/
                                        manipulation.addAccelerationData(current);
                                        duration += current.getTimestampDifference(previous);

                                    } else {
                                    /*IF the movement stops, we save last acceleration data with the timestamp*/
                                        manipulation.addAccelerationData(lastManipulation);
                                        lastManipulation = current;
                                        isMoving = false;

                                    }
                                } else {/*In this IF part the sensor is NOT moving*/
                                    if (current.checkIfMoved(previous, segmentThresholdOnStop)) {
                                    /*In this IF statment the sensor starts moving so we check how much time passed from last stop*/
                                        if (lastManipulation != null) {
                                            double timeStampDifference = lastManipulation.getTimestampDifference(current);

                                            if (timeStampDifference < timeThreshold) {
                                            /*This IF statement means didn't take too long from last stop*/
                                                manipulation.addAccelerationData(current);
                                                isMoving = true;
                                            } else {
                                                isMoving = true;
                                                manipulation.addAccelerationData(current);
                                                System.out.println("Movement STARTED");
                                            }
                                        } else {
                                            isMoving = true;
                                            manipulation.addAccelerationData(current);
                                            System.out.println("Movement STARTED");
                                        }
                                    } else {
                                        if (lastManipulation != null) {
                                            if (current.getTimestampDifference(lastManipulation) > timeThreshold) {
                                                System.out.println("Movement ENDED, duration: " + manipulation.getDuration() + "ms");
                                                System.out.println(manipulation.toString());
                                                // IF statment tries to block unintended movements
                                                if (manipulation.getDuration() < 500 && current.checkRange(previous, 20)) {
                                                    System.out.println("Movement unregistered");
                                                } else {
                                                    sendMovement(manipulation);
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


    //Send Manipulation to Server
    private void sendMovement(MetaWearManipulation m) {
        long duration = m.getDuration();
        Statistics stat = new Statistics(m.getListX(), m.getListY(), m.getListZ(), duration);
        MyMovement movement = new MyMovement(stat, device, duration, m.getListX().size(), null);

        //MovementList ml = SpezzaMovimento(movement);
        movement = classify(movement);
        if(sendToServer) {
            send(movement);
        }
        publishProgress(movement);

    }

    private String send(MyMovement movement) {
        Log.i("MetaWear", "Sending to server...");
        ClassifiedManipulation classifiedManipulation = new ClassifiedManipulation(movement);
        String r = Utils.sendObject(classifiedManipulation, "sticker/classifiedmanipulation");
        //numSent++;
        return r;
    }

    //Show runtime a Dialog where are shown image of manipulation and device
    @Override
    protected void onProgressUpdate(Object[] values) {
        MyMovement movement = (MyMovement) values[0];
        System.out.println(device + ": " + " Recognized: "+movement.getAction());

        switch(model){

            case "medicine":
                setMedicine(movement);
                break;
            case "bottle":;
                setBottle(movement);
                break;
            case "pasta":
                setPasta(movement);
                break;
            case "notrefrigeratedfood":
                setNotRefrigeratedFood(movement);
                break;
            case "knife":
                setKnife(movement);
                break;
            //default: Toast.makeText(context, device +  ": " +"Recognized: " + movement.getAction(), Toast.LENGTH_SHORT).show();
        }

    }



    private void setMedicine(MyMovement movement) {
        if((movement.getAction()).equals("OpenMedicine")){
            //showImage(drawable.medicine, drawable.medicine_open_medicine);
        }
        else if((movement.getAction()).equals("SignificantMovement")){
            //showImage(drawable.medicine, drawable.significant_movement);
        }
        else if ((movement.getAction()).equals("Insignificant")) {
            //showImage(drawable.medicine, drawable.insignificant_movement);
        }
    }

    private void setBottle(MyMovement movement) {
        if((movement.getAction()).equals("Drink/Pour")){
            //showImage(drawable.bottle, drawable.bottle_drink_pour);
        }
        else if((movement.getAction()).equals("SignificantMovement")){
            //showImage(drawable.bottle, drawable.significant_movement);
        }
        else if ((movement.getAction()).equals("Insignificant")) {
            showImage(drawable.bottle, drawable.insignificant_movement );
        }
    }

    private void setPasta(MyMovement movement) {
        if((movement.getAction()).equals("Pour")){
            //showImage(drawable.pasta, drawable.pasta_pour);
        }
        else if((movement.getAction()).equals("SignificantMovement")){
            //showImage(drawable.pasta, drawable.significant_movement);
        }
        else if ((movement.getAction()).equals("Insignificant")) {
            //showImage(drawable.pasta, drawable.insignificant_movement);
        }
    }

    private void setKnife(MyMovement movement) {
        if((movement.getAction()).equals("Cut")){
            //showImage(drawable.knife, drawable.knife_cut);
        }
        else if((movement.getAction()).equals("SignificantMovement")){
            //showImage(drawable.knife, drawable.significant_movement);
        }
        else if ((movement.getAction()).equals("Insignificant")) {
            //showImage(drawable.knife, drawable.insignificant_movement);
        }
    }

    private void setNotRefrigeratedFood(MyMovement movement) {
        if((movement.getAction()).equals("SignificantMovement")){
            //showImage(drawable.notrefrigeratedfood, drawable.significant_movement);
        }
        else if ((movement.getAction()).equals("Insignificant")) {
            //showImage(drawable.notrefrigeratedfood, drawable.insignificant_movement);
        }
    }

    //Used to show a specific image depending on type of manipulation
    private void showImage(int resId, int resText) {

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL);

        dialog.setContentView(layout.custom_view);

        //set up dialog
        //dialog.setTitle("Manipulation");
        dialog.setCancelable(false);


        //set up Text
        TextView textDevice = (TextView) dialog.findViewById(R.id.dialog_device);
        textDevice.setText(device);
        textDevice.setTextColor(context.getResources().getColor(titleDialog));

        //set up image view
        ImageView img = (ImageView) dialog.findViewById(id.custom_imageView);
        img.setImageResource(resId);
        img.setBackgroundColor(80000000); //the background is setted to be transparent

        ImageView imgText = (ImageView) dialog.findViewById(id.custom_image_text);
        imgText.setImageResource(resText);

        if(resText == drawable.significant_movement){
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.move);
            img.startAnimation(animation);
        }else if(resText == drawable.knife_cut){
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.move_knife);
            img.startAnimation(animation);
        }else if(resText == drawable.insignificant_movement){
            //no animation
        }else {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.rotate_around_center_point);
            img.startAnimation(animation);
        }


        dialog.show();

        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                dialog.dismiss();
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 2000);


        /*
        //Used to show only a image (without device)
        Toast toast = new Toast(context);
        ImageView view = new ImageView(context);
        view.setImageResource(resId);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setView(view);
        toast.show();*/
    }


    //Classify the movement
    private MyMovement classify(MyMovement ml) {
        String prediction = "";

        // classify from .model
        /*******************************************************************
         * STEFANO: Weka.classify(classifier, Weka.getTest(), trainingSet)
         * ******************************************************************/
        //String identifier = fixMacAddress(ml.getIdentifier());
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

    //Used to change model runtime
    public boolean changeModel(String model){
        this.model = model;
        return true;
    }



    public String getDevice(){
        return device;
    }

    public String getModel(){
        return model;
    }

    private Context getContext(){
        return context;
    }



    //Custom Dialog used to show the Manipulation
    public class CustomDialog extends Dialog {

        protected CustomDialog(Context context, int theme) {
            super(context, theme);

        }

    }



}
