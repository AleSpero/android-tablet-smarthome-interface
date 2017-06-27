package com.example.stefano.myapplication.stickers.Activity.DatasetCollection;

import com.example.stefano.myapplication.stickers.MyMovement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Stefano on 12/01/2016.
 */

public class MovementList{
    private HashMap<Integer, MyMovement> map;

    public MovementList(){
        map = new HashMap<>();
    }

    public MovementList(HashMap<Integer, MyMovement> m){
        map = m;
    }

    public MovementList add(MovementList m){
        for(int i=1; i<m.size()+1; i++){
            map.put(map.size()+1, m.getMap().get(i));
        }
        return new MovementList(map);
    }

    public MovementList add(MyMovement obj, int contatore){
        map.put(contatore, obj);
        return new MovementList(map);
    }

    public HashMap<Integer, MyMovement> getMap(){
        return map;
    }

    public int size() {
        return map.size();
    }

    public MyMovement removeMovement(int position){
        return map.remove(position);
    }

    public List<MyMovement> getListMyMovements(){
        List<MyMovement> l = new ArrayList<>();
        for(int i=1; i< map.size()+1; i++){
            l.add(map.get(i));
        }
        return l;
    }
}
