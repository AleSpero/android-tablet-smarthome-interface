package com.example.stefano.myapplication.stickers.Activity.DatasetCollection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;

import com.example.stefano.myapplication.R;
import com.example.stefano.myapplication.Utils;
import com.example.stefano.myapplication.stickers.MyMovement;
import com.example.stefano.myapplication.stickers.MyNearable;
import com.example.stefano.myapplication.stickers.Statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Stefano on 12/01/2016.
 */

public class ManipulationLabeling extends AppCompatActivity{
    private MovementListAdapter adapter;
    AlternativeAdapter adapter2;
    MovementList mvList;
    ListView list;

    private int idSt = 0;

    Button but;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_mov);

        but = (Button) findViewById(R.id.button);

        Intent mIntent = getIntent();

        HashMap<Integer, MyMovement> mapMov = (HashMap<Integer, MyMovement>) mIntent.getSerializableExtra("Lista_MyMovement");

        // Così non usa la segmentazione
        mvList = new MovementList(mapMov);//SpezzaMovimenti(new MovementList(mapMov));

        adapter = new MovementListAdapter(this, mvList);
        list = (ListView) findViewById(R.id.myListView);
        list.setAdapter(adapter);
        /*list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                for(int i=firstVisibleItem; i<firstVisibleItem+visibleItemCount; i++){
                    MyMovement m = (MyMovement) list.getItemAtPosition(i);
                    String etichetta = m.getEtichettaTemporanea(); // non riesco  a prendere etichetta nuova
                    mvList.getMap().get(i+1).setEtichettaTemporanea(etichetta);
                }
                adapter.update(mvList);
            }
        });
        */

        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String res = inviaAlServer(mvList);

                if (res.equals("IOException")) {
                    showPopup(ManipulationLabeling.this);
                    list.setVisibility(View.INVISIBLE);
                    but.setVisibility(View.INVISIBLE);
                } else {
                    Intent intent = new Intent(ManipulationLabeling.this, StartExp.class);
                    startActivity(intent);
                    ManipulationLabeling.this.finish();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private String inviaAlServer(MovementList mvList) {
        String r = "";

        for (int i = 0; i < mvList.size(); i++) {
            View v = adapter.getViewByPosition(i, list);//adapter2.getView(i, null, list);//adapter.getViewByPosition(i, list);

            Spinner spinner = (Spinner) v.findViewById(R.id.spinner);
            String nameAction = spinner.getSelectedItem().toString();

            //invio movimenti
            MyMovement movement = mvList.getMap().get(i + 1);
            movement.setAction(nameAction);
            movement.setStickersId(idSt);
            r = Utils.sendObject(movement, "sticker/movimento");
            idSt++;
        }
        return r;
    }

    private MovementList SpezzaMovimenti(MovementList movementList) {
        double alfa = 0.7; // più è alto più considera l'ultima rilevazione
        double soglia = 400.00;
        final int numeroCampioni = 3;

        MovementList tmp = new MovementList();
        MovementList ml = new MovementList();
        HashMap<Integer, MyMovement> mapMov = movementList.getMap();
        List<MyNearable> list = new ArrayList<>();

        int count = 1;
        double emaAlt = 0.0;
        MyNearable[] myNearables = new MyNearable[numeroCampioni];

        for(int i=0; i<mapMov.size(); i++){ //per ogni movimento
            MyMovement mov = mapMov.get(i + 1); //prendo il value nella mappa HashMap<Integer, MyMovement>

            List<MyNearable> listNearable = mov.getList(); //ho la lista dei Nearable con movimenti
            for(MyNearable n : listNearable){ // controllo per ogni rilevazione se spezzettare il movimento o no

                MyNearable[] myNearablesTmp = new MyNearable[numeroCampioni];
                for(int k=1; k<numeroCampioni; k++){
                    myNearablesTmp[k] = myNearables[k-1];
                }
                myNearablesTmp[0] = n;
                myNearables = myNearablesTmp;

                double calcAlt = AlternativeCalc(myNearables);

                emaAlt = alfa * calcAlt + (1 - alfa) * emaAlt;

                if(list.isEmpty()){ // se è la prima rilevazione del movimento
                    list.add(n);
                }else {
                    if (emaAlt <= soglia || n.equals(listNearable.get(listNearable.size() - 1))) {
                        list.add(n);
                        if (n.equals(listNearable.get(listNearable.size() - 1))) { // ultima rilevazione del movimento
                            long dur = list.get(list.size() - 1).getTime() - list.get(0).getTime();
                            Statistics stat = new Statistics(list, dur);

                            MyMovement myMovement = new MyMovement(stat, n.getId(), dur, list.size(), list.get(0).getDate());
                            myMovement.addStickers(list);

                            tmp = tmp.add(myMovement, count);
                            count++;
                            list = new ArrayList<>();

                        }
                    } else { // bisogna spezzare il movimento
                        long dur = list.get(list.size() - 1).getTime() - list.get(0).getTime();
                        Statistics stat = new Statistics(list, dur);

                        MyNearable prec = list.get(list.size()-1);

                        MyMovement myMovement = new MyMovement(stat, prec.getId(), dur, list.size(), list.get(0).getDate());
                        myMovement.addStickers(list);

                        tmp = tmp.add(myMovement, count);
                        count++;

                        list = new ArrayList<>();
                        list.add(n);

                        myNearables = new MyNearable[numeroCampioni];
                        emaAlt = 0.0;

                    }
                }
            }
            myNearables = new MyNearable[numeroCampioni];
            emaAlt = 0.0;
        }

        ml = ml.add(ControlloTempoMinimo(tmp));
        return ml;
    }

    private MovementList ControlloTempoMinimo(MovementList ml) {
        long tMinimo = 1000;
        MovementList res = new MovementList();
        int count = 1;

        boolean durataUnioneMovimenti  = false;

        for (int i=1; i<ml.size()+1; i++){
            MyMovement movCorrente;
            MyMovement movSuccessivo;

            if(durataUnioneMovimenti && ml.getMap().get(i + 1) != null) {
                movSuccessivo = ml.getMap().get(i);
                movCorrente = res.removeMovement(count - 1);
                count --;
                durataUnioneMovimenti = false;

            } else {
                movCorrente = ml.getMap().get(i);
                movSuccessivo = ml.getMap().get(i + 1);
            }


            if(movSuccessivo != null) {
                boolean idUguali = movCorrente.getIdentifier().equals(movSuccessivo.getIdentifier());

                long diff = movSuccessivo.getList().get(0).getTime() - movCorrente.getList().get(movCorrente.getList().size()-1).getTime();
                boolean stessoMovimentoOriginario = diff < 300;
                boolean movimentoDurataZero = movCorrente.getDurata() == (long) 0 || movSuccessivo.getDurata() == (long) 0;
                boolean movimentoCorto = movCorrente.getDurata() < tMinimo || movSuccessivo.getDurata() < tMinimo;
                boolean ultimoMovimento = i != ml.size();

                if (idUguali && stessoMovimentoOriginario && (movimentoDurataZero || movimentoCorto) && ultimoMovimento) {
                    res = res.add(joinMovements(movCorrente, movSuccessivo), count);
                    count++;
                    i++;
                    durataUnioneMovimenti = movCorrente.getDurata() + movSuccessivo.getDurata() < tMinimo;
                } else {
                    res = res.add(movCorrente, count);
                    count++;
                }
            } else {
                res = res.add(movCorrente, count);
                count++;
            }
        }

        return res;
    }

    private MyMovement joinMovements(MyMovement a, MyMovement b) {
        List<MyNearable> ln = a.getList();
        ln.addAll(b.getList());
        Statistics stat = new Statistics(ln, ln.get(ln.size()-1).getTime()-ln.get(0).getTime());
        MyMovement ret = new MyMovement(stat, ln.get(0).getId(), ln.get(ln.size()-1).getTime()-ln.get(0).getTime(), ln.size(), a.getDate());
        ret.addStickers(ln);

        return ret;
    }

    private static double Calc(double x, double y, double z, double x2, double y2, double z2) {
        double a = x-x2;
        double b = y-y2;
        double c = z-z2;

        double powA = Math.pow((a), 2);
        double powB = Math.pow((b), 2);
        double powC = Math.pow((c), 2);

        double result = Math.sqrt(powA + powB + powC);

        return result;
    }

    private double AlternativeCalc(MyNearable[] myNearables) {
        double sum = 0.0;

        int size = myNearables.length;

        for(int i=1; i<size; i++){
            MyNearable a = myNearables[i-1]==null ? null : myNearables[i-1];
            MyNearable b = myNearables[i]==null ? null : myNearables[i];

            double x = a==null ? 0.0 : a.getX();
            double y = a==null ? 0.0 : a.getY();
            double z = a==null ? 0.0 : a.getZ();

            double x2 = b==null ? 0.0 : b.getX();
            double y2 = b==null ? 0.0 : b.getY();
            double z2 = b==null ? 0.0 : b.getZ();

            sum += Calc(x, y, z, x2, y2, z2);
        }

        return sum/size;
    }

    // The method that displays the popup.
    private void showPopup(final Activity context) {
        int popupWidth = 500;
        int popupHeight = 1000;

        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup, viewGroup);

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(context);
        popup.setContentView(layout);
        popup.setWidth(context.findViewById(R.id.aaa).getWidth());
        popup.setHeight(context.findViewById(R.id.aaa).getHeight());
        popup.setFocusable(true);

        // Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
        int OFFSET_X = 30;
        int OFFSET_Y = 30;

        // Clear the default translucent background
        popup.setBackgroundDrawable(new BitmapDrawable());

        // Displaying the popup at the specified location, + offsets.
        int[] location = new int[2];
        ListView lv = (ListView) findViewById(R.id.myListView);

        // Get the x, y location and store it in the location[] array
        // location[0] = x, location[1] = y.
        lv.getLocationOnScreen(location);

        //Initialize the Point with x, and y positions
        Point p = new Point();
        p.x = location[0];
        p.y = location[1];
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);

        // Getting a reference to Close button, and close the popup when clicked.
        Button close = (Button) layout.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popup.dismiss();
                list.setVisibility(View.VISIBLE);
                but.setVisibility(View.VISIBLE);
            }
        });
    }

}
