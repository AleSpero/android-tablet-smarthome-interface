package com.example.stefano.myapplication.stickers.Activity.DatasetCollection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import com.example.stefano.myapplication.R;
import com.example.stefano.myapplication.stickers.MyMovement;

/**
 * Created by Stefano on 13/01/2016.
 */

public class MovementListAdapter extends BaseAdapter {
    private MovementList mvList;
    private LayoutInflater inflater;

    public MovementListAdapter(Context context, MovementList newMovements) {
        this.inflater = LayoutInflater.from(context);
        this.mvList = new MovementList();
        this.mvList = newMovements;
    }

    @Override
    public int getCount() {
        return mvList.size();
    }

    @Override
    public MyMovement getItem(int position) {
        return mvList.getMap().get(position + 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflateIfRequired(view, position, parent);
        bind(getItem(position), view);
        return view;
    }

    private void bind(MyMovement movement, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String med = "";
        if (movement.getIdentifier().equals("c2a3eb71882ac639")){
            med = "Medicine 1";
        } else if(movement.getIdentifier().equals("0a328889c095886e")){
            med = "Knife";
        } else if(movement.getIdentifier().equals("cdef8898f9f0ef0f")) {
            med = "Water bottle";
        }
        holder.macTextView.setText("Object: " + med);

        String ora = movement.getDate().getHours() + "";
        if(ora.length() == 1)
            ora = "0" + ora;
        String minuti = movement.getDate().getMinutes() + "";
        if(minuti.length() == 1)
            minuti = "0" + minuti;
        String secondi = movement.getDate().getSeconds() + "";
        if(secondi.length() == 1)
            secondi = "0" + secondi;

        holder.timeTextView.setText("Start: " + ora + ":" + minuti + ":" + secondi);
        //holder.timeTextView.setText("Inizio: " + movement.getDate());
        holder.durataTextView.setText("Duration: " + (double) movement.getDurata() / 1000 + " s");

        //holder.spinner.setSelection(getIndex(holder.spinner, movement.getEtichettaTemporanea()));
    }

    private int getIndex(Spinner spinner, String etichetta) {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(etichetta)){
                index = i;
                break;
            }
        }
        return index;
    }

    private View inflateIfRequired(View view, int position, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.movement_item, null);
            view.setTag(new ViewHolder(view));
        }
        return view;
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public void update(MovementList movList) {
        this.mvList = movList;
    }

    static class ViewHolder {
        final TextView macTextView;
        final TextView timeTextView;
        final TextView durataTextView;
        final Spinner spinner;

        ViewHolder(View view) {
            macTextView = (TextView) view.findViewWithTag("mac");
            timeTextView = (TextView) view.findViewWithTag("time");
            durataTextView = (TextView) view.findViewWithTag("durata");
            spinner = (Spinner) view.findViewWithTag("spinner");
        }
    }
}
