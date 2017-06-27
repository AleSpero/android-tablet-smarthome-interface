package com.alesp.feedbackapp.MetaWear;
import com.alesp.feedbackapp.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.stefano.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends ArrayAdapter<String> {

    private static String DEVICE;
    private static String MODEL;
    private static boolean SEND = false;
    private ArrayList<String> list;
    private int layout;
    private Context context;
    //private Map<String, Sticker> mapSticker = SingletonMapSticker.getInstance().getMapSticker();
    //private Map<String, Classifier> mapModel = SingletonMapModel.getInstance().getMapModel();
    private volatile String model = "medicine";
    private boolean sendToServer = false;




    public ListAdapter(Context context, int resource, ArrayList<String> objects) {
        super(context, resource, objects);
        context = getContext();
        layout = resource;
        list = objects;
    }


    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder = new ViewHolder();
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.row_item, null);
            viewHolder.button_start = (Button) view.findViewById(R.id.start_button);
            viewHolder.button_stop = (Button) view.findViewById(R.id.stop_button);
            viewHolder.device = (TextView) view.findViewById(R.id.list_item_string);
            viewHolder.model = (TextView) view.findViewById(R.id.model);
            viewHolder.temperature = (TextView) view.findViewById(R.id.temperature);
            viewHolder.spinner = (Spinner) view.findViewById(R.id.spinner_model); //spinner dei modelli

            viewHolder.device.setText(list.get(position));
            List<String> model = new ArrayList<>();
            model.add("medicine");
            model.add("bottle");
            ArrayAdapter<String> array_spinner = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, model);
            viewHolder.spinner.setAdapter(array_spinner);
            viewHolder.aSwitch = (Switch) view.findViewById(R.id.switch1);
            view.setTag(viewHolder);

        }else
        {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                model = parent.getItemAtPosition(position).toString();
                MODEL = model;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //model = "medicine";
            }
        });

        viewHolder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendToServer = isChecked;
                SEND = true;
                //
            }
        });

        final ViewHolder finalViewHolder = viewHolder;
        viewHolder.button_start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(), "start - Button was clicked for list item " + position, Toast.LENGTH_SHORT).show();
                DEVICE = list.get(position);
                //avvio la modalità in background e lo aggiunto alla lista dei background attivi

                finalViewHolder.button_start.setVisibility(View.GONE);

                //task.doInBackground();
                notifyDataSetChanged();


            }
        });

        viewHolder.button_stop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(), "delete - Button was clicked for list item " + position, Toast.LENGTH_SHORT).show();
                //do something
                String tempDevice = list.get(position);

                finalViewHolder.button_start.setVisibility(View.VISIBLE);
                notifyDataSetChanged();
            }
        });


        return view;

    }

    public static String getModelSelected(){
        return MODEL;
    }

    public static String getDeviceSelected(){
        return DEVICE;
    }

    public static boolean getSendSelected(){
        return SEND;
    }


    static class ViewHolder
    {
        Button button_start;
        Button button_stop;
        TextView device;
        TextView model;
        TextView temperature;
        Spinner spinner;
        Switch aSwitch;
    }

    /*private void isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        services = manager.getRunningServices(Integer.MAX_VALUE);
        List<ActivityManager.RunningServiceInfo> active_service = manager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : active_service) {
            if ((runningServiceInfo.service.compareTo(ComponentName.unflattenFromString("com.example.stefano.myapplication/com.example.stefano.myapplication.MetaWear.MetaWearService")) == 0)){
                services.add(runningServiceInfo);
            }
        }

    }*/
}
