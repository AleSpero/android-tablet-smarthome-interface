package com.alesp.feedbackapp;

/**
 * Created by alesp on 22/03/2017.
 */

public interface ServiceConnectionListener {
    void onConnectionStatusChange();
    void onConnectionStatusChange(String message);

}
