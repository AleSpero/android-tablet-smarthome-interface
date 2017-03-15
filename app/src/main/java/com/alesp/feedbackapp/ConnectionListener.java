package com.alesp.feedbackapp;

import java.net.Socket;

/**
 * Created by alesp on 14/03/2017.
 */

public interface ConnectionListener {
    void onMessage(String message);
    void onConnect(Socket socket);
    void onDisconnect(Socket socket, String message);
    void onConnectError(Socket socket, String message);
}

