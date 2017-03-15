package com.alesp.feedbackapp;

import java.net.Socket;

/**
 * Created by alesp on 14/03/2017.
 */

//Creo listener customizzato per quando avviene la connessione
public interface ServiceConnectionListener {
    void onConnected();
}