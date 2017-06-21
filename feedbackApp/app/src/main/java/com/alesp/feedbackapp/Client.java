package com.alesp.feedbackapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by alesp on 08/03/2017.
 */

public class Client {
    private Socket socket;
    private OutputStream socketOutput;
    private BufferedReader socketInput;

    private String ip;
    private int port;
    private ConnectionListener listener=null;

    Thread checkConnectionThread;

    public Client(String ip, int port){
        this.ip=ip;
        this.port=port;
    }

    public void connect(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                socket = new Socket();
                InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
                try {
                    socket.connect(socketAddress);
                    socketOutput = socket.getOutputStream();
                    socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    //Faccio partire thread che gestisce la ricezione dei dati
                    new ReceiveThread().start();

                    //Faccio partire thread che gestisce il connection check
                   //checkConnectionThread = new Thread(new SendRunnable(true));
                    //checkConnectionThread.start();


                    if(listener!=null)
                        listener.onConnect(socket);
                } catch (IOException e) {
                    if(listener!=null)
                        listener.onConnectError(socket, e.getMessage());
                }
            }
        }).start();
    }

    public void disconnect(){
        try {
            socket.close();
        } catch (IOException e) {
            if(listener!=null)
                listener.onDisconnect(socket, e.getMessage());
        }


    }

    public void send(String message){

        //in questo metodo, per inviare i dati creo un nuovo thread con un runnable custom, che invierà i dati.

        //faccio partire il thread
        new Thread(new SendRunnable(message)).start();

    }

    private class ReceiveThread extends Thread implements Runnable{
        public void run(){
            String message;
            try {
                while((message = socketInput.readLine()) != null) {// each line must end with a \n to be received
                    if(listener!=null)
                        listener.onMessage(message);
                }
            } catch (IOException e) {
                if(listener!=null)
                    listener.onDisconnect(socket, e.getMessage());
            }
        }
    }

    //DEfinisco thread per inviare i dati, che verrà poi startato dal metodo "send" di wakeupservice.
    //in queto caso ci sono 2 thread, uno per la ricezione e uno per l'invio: forse c'è modo di unificarli? investiga

    public void setConnectionListener(ConnectionListener listener){
        this.listener=listener;
    }

    public void removeConnectionListener(){
        this.listener=null;
    }



    private class SendRunnable implements Runnable {

        //definisco il dato da inviare
        private String data;
        boolean isCheckingConnection = false;

        public SendRunnable(String message){
            data=message;
            isCheckingConnection = false;
        }

        public SendRunnable(boolean check){
            data="Connection check";
            isCheckingConnection = check;
        }

        public void run() {

            if (isCheckingConnection) {
                try {
                    while (isCheckingConnection) {
                        Thread.sleep(10000);
                        try {
                            socketOutput.write(data.getBytes());
                        } catch (IOException e) {
                            if (listener != null) {
                                listener.onDisconnect(socket, e.getMessage());
                                isCheckingConnection = false;
                            }

                        }
                    }
                } catch (Exception e) {
                    Log.e("Client", e.toString());
                }
            } else {

                try {
                    socketOutput.write(data.getBytes());
                } catch (IOException e) {
                    if (listener != null)
                        listener.onDisconnect(socket, e.getMessage());
                }
            }
        }
    }

}
