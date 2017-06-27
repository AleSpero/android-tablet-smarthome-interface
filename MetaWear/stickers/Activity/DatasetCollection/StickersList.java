package com.example.stefano.myapplication.stickers.Activity.DatasetCollection;

import com.example.stefano.myapplication.stickers.MyNearable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Stefano on 12/01/2016.
 */

public class StickersList {
    private List<MyNearable> listStickers;
    private HashMap<Integer, List<MyNearable>> map;

    public StickersList(){
        map = new HashMap<>();
        listStickers = new ArrayList<>();
    }

    StickersList(HashMap<Integer, List<MyNearable>> m){
        map = m;
    }

    HashMap<Integer, List<MyNearable>> getMap(){
        return map;
    }

    public void add(MyNearable obj, int contatore){
        listStickers.add(obj);
        map.put(contatore, listStickers);
    }

    void clearList(){
        listStickers = new ArrayList<>();
    }

    public List<MyNearable> getList(){
        return listStickers;
    }

    int size(){
        return map.size();
    }
}
