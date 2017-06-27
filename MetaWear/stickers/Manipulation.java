package com.alesp.feedbackapp.MetaWear.stickers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Stefano on 24/11/2015.
 */

public class Manipulation {
    private String identifier;
    private List<MyNearable> listNearable;

    public Manipulation(MyNearable n){
        identifier = n.getId();
        listNearable = new ArrayList<>();
    }

    public String getIdentifier(){ return identifier; }

    public void addNearable(MyNearable n){
        listNearable.add(n);
    }

    public List<MyNearable> getListNearable(){
        return listNearable;
    }

    public void clearListNearable(){
        listNearable = new ArrayList<>();
    }

    public long getStartTime(){
        return listNearable.get(0).getTime();
    }

    public long getEndTime(){
        return listNearable.get(listNearable.size()-1).getTime();
    }

    public long getDuration(){
        return getEndTime() - getStartTime();
    }


}
