package com.example.stefano.myapplication.stickers.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Nearable;
import com.estimote.sdk.SystemRequirementsChecker;
import com.example.stefano.myapplication.R;

import java.util.List;

/**
 * Created by Stefano on 21/12/2015.
 */

public class RefrigeratedMonitoring extends AppCompatActivity {
    private BeaconManager beaconManager;
    private TextView view;
    private Double[] time = {0.0, 0.0}; // time[0] tempo che sticker sta in frigo, time[1] tempo che sticker sta fuori
    private Double precTemp; // temperatura della rilevazione precedente
    private Long precTime; // time della rrilevazione precedente

    private int pos = 0;
    private Nearable[] lastTenNearable = new Nearable[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        view = (TextView)findViewById(R.id.textView);

        beaconManager = new BeaconManager(this);
    }

    @Override protected void onResume() {
        super.onResume();
        if (SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            startScanning();
        }
    }

    private void startScanning() {
        beaconManager.setNearableListener(new BeaconManager.NearableListener() {

            @Override
            public void onNearablesDiscovered(List<Nearable> list) {
                for(Nearable n : list){
                    if(n.identifier.equals("52c92e96776e8130")){

                        // in lastTenNearable ho gli ultimi dieci nearable
                        int count = 0;
                        for(int i=0; i<10; i++){
                            if(lastTenNearable[i] == null) {
                                lastTenNearable[i] = n;
                                count ++;
                                pos = count + 1;
                                break;
                            }
                        }
                        if(count == 0){ // lastTenNearable era già piena
                            if(pos == 10)
                                pos = 0; // ora pos è l'indirizzo della rilevazione più lontana che verrà sostituita
                            lastTenNearable[pos] = n;
                            pos ++;
                        }

                        String t = "";
                        int posNew = pos - 1;
                        for(int i=pos; i==posNew; i++){
                            if(i == 10)
                                i = 0;
                            t += lastTenNearable[i].temperature + " ";
                        }
                        view.setText(t);

                        if(n.isMoving){
                            if(time[0] == 0.0){
                                view.setText("Durata Not Refrigerated: " + time[1]);
                                time[1] = 0.0;
                            } else if(time[1] == 0.0){
                                view.setText("Durata Refrigerated: " + time[0]);
                                time[0] = 0.0;
                            }
                        } else {
                            if(precTemp != null){
                                if(n.temperature < precTemp){
                                    time[0] += System.currentTimeMillis() - precTime;
                                } else if(n.temperature > precTemp){
                                    time[1] += System.currentTimeMillis() - precTime;
                                } else {
                                    if(time[1] == 0.0 && time[0] != 0.0){
                                        time[0] += System.currentTimeMillis() - precTime;
                                    } else if(time[0] == 0.0 && time[1] != 0.0){
                                        time[1] += System.currentTimeMillis() - precTime;
                                    }
                                }
                            }
                            //Utils.sendObject(new Temperature(n), "temperature");
                        }
                        precTemp = n.temperature;
                        precTime = System.currentTimeMillis();
                    }
                }
            }
        });

        beaconManager.setForegroundScanPeriod(300, 0); // scan period = 300 millisecondi, 0 millisecondi di pausa
        beaconManager.setBackgroundScanPeriod(300, 0);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startNearableDiscovery();
            }
        });
    }

    @Override
    protected void onStop() {
        //beaconManager.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        beaconManager.disconnect();
        super.onDestroy();
    }
}
