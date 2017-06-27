package com.alesp.feedbackapp.MetaWear.stickers.beanInfoSticker;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.core.SerializationHelper;

/**
 * Created by claudio on 19/05/16.
 */
public class SingletonMapModel {

    private static SingletonMapModel instance = null;
    private static Map<String, Classifier> mapModel = null;
    private static BeanInfoSticker infoSticker;

    private SingletonMapModel(Map<String,Classifier> mapModel){
        this.mapModel = mapModel;
    }

    public static synchronized SingletonMapModel getInstance(){
        if(instance == null) {
            mapModel = new HashMap<>();
            infoSticker = SingletonInfoSticker.getInstance().getInfoSticker();
            for (Category category : infoSticker.getCategory()) {
                byte[] modelDecoded = Base64.decode(category.getModel(), Base64.DEFAULT);
                mapModel.put(category.getName(), getClassifierFromModel(modelDecoded));
            }
            instance = new SingletonMapModel(mapModel);
        }
        return instance;
    }

    public Map<String,Classifier> getMapModel(){
        return mapModel;
    }

    private static Classifier getClassifierFromModel(byte[] model) {
        // deserialize model
        Classifier cls = null;
        try {
            /*Create an InputStream object to pass to SerializationHelper.read method.
             *This permit to avoid save all model in a file and read it to create the classifier */
            //InputStream stream = new ByteArrayInputStream(/*infoSticker.getCategory().get(0).getModel().getBytes(StandardCharsets.UTF_8)*/mapModel.get(objType));
            /*Vecchio argomento di read:
             *Environment.getExternalStorageDirectory()+"/Android/"+ objType +".model"*/
            /*FileReader file = new FileReader(Environment.getExternalStorageDirectory() + "/Android/" + model + ".model");
            BufferedReader br = new BufferedReader(file);*/
            //System.out.println("File model: " + br.readLine());
            cls = (Classifier) SerializationHelper.read(new ByteArrayInputStream(model));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cls;
    }
}
