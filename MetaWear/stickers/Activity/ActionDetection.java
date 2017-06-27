
package com.example.stefano.myapplication.stickers.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Spinner;
import android.widget.TextView;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Nearable;
import com.estimote.sdk.SystemRequirementsChecker;
import com.example.stefano.myapplication.R;
import com.example.stefano.myapplication.Utils;
import com.example.stefano.myapplication.stickers.Manipulation;
import com.example.stefano.myapplication.stickers.MyMovement;
import com.example.stefano.myapplication.stickers.MyNearable;
import com.example.stefano.myapplication.stickers.Statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Stefano on 10/11/2015.
 */

public class ActionDetection extends AppCompatActivity {
    private TextView viewId;
    private TextView viewMovements;
    private Spinner spinnerMov1;
    private Spinner spinnerMov2;
    private Spinner spinnerMov3;

    private BeaconManager beaconManager;
    private Map<String, Boolean> mapNearables = new HashMap<String, Boolean>();

    private List<Manipulation> mov;
    private int idSt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.sel_movimento);
        viewId = (TextView) findViewById(R.id.textId);
        viewMovements = (TextView) findViewById(R.id.textMovimenti);

        spinnerMov1 = (Spinner) findViewById(R.id.spinner_movimento1);
        spinnerMov2 = (Spinner) findViewById(R.id.spinner_movimento2);
        spinnerMov3 = (Spinner) findViewById(R.id.spinner_movimento3);

        beaconManager = new BeaconManager(this);

        viewMovements.setText("Wait..."); // Wait until first motion field FALSE arrives

        mov = new ArrayList<>();
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

                for (Nearable n : list) {
                    if (n.identifier.equals("52c92e96776e8130") || n.identifier.equals("d9c6f6cf98537b2a") || n.identifier.equals("17a58090648de051")) {
                        //Utils.sendObject(new Temperature(n), "temperature");

                        MyNearable obj = new MyNearable(n);

                        int count = 0;

                        for (Manipulation m : mov) {
                            if (m.getIdentifier().equals(obj.getId())) {
                                count++;
                            }
                        }

                        if (count == 0) {
                            mov.add(new Manipulation(obj));
                        }

                        if (!mapNearables.containsKey(obj.getId())) { // primo valore di n rilevato
                            mapNearables.put(obj.getId(), obj.getMotion());
                            viewMovements.setText("Select your action and then make it!");

                            //String r = Utils.sendObject(obj, "sticker");
                        } else if (mapNearables.containsKey(obj.getId()) && mapNearables.get(obj.getId()) != obj.getMotion()) { // il movimento o è iniziato o è finito
                            mapNearables.put(obj.getId(), obj.getMotion());
                            if (obj.getMotion()) { // motion è passato da false a true quindi è iniziato il movimento
                                for (Manipulation m : mov) {
                                    if (m.getIdentifier().equals(obj.getId())) {
                                        m.addNearable(obj);
                                        break;
                                    }
                                }
                            } else { // motion è passato da true a false quindi è finito il movimento
                                for (Manipulation m : mov) {
                                    if (m.getIdentifier().equals(obj.getId())) {
                                        // Operazioni per aggiungere la durata
                                        List<MyNearable> listN = m.getListNearable();
                                        Long durata = obj.getTime() - listN.get(0).getTime(); // motion false - primo motion true

                                        sendMovement(m, durata, obj.getId()); // sendMoviment invia al server rest il movimento
                                        m.clearListNearable();
                                        break;
                                    }
                                }
                                //String re = Utils.sendObject(obj, "sticker");
                            }
                        } else if (mapNearables.containsKey(obj.getId()) && obj.getMotion()) { // il movimento continua
                            for (Manipulation m : mov) {
                                if (m.getIdentifier().equals(obj.getId())) {
                                    m.addNearable(obj);
                                }
                            }
                            mapNearables.put(obj.getId(), obj.getMotion());
                        }
                    } // fine if (n.identifier.equals("52c92e96776e8130") || ...)
                }
            }
        });

        beaconManager.setForegroundScanPeriod(150, 0); // scan period = 300 millisecondi, 0 millisecondi di pausa
        beaconManager.setBackgroundScanPeriod(150, 0);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback()

        {
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

    private void sendMovement(Manipulation m, long durata, String id) {

        String nameAction = "";
        if(id.equals("52c92e96776e8130")){
            nameAction = spinnerMov1.getSelectedItem().toString();
        }else if(id.equals("d9c6f6cf98537b2a")){
            nameAction = spinnerMov2.getSelectedItem().toString();
        }else{
            nameAction = spinnerMov3.getSelectedItem().toString();
        }

        Statistics stat = new Statistics(m.getListNearable());
        MyMovement movement = new MyMovement(stat, id, durata, m.getListNearable().size(), null);
        movement.setAction(nameAction);
        movement.setStickersId(idSt);

        String re = Utils.sendObject(movement, "sticker/movimento");
        viewMovements.setText("Done");
        idSt++;
    }
}