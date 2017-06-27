package com.example.stefano.myapplication.stickers.Activity;

import com.example.stefano.myapplication.Utils;
import com.example.stefano.myapplication.Weka;
import com.example.stefano.myapplication.stickers.ClassifiedManipulation;
import com.example.stefano.myapplication.stickers.Manipulation;
import com.example.stefano.myapplication.stickers.MyMovement;
import com.example.stefano.myapplication.stickers.MyNearable;
import com.example.stefano.myapplication.stickers.Statistics;
import com.example.stefano.myapplication.stickers.beanInfoSticker.SingletonMapModel;
import com.example.stefano.myapplication.stickers.beanInfoSticker.SingletonMapSticker;
import com.example.stefano.myapplication.stickers.beanInfoSticker.Sticker;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import weka.classifiers.Classifier;

/**
 * Created by claudio on 19/05/16.
 */
public class NearableThread implements Runnable {

    private NearableQueue nearableQueue;
    private String id;
    private Map<String, Sticker> mapSticker;
    private Map<String, Classifier> mapModel;

    public NearableThread(String id, NearableQueue nearableQueue){
        this.id = id;
        this.nearableQueue = nearableQueue;
        this.mapSticker = SingletonMapSticker.getInstance().getMapSticker();
        this.mapModel = SingletonMapModel.getInstance().getMapModel();
    }

    @Override
    public void run() {

        boolean inMotion = false;
        Manipulation currentManipulation = null;
        long THRESHOLD = 500;
        boolean currentMotionState = false;
        MyNearable myNearablePacket = null;

        while(true) {

            try {

                myNearablePacket = this.nearableQueue.poll(THRESHOLD, TimeUnit.MILLISECONDS);

                if(myNearablePacket == null) {
                    currentMotionState = false;
                }
                else {
                    currentMotionState = myNearablePacket.getMotion();
                }

                if (currentMotionState) {

                    if (!inMotion) {
                        inMotion = true;
                        currentManipulation = new Manipulation(myNearablePacket);
                    }

                    currentManipulation.addNearable(myNearablePacket);

                } else {

                    if (inMotion) {

                        inMotion = false;
                        sendMovement(currentManipulation);

                    }


                }


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void sendMovement(Manipulation m) {
        long duration = m.getDuration();
        Statistics stat = new Statistics(m.getListNearable(), duration);
        MyMovement movement = new MyMovement(stat, id, duration, m.getListNearable().size(), null);


        //MovementList ml = SpezzaMovimento(movement);
        movement = classify(movement);
        String ret = send(movement);

        /*
        view.setText(ret);
        viewInviati.setText("Number of sent manipulations: " + numSent);
        */

        /*mvList.add(movement, contatore);
        button.setVisibility(View.VISIBLE);*/
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

        prediction = Weka.classify(mapModel.get(mapSticker.get(ml.getIdentifier()).getType()), Weka.getTest(ml, mapSticker.get(ml.getIdentifier()).getType()));

        ml.setAction(prediction);
        return ml;
    }
}
