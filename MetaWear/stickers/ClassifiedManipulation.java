package com.alesp.feedbackapp.MetaWear.stickers;

/**
 * Created by Stefano on 11/04/2016.
 */

public class ClassifiedManipulation {
    private String identifier;
    private Long duration;
    private String action;

    public ClassifiedManipulation(MyMovement movement){
        identifier = movement.getIdentifier();
        duration = movement.getDurata();
        action = movement.getAction();
    }

    public String getIdentifier(){
        return identifier;
    }

    public String getAction(){
        return action;
    }

}
