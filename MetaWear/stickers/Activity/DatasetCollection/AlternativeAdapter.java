package com.example.stefano.myapplication.stickers.Activity.DatasetCollection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.stefano.myapplication.R;
import com.example.stefano.myapplication.stickers.MyMovement;
import com.example.stefano.myapplication.stickers.MyNearable;

import java.util.ArrayList;

/**
 * Created by Stefano on 04/02/2016.
 */
public class AlternativeAdapter extends ArrayAdapter<MyMovement> {
    ArrayList<MyMovement> arrayMovimenti = new ArrayList<>();
    MyMovement movement;
    int countMov = 0;
    Spinner[] spinner;

    public AlternativeAdapter(Context context, ArrayList<MyMovement> ml) {
        super(context, 0, ml);
        arrayMovimenti = ml;
        spinner = new Spinner[arrayMovimenti.size()];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        movement = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.movement_item, parent, false);
        }

        String med = "";
        if (movement.getIdentifier().equals("52c92e96776e8130")){
            med = "Tacchipirina";
        } else if(movement.getIdentifier().equals("d9c6f6cf98537b2a")){
            med = "Imodium";
        } else if(movement.getIdentifier().equals("17a58090648de051")) {
            med = "Benagol";
        }

        TextView mac = (TextView) convertView.findViewById(R.id.mac);
        TextView time = (TextView) convertView.findViewById(R.id.time);
        TextView durata = (TextView) convertView.findViewById(R.id.durata);

        mac.setText("Medicina: "+med);

        String ora = movement.getDate().getHours() + "";
        if(ora.length() == 1)
            ora = "0" + ora;
        String minuti = movement.getDate().getMinutes() + "";
        if(minuti.length() == 1)
            minuti = "0" + minuti;
        String secondi = movement.getDate().getSeconds() + "";
        if(secondi.length() == 1)
            secondi = "0" + secondi;

        time.setText("Inizio: " + ora + ":" + minuti + ":" + secondi);
        durata.setText("Durata: " + (double)movement.getDurata()/1000+" sec");



        for(MyMovement m : arrayMovimenti){
            if(m.equals(movement)){
                countMov = arrayMovimenti.indexOf(m);
                spinner[countMov] = (Spinner) convertView.findViewById(R.id.spinner);
                spinner[countMov].setId(countMov);
                spinner[countMov].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        int abc = -1;
                        Spinner sp = (Spinner) parent;
                        for(Spinner s : spinner)
                            if(sp.equals(s)) {
                                abc = s.getId();
                            }
                        String azione = (String) sp.getItemAtPosition(position);

                        for (MyMovement tmp : arrayMovimenti) {
                            String act = tmp.getAction();
                            if (arrayMovimenti.indexOf(tmp) == (abc) && abc != -1) {
                                tmp.setAction(azione);
                                for (MyNearable myNearable : tmp.getList())
                                    myNearable.addAction(azione);
                            } else {
                                tmp.setAction(act);
                                for (MyNearable myNearable : tmp.getList())
                                    myNearable.addAction(act);
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        }

        /*if(countMov>arrayMovimenti.size()){
            int tmp = 0;
            for(MyMovement m : arrayMovimenti){
                if(m.equals(movement)){
                    tmp = countMov;
                    countMov = arrayMovimenti.indexOf(m);
                    spinner[countMov] = (Spinner) convertView.findViewById(R.id.spinner);
                    spinner[countMov].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Spinner sp = (Spinner) parent;
                            String azione = (String) sp.getItemAtPosition(position);


                            for (MyMovement tmp : arrayMovimenti) {
                                if (tmp.equals(movement)) {
                                    tmp.setAction(azione);
                                    for (MyNearable myNearable : tmp.getList())
                                        myNearable.addAction(azione);
                                }
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
            }
            countMov = tmp+1;
        } else{
            spinner[countMov] = (Spinner) convertView.findViewById(R.id.spinner);

            spinner[countMov].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Spinner sp = (Spinner) parent;
                    String azione = (String) sp.getItemAtPosition(position);


                    for (MyMovement tmp : arrayMovimenti) {
                        if (tmp.equals(movement)) {
                            tmp.setAction(azione);
                            for (MyNearable myNearable : tmp.getList())
                                myNearable.addAction(azione);
                        }
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            countMov++;
        }*/
        return convertView;
    }
}
