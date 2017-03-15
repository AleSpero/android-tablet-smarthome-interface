package com.alesp.feedbackapp;

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

                    new ReceiveThread().start();

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
        try {
            socketOutput.write(message.getBytes());
        } catch (IOException e) {
            if(listener!=null)
                listener.onDisconnect(socket, e.getMessage());
        }
    }

    private class ReceiveThread extends Thread implements Runnable{
        public void run(){
            String message;
            try {
                while((message = socketInput.readLine()) != null) {   // each line must end with a \n to be received
                    if(listener!=null)
                        listener.onMessage(message);
                }
            } catch (IOException e) {
                if(listener!=null)
                    listener.onDisconnect(socket, e.getMessage());
            }
        }
    }

    public void setConnectionListener(ConnectionListener listener){
        this.listener=listener;
    }

    public void removeConnectionListener(){
        this.listener=null;
    }

}
