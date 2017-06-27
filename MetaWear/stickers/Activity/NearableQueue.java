package com.example.stefano.myapplication.stickers.Activity;

import com.estimote.sdk.Nearable;
import com.example.stefano.myapplication.stickers.MyNearable;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by claudio on 19/05/16.
 */
public class NearableQueue extends LinkedBlockingQueue<MyNearable> {

    private boolean isStill;

    public NearableQueue(){
        super();
        isStill = false;
    }

    @Override
    public boolean add(MyNearable n){

        if(n.getMotion()){

            if(isStill)
                isStill = false;

            super.add(n);
        }
        else{

            if(!isStill) {
                isStill = false;
                super.add(n);
            }
        }

        return true;
    }




}