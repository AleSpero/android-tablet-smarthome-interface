package com.example.stefano.myapplication.stickers.Activity.DatasetCollection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Nearable;
import com.estimote.sdk.SystemRequirementsChecker;
import com.example.stefano.myapplication.R;
import com.example.stefano.myapplication.stickers.Manipulation;
import com.example.stefano.myapplication.stickers.MyMovement;
import com.example.stefano.myapplication.stickers.MyNearable;
import com.example.stefano.myapplication.stickers.Statistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Stefano on 12/01/2016.
 */

public class ManipulationsAcquisition extends AppCompatActivity {
    private BeaconManager beaconManager;
    private Map<String, Boolean> mapNearables = new HashMap<String, Boolean>();

    private List<Manipulation> mov;

   // private int idSt = 0;

    MovementList mvList = new MovementList();
    int contatore = 1;
    TextView t;
    TextView textHour;
    TextView numeroMovimenti;
    TextView motionTacchipirina;
    TextView motionImodium;
    TextView motionBenagol;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.esp_medicine);

        t = (TextView) findViewById(R.id.textView5);
        textHour = (TextView) findViewById(R.id.textHour);

        numeroMovimenti = (TextView) findViewById(R.id.textView_numeroMovimenti);
        motionTacchipirina = (TextView) findViewById(R.id.motion_tacchi);
        motionImodium = (TextView) findViewById(R.id.motion_imodium);
        motionBenagol = (TextView) findViewById(R.id.motion_benagol);

        mov = new ArrayList<>();

        button = (Button) findViewById(R.id.button_fine);
        button.setVisibility(View.INVISIBLE);

        beaconManager = new BeaconManager(ManipulationsAcquisition.this);
        t.setText("Waiting for a manipulation.");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<Integer, MyMovement> hashMap = mvList.getMap();
                //HashMap<Integer, List<MyNearable>> mapSt = stList.getMap();

                Intent intent = new Intent(ManipulationsAcquisition.this, ManipulationLabeling.class);
                //intent.putExtra("Lista_MyNearable", mapSt);
                intent.putExtra("Lista_MyMovement", hashMap);
                startActivity(intent);
                ManipulationsAcquisition.this.finish();
                beaconManager.disconnect();

            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        if (SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            startScanning();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        beaconManager.disconnect();
        super.onDestroy();
    }

    private void startScanning() {
        beaconManager.setNearableListener(new BeaconManager.NearableListener() {

            @Override
            public void onNearablesDiscovered(List<Nearable> list) {
                textHour.setText(new Date().toString());
                for (Nearable n : list){
                    if (n.identifier.equals("c2a3eb71882ac639") || n.identifier.equals("0a328889c095886e") || n.identifier.equals("cdef8898f9f0ef0f")) {
                        MyNearable obj = new MyNearable(n);

                        if(n.identifier.equals("c2a3eb71882ac639"))
                            motionTacchipirina.setText(obj.getMotion()+"\n0");
                        if(n.identifier.equals("0a328889c095886e"))
                            motionImodium.setText(obj.getMotion()+"\n0");
                        if(n.identifier.equals("cdef8898f9f0ef0f"))
                            motionBenagol.setText(obj.getMotion()+"\n0");

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

                        } else if (mapNearables.get(obj.getId()) != obj.getMotion()) { // il movimento o è iniziato o è finito
                            mapNearables.put(obj.getId(), obj.getMotion());
                            if (obj.getMotion()) { // motion è passato da false a true quindi è iniziato il movimento
                                for (Manipulation m : mov) {
                                    if (m.getIdentifier().equals(obj.getId())) {
                                        m.addNearable(obj);
                                    }
                                }
                            } else { // motion è passato da true a false quindi è finito il movimento
                                for (Manipulation m : mov) {
                                    if (m.getIdentifier().equals(obj.getId())) {
                                        // Operazioni per aggiungere la durata
                                        Long durata = m.getListNearable().get(m.getListNearable().size()-1).getTime() - m.getListNearable().get(0).getTime(); // ultimo motion false - primo motion true

                                        addMovement(m, durata, obj.getId()); // sendMoviment invia al server rest il movimento
                                        m.clearListNearable();
                                    }
                                }
                            }
                        } else if (obj.getMotion()) { // il movimento continua e il valore precedente di motion era sempre true
                            for (Manipulation m : mov) {
                                if (m.getIdentifier().equals(obj.getId())) {

                                    // Segmentazione

                                    //if(n.identifier.equals("52c92e96776e8130") && !m.getListNearable().isEmpty())
                                    if(n.identifier.equals("c2a3eb71882ac639") && !m.getListNearable().isEmpty())
                                        motionTacchipirina.setText(obj.getMotion()+"\n"+(m.getListNearable().get(m.getListNearable().size()-1).getTime() - m.getListNearable().get(0).getTime()));
                                    if(n.identifier.equals("0a328889c095886e") && !m.getListNearable().isEmpty())
                                        motionImodium.setText(obj.getMotion()+"\n"+(m.getListNearable().get(m.getListNearable().size()-1).getTime() - m.getListNearable().get(0).getTime()));
                                    if(n.identifier.equals("cdef8898f9f0ef0f") && !m.getListNearable().isEmpty())
                                        motionBenagol.setText(obj.getMotion()+"\n"+(m.getListNearable().get(m.getListNearable().size()-1).getTime() - m.getListNearable().get(0).getTime()));

                                    m.addNearable(obj);
                                }
                            }
                        }
                    } // end if (n.identifier.equals......)
                } // end for (Nearable n : list)
            } // end onNearableDiscovered
        });

        beaconManager.setForegroundScanPeriod(150,0); // scan period = 300 millisecondi, 0 millisecondi di pausa
        beaconManager.setBackgroundScanPeriod(150,0);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback(){
            @Override
            public void onServiceReady() {
                beaconManager.startNearableDiscovery();
            }
        });
    }

    private void addMovement(Manipulation m, Long durata, String id) {

        Statistics stat = new Statistics(m.getListNearable(), durata);
        MyMovement movement = new MyMovement(stat, id, durata, m.getListNearable().size(), null);

        StickersList stList = new StickersList();
        for(MyNearable myNearable : m.getListNearable()){
            stList.add(myNearable, contatore);
        }
        movement.addStickers(stList.getList());

        mvList.add(movement, contatore);
        button.setVisibility(View.VISIBLE);
        numeroMovimenti.setVisibility(View.VISIBLE);
        t.setText("First manipulation received. Click the button to stop.");
        numeroMovimenti.setText("Manipulations received: "+contatore);
        contatore ++;
    }
}
