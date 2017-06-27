package com.alesp.feedbackapp.MetaWear;

import com.mbientlab.metawear.MetaWearBoard;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Sara on 14/12/2016.
 */

/*
classe usata per gestire i dati condivisi tra MetaWearSelectionActivity e MetaWearAsyncTask
*/

public class MetaWearSingleton {

    //Map combines device and model
    private static Map<String,String> mapModelAsync;
    //Map combines address device - metawearboard
    private Map<String, MetaWearBoard> mapMWB = new ConcurrentHashMap <>();
    //Map combines address device - AsyncTask
    private Map<String, MetaWearAsyncTask> mapAsync = new ConcurrentHashMap <>();

    private static MetaWearSingleton instance;

    private MetaWearSingleton(Map<String,String> map){
        this.mapModelAsync = map;
    }

    synchronized public static MetaWearSingleton getInstance(){
        if (instance == null){

            mapModelAsync = new ConcurrentHashMap<>();
            instance = new MetaWearSingleton(mapModelAsync);

        }
        return instance;
    }

    //Return map of the model of every device
    synchronized public Map<String,String> getMapModelAsync(){
        return mapModelAsync;
    }
    //Return map of the MetaWearBoard
    synchronized public Map<String,MetaWearBoard> getMapMWB() {
        return mapMWB;
    }
    //Return the active AsyncTask
    synchronized public Map<String,MetaWearAsyncTask> getMapAsync() {
        return mapAsync;
    }

    synchronized public MetaWearBoard getMWB(String device){
        return mapMWB.get(device);
    }

    synchronized public MetaWearAsyncTask getAsync(String device){
        return mapAsync.get(device);
    }

    //Add item to the mapModelAsync
    synchronized public void addItemModel(String device, String model){
        mapModelAsync.put(device,model);
    }
    //Add item device-MetaWearBoard
    synchronized public void addItemMWB(String device, MetaWearBoard metaWearBoard){
        mapMWB.put(device,metaWearBoard);
    }
    //Add item device-MetaWearAsyncTask
    synchronized public void addItemAsync(String device, MetaWearAsyncTask metaWearAsyncTask){
        mapAsync.put(device,metaWearAsyncTask);
    }

    //Change a value associated to a device in the mapModelAsync
    synchronized public boolean changeItemModel(String device, String model){
        if(mapModelAsync.containsKey(device)){
            mapModelAsync.put(device,model);
            return true;
        }else{
            return false;
        }
    }

    //Remove all elements from the map of model
    synchronized public void removeMapModel(){
        for(String device : mapModelAsync.keySet()){
            mapModelAsync.remove(device);
        }
    }
    //Remove all elements from the map of AsyncTask
    synchronized public void removeMapAsync(){
        for(String device : mapAsync.keySet()){
            mapAsync.remove(device);
        }
    }
    //Remove all elements from the map MetaWearBoard
    synchronized public void removeMapMWB(){
        for(String device : mapMWB.keySet()){
            mapMWB.remove(device);
        }
    }
    //Remove a selected elements from map of AsyncTask
    synchronized public boolean removeAsync(String device){
        if(mapAsync.containsKey(device)){
            mapAsync.remove(device);
            return true;
        }else {
            return false;
        }
    }
    //Check if a specific AsyncTask is in the map
    synchronized public boolean containsAsync(String device){
        if(mapAsync.containsKey(device)){
            return true;
        }else {
            return false;
        }
    }
    //Return the model of the device
    synchronized public String getValue(String device){
        if(mapModelAsync.containsKey(device)){
            return mapModelAsync.get(device);
        }
        return null;
    }

    synchronized public Set<String> getKey(){
        return mapModelAsync.keySet();
    }


    synchronized public String toString(){
        return mapModelAsync.toString();
    }


}
