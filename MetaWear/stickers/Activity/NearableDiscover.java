package com.example.stefano.myapplication.stickers.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Nearable;
import com.estimote.sdk.SystemRequirementsChecker;
import com.example.stefano.myapplication.Utils;
import com.example.stefano.myapplication.R;
import com.example.stefano.myapplication.stickers.MyNearable;
import com.example.stefano.myapplication.stickers.Temperature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Stefano on 10/11/2015.
 */

public class NearableDiscover extends AppCompatActivity{
    private TextView view;
    private BeaconManager beaconManager;
    private Map<String, Boolean> mapNearables = new HashMap<String, Boolean>();

    private List<Temperature> changing;
    private List<Temperature> soglie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        view = (TextView) findViewById(R.id.textView);
        beaconManager = new BeaconManager(this);

        changing = new ArrayList<>();
        soglie = new ArrayList<>();
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
                    MyNearable obj = new MyNearable(n);

                    if(n.identifier.equals("52c92e96776e8130"))
                        Utils.sendObject(new Temperature(n), "temperature");

                    // Map
                    if(!mapNearables.containsKey(obj.getId())){ // primo valore di n rilevato
                        mapNearables.put(obj.getId(), obj.getMotion());

                        changing.add(new Temperature(n));
                        soglie.add(new Temperature(n));

                        //Utils.sendObject(new Temperatures(n), "temperature");
                        //Utils.sendObject(obj, "sticker"); // send Sticker's data
                    } else if(mapNearables.containsKey(obj.getId()) && mapNearables.get(obj.getId()) != obj.getMotion()){
                        mapNearables.put(obj.getId(), obj.getMotion());
                        //Utils.sendObject(obj, "sticker");
                    } else if(mapNearables.containsKey(obj.getId()) && obj.getMotion()){
                        mapNearables.put(obj.getId(), obj.getMotion());
                        //Utils.sendObject(obj, "sticker");
                    }

                    Temperature s = null;
                    for (Temperature soglia : soglie){
                        if (soglia.getIdentifier().equals(n.identifier)){
                            s = soglia; // s diventa la soglia con l' id = n.id
                            break;
                        }
                    }

                    for (Temperature tr : changing){
                        if (tr.getIdentifier().equals(n.identifier) && n.identifier.equals("52c92e96776e8130")){
                            view.setText(tr.getRefrigerated().toString() + "  " + n.temperature + "  " + tr.getTemperature() + "  " + s.getTemperature());
                            break;
                        }
                    }

                    for (Temperature tmp : changing){
                        if (tmp.getIdentifier().equals(n.identifier)) {
                            if (n.temperature - s.getTemperature() >= 0.5 && System.currentTimeMillis() - tmp.getTime() < 150000) { // la temperatura è aumentata di 0.5 gradi o più in meno di 5 minuti
                                //Utils.sendObject(tmp.setRefrigerated(n, false), "temperature");
                                for (Temperature soglia : soglie)
                                    if (soglia.getIdentifier().equals(n.identifier))
                                        soglia.update(n);
                                break;
                            } else if (n.temperature - s.getTemperature() <= -0.5 && System.currentTimeMillis() - tmp.getTime() < 150000) { // la temperatura  diminuita di 0.5 gradi o più
                                //Utils.sendObject(tmp.setRefrigerated(n, true), "temperature");
                                for (Temperature soglia : soglie)
                                    if (soglia.getIdentifier().equals(n.identifier))
                                        soglia.update(n);
                                break;
                            } else { // aggiornamento mappa temperature
                                if (tmp.getIdentifier().equals(n.identifier))
                                    if (tmp.getRefrigerated() && n.temperature < s.getTemperature() || !tmp.getRefrigerated() && n.temperature > s.getTemperature())
                                        for (Temperature soglia : soglie)
                                            if (soglia.getIdentifier().equals(n.identifier))
                                                soglia.update(n);
                            }
                        }
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
