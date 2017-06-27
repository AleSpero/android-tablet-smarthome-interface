package com.alesp.feedbackapp.MetaWear.stickers.beanInfoSticker;

import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.Gson;
import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.GsonBuilder;
import com.example.stefano.myapplication.Utils;

/**
 * Created by claudio on 26/04/16.
 */
public class SingletonInfoSticker {

    private static BeanInfoSticker infoSticker = null;
    private static SingletonInfoSticker instance = null;

    private SingletonInfoSticker(BeanInfoSticker infoSticker){

        this.infoSticker = infoSticker;

    }

    public static synchronized SingletonInfoSticker getInstance(){

        String serverResponse = null;

        if(instance == null) {

            while(serverResponse == null) { //server request -> con server attivo deve essere ==
                serverResponse = Utils.sendRequest("getStickerInformation");
                String response = serverResponse.substring(1, (serverResponse.length() - 1));
                Gson gson = new GsonBuilder().create();
                infoSticker = gson.fromJson(response, BeanInfoSticker.class);
            }

            instance = new SingletonInfoSticker(infoSticker);

        }


        return instance;
    }

    public BeanInfoSticker getInfoSticker(){
        return infoSticker;
    }



}
