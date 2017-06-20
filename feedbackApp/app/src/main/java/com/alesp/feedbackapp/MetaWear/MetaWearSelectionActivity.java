package com.alesp.feedbackapp.MetaWear;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
/**
 * Created by civi on 07/09/16.
 * Edited by Sara on 08/11/2016
 */

/*
La classe si occupa del rilevamento dei MetaWear nell'ambiente circostante, instaurando con essi una connessione.
*/
public class MetaWearSelectionActivity extends ListActivity implements ServiceConnection {
    private ListAdapter listAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private MetaWearBleService.LocalBinder localBinder;
    //Map combines address device - metawearboard
    //private Map<String, MetaWearBoard> mapMWB = new HashMap<>();
    //Map combines address device - AsyncTask
    //private Map<String, MetaWearAsyncTask> mapAsync = new HashMap<>();

    // Stops scanning after 3 seconds.
    private static final long SCAN_PERIOD = 3000;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getActionBar();
        mHandler = new Handler();

        // Initializes a Bluetooth adapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class), this, Context.BIND_AUTO_CREATE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //This method controls the Bluetooth scanning
    private void scanLeDevice(final boolean enable) {

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    //setListAdapter(listAdapter);
                    if(!mBluetoothAdapter.isDiscovering() && listAdapter.mLeDevices.isEmpty()){
                        Toast.makeText(MetaWearSelectionActivity.this,"No MetaWear found", Toast.LENGTH_SHORT).show();
                        Intent back= new Intent(MetaWearSelectionActivity.this, MainActivity.class);
                        startActivity(back);

                    }
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);

        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            //setListAdapter(listAdapter);

        }
        /*if(!mBluetoothAdapter.isDiscovering() && listAdapter.mLeDevices.isEmpty()){
            Toast.makeText(this,"No MetaWear found", Toast.LENGTH_SHORT).show();
            Intent back= new Intent(MetaWearSelectionActivity.this, MainActivity.class);
            startActivity(back);

        }*/
    }

    protected void onResume() {
        super.onResume();
        // Initializes list view adapter.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        listAdapter = new ListAdapter();
        setListAdapter(listAdapter);
        //Begin scanning for LE devices
        scanLeDevice(true);
    }

    protected void onPause() {
        super.onPause();
        //Stop scanning
        listAdapter.clear();
        scanLeDevice(false);
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listAdapter.addDevice(device);
                            listAdapter.notifyDataSetChanged();

                        }
                    });
                }
            };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unbindService(this);

        MetaWearSingleton.getInstance().removeMapModel();

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        localBinder = (MetaWearBleService.LocalBinder) service;
        localBinder.executeOnUiThread();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    //Adapter that creates a Listview formed by row. A row for a MetaWear found
    private class ListAdapter extends BaseAdapter{
        //check controls if model is correct
        private boolean check = true;
        private String DEVICE;
        private String model = "medicine";
        private boolean sendToServer = false;
        //private MultiChannelTemperature temperatureModule = null;
        //private List<MultiChannelTemperature.Source> tempSources = null;

        //List of devices scanned by Bluetooth
        private ArrayList<BluetoothDevice> mLeDevices;

        //List to control model runtime
        private Map<String,Boolean> mapStart = new ConcurrentHashMap<>();

        private LayoutInflater mInflator;
        private ProgressDialog dialog;


        public ListAdapter(){
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = MetaWearSelectionActivity.this.getLayoutInflater();
        }

        //Every MetaWear found is added to the list of devices (mLeDevices)
        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)&&(device.getName()!=null)&&(device.getName().equals("MetaWear"))) {
                mLeDevices.add(device);
                MetaWearSingleton.getInstance().addItemModel(device.getAddress(),"medicine");
                mapStart.put(device.getAddress(),false);
            }
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //Empty the list mLeDevices
        public void clear() {
            mLeDevices.clear();
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder viewHolder;

            //Create the row
            if (view == null) {
                viewHolder = new ViewHolder();
                view = mInflator.inflate(R.layout.row_item, null);
                viewHolder.button_start = (Button) view.findViewById(R.id.start_button);
                viewHolder.button_stop = (Button) view.findViewById(R.id.stop_button);
                viewHolder.device = (TextView) view.findViewById(R.id.list_item_string);
                viewHolder.model = (TextView) view.findViewById(R.id.model);
                //togliere il commento quando si implementa la rilevazione temperatura
                //viewHolder.temperature = (TextView) view.findViewById(R.id.temperature);
                viewHolder.spinner = (Spinner) view.findViewById(R.id.spinner_model); //spinner dei modelli
                viewHolder.aSwitch = (Switch) view.findViewById(R.id.switch1);
                view.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) view.getTag();
            }

            final ViewHolder finalViewHolder = viewHolder;
            DEVICE = null;
            DEVICE = mLeDevices.get(position).getAddress();
            viewHolder.device.setText(DEVICE);



            viewHolder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    model = parent.getItemAtPosition(pos).toString();
                    DEVICE = mLeDevices.get(position).getAddress();
                    check = false;
                    //used to change model in the Map in MetaWearSingleton
                    changeModelSingleton(DEVICE);
                    //IF statement allows to change model's device runtime in MetaWearAsyncTask
                    if((mapStart.get(DEVICE)) == true){
                        MetaWearAsyncTask async = MetaWearSingleton.getInstance().getAsync(DEVICE);
                        async.changeModel(model);
                        System.out.println("Model change: " + DEVICE + " "+MetaWearSingleton.getInstance().getMapModelAsync());
                    }


                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            viewHolder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    sendToServer = isChecked;
                }
            });

            //Start background process with AsyncTask and AsyncTask is added to the mapAsync
            viewHolder.button_start.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                    dialog = new ProgressDialog(MetaWearSelectionActivity.this);
                    //dialog.setCanceledOnTouchOutside(false);

                    finalViewHolder.button_start.setVisibility(View.INVISIBLE);

                    //Get MetaWearBoard of the MetaWear
                    final MetaWearBoard mwb = localBinder.getMetaWearBoard(mLeDevices.get(position));
                    DEVICE = mLeDevices.get(position).getAddress();
                    MetaWearSingleton.getInstance().addItemMWB(DEVICE,mwb);
                    //mapMWB.put(DEVICE, mwb);

                    dialog.setTitle("MetaWear " + DEVICE);
                    dialog.setMessage("Connecting...");
                    dialog.setCancelable(false);
                    dialog.show();

                    if(mwb.isConnected()){
                        mwb.disconnect();
                    }

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Lollipop and above: attend to connect
                            //Need some moments to connect or reconnect
                            mwb.setConnectionStateHandler(new MetaWearBoard.ConnectionStateHandler() {
                                @Override
                                public void connected() {

                                    dialog.dismiss();

                                    Toast.makeText(getApplicationContext(), "MetaWear " + DEVICE + " connected!", Toast.LENGTH_SHORT).show();

                                    finalViewHolder.button_stop.setVisibility(View.VISIBLE);

                                    //IF statement controls if model is changed
                                    if (check) { //model is not changed
                                        model = MetaWearSingleton.getInstance().getValue(DEVICE);

                                    } else { //model is changed
                                        MetaWearSingleton.getInstance().changeItemModel(DEVICE, model);
                                    }
                                    //asyncTask is started so the mapStart associated to device needs to change
                                    mapStart.put(DEVICE, true);

                                    System.out.println("mapModel: " + MetaWearSingleton.getInstance().getMapModelAsync());

                                    MetaWearAsyncTask mwat = new MetaWearAsyncTask(getApplicationContext(), model, sendToServer, mwb);

                                    MetaWearSingleton.getInstance().addItemAsync(DEVICE, mwat);
                                    //mapAsync.put(DEVICE, mwat);
                                    System.out.println("mapMWB: " + MetaWearSingleton.getInstance().getMapMWB());
                                    System.out.println("mapAsync: " + MetaWearSingleton.getInstance().getMapAsync());

                                    //Start AsyncTask
                                    mwat.execute();

                                    //reset fields
                                    DEVICE = null;
                                    model = null;
                                    check = true;
                                }

                                @Override
                                public void disconnected(){

                                    mwb.disconnect();

                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            //Lollipop and above: attend to connect
                                            //Need some moments to connect or reconnect
                                            finalViewHolder.button_start.setVisibility(View.VISIBLE);
                                        }
                                    }, 1000);
                                }

                                @Override
                                public void failure(int status, Throwable error) {
                                    Log.e("MetaWearSelectionAct", "Error connecting", error);
                                    dialog.dismiss();
                                    model = null;
                                    check = true;
                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mwb.disconnect();
                                        }
                                    },100);

                                    if(error instanceof RuntimeException){ //Caused by BluetoothGatt
                                        Toast.makeText(getApplicationContext(),"Bluetooth error. Retry...",Toast.LENGTH_SHORT).show();
                                    }

                                    if(error instanceof TimeoutException){ //Caused by MetaWearBoard
                                        Toast.makeText(getApplicationContext(),"MetaWearBoard error. Retry...",Toast.LENGTH_SHORT).show();
                                    }

                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            //Lollipop and above: attend to connect
                                            finalViewHolder.button_start.setVisibility(View.VISIBLE);
                                        }
                                    }, 1000);

                                }
                            });
                            //mwb.connect();
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Lollipop and above: attend to connect
                                    //Need some moments to connect or reconnect
                                    mwb.connect();
                                }
                            }, 300);
                        }
                    }, 500);
                }
            });

            //Stop AsyncTask and disconnect Metawearboard
            viewHolder.button_stop.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                    String tempDevice = mLeDevices.get(position).getAddress();
                    final MetaWearBoard mwb = MetaWearSingleton.getInstance().getMWB(tempDevice);
                    //final MetaWearBoard mwb = mapMWB.get(tempDevice);

                    if(MetaWearSingleton.getInstance().containsAsync(tempDevice)){
                        MetaWearAsyncTask tempAync = MetaWearSingleton.getInstance().getAsync(tempDevice);
                        //MetaWearAsyncTask tempAync = mapAsync.get(tempDevice);
                        //Stop the AsyncTask
                        tempAync.cancel(true);
                        if(tempAync.isCancelled()){
                            MetaWearSingleton.getInstance().removeAsync(tempDevice);
                            //mapAsync.remove(tempDevice); //remove AyncTask from the map
                        }
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Lollipop and above: attend to connect
                                //Need some moments to connect or reconnect
                                mwb.disconnect();
                                Toast.makeText(getApplicationContext(),"MetaWear " + DEVICE + " disconnected!",Toast.LENGTH_SHORT).show();
                                finalViewHolder.button_start.setVisibility(View.VISIBLE);
                                finalViewHolder.button_stop.setVisibility(View.INVISIBLE);
                            }
                        }, 500);
                    }
                    System.out.println("mapModel: " + MetaWearSingleton.getInstance().getMapModelAsync());
                    notifyDataSetChanged();
                }
            });



            return view;


        }

        /*private void temperatureSampling(String device){


            final MetaWearBoard metaWearBoard = MetaWearSingleton.getInstance().getMWB(device);

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {

                    try {
                        temperatureModule = metaWearBoard.getModule(MultiChannelTemperature.class);
                        temperatureModule.routeData().fromSource(tempSources.get(MultiChannelTemperature.MetaWearRChannel.NRF_DIE)).stream("temperature_stream").commit().
                                onComplete(new AsyncOperation.CompletionHandler<RouteManager>(){

                                    @Override
                                    public void success(RouteManager result){


                                        result.subscribe("temperature_stream", new RouteManager.MessageHandler(){


                                            @Override
                                            public void process(Message msg){

                                                TextView textTemp = (TextView) findViewById(R.id.temperature);
                                                temperatureModule.readTemperature(tempSources.get(MultiChannelTemperature.MetaWearRChannel.NRF_DIE));
                                                textTemp.setText("Temperature: " + temperatureModule + "°");
                                                notifyDataSetChanged();

                                            }

                                        });

                                    }

                                });

                    } catch (UnsupportedModuleException e) {
                        e.printStackTrace();
                    }

                }
            }, 500);


        };*/

        private boolean changeModelSingleton(String device){
            //IF statement controls if model is changed
            if (check == false){ //model is changed
                MetaWearSingleton.getInstance().changeItemModel(DEVICE, model);
                return true;
            }else {
                return false;
            }
        }







    }
    static private class ViewHolder {
        Button button_start;
        Button button_stop;
        TextView device;
        TextView model;
        TextView temperature;
        Spinner spinner;
        Switch aSwitch;
    }


}