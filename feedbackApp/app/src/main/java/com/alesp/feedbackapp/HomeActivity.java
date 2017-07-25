package com.alesp.feedbackapp;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.mikepenz.crossfader.Crossfader;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.MiniDrawer;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableBadgeDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondarySwitchDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.mikepenz.materialize.util.UIUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v4.app.FragmentActivity;


/**
 * Created by alesp on 31/03/2017.
 */

public class HomeActivity extends FragmentActivity {

    //Definisco il mio service e il boolean boundtoactivity per indicare se il processo
    // è collegato all'activity
    private WakeUpService wakeService;
    private boolean wakeupBoundToActivity = false;

    //variabile che gestisce visibilità webview
    boolean webviewVisibleBefore = false;

    //variabili per notifiche
    NotificationManager notificationmanager;
    Notification notifica;
    int NOTIFICATION_SERVICE_RUNNING_ID = 0;

    private ServiceConnection wakeupConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v("onServiceConnected", "Service WakeUpService connesso!");


            Toast.makeText(HomeActivity.this, "Activity recognition service started!", Toast.LENGTH_SHORT).show();

            //creo notifica permanente
            showNotification();

            //Setto il flag boundtoprocess = true
            wakeupBoundToActivity = true;

            //Effettuo il collegamento (giusto?)
            WakeUpService.WakeUpBinder binder = (WakeUpService.WakeUpBinder) service;
            wakeService = binder.getService();

            //Controllo che il service sia connesso all'activity, e una volta fatto ciò connetto e ricevo i dati
            if (wakeupBoundToActivity) {


                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {


                        if (!wakeService.isConnected()) {
                            //creo alertdialog e faccio terminare il servizio
                            new AlertDialog.Builder(HomeActivity.this)
                                    .setTitle("Connection not available")
                                    .setMessage("Couldn't connect to the server.\nPlease try later.")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            disconnectService();
                                            getFragmentManager().beginTransaction().detach(firstFragment);
                                            findViewById(R.id.fragmentcontainer).setVisibility(View.GONE);
                                        }
                                    })
                                    .show()
                                    .setCanceledOnTouchOutside(false);


                        }
                    }
                }, 300);

            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v("onServiceDisonnected", "Service WakeUpService disconnesso!");
            wakeupBoundToActivity = false;
        }
    };

    //Variabile che gestisce il drawer
    Drawer result;
    MiniDrawer miniResult;

    //variabile che gestisce lo scambio tra drawer piccolo e drawer grande
    Crossfader crossFader;
    CrossfadeWrapper crossfaderWrapper;

    SharedPreferences pref;
    SharedPreferences.Editor edit;

    //gestisco costanti per gestire gli elementi del drawer
    final static int CONTROL_PANEL = 0;
    final static int ACTIVITY_RECOGNITION = 2;
    final static int DASHBOARD = 3;
    final static int SENSOR_DATA = 4;
    final static int LOG = 5;
    final static int SETTINGS_DUMMY = 7;
    final static int SETTINGS = 8;
    final static int ABOUT = 9;

    //Inizializzo variabili webview
    WebView webView; //abbastanza autoesplicativo
    String url = "159.149.152.241";
    ProgressDialog progress;

    //fragmento activityrecognition
    ActivityRecognitionFragment firstFragment;



    boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        //creo dummy
        final PrimaryDrawerItem dummy = new PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_settings)
                .withIdentifier(7)
                .withName("remove");

        //memorizzo un draweritem in una variable poichè poi devo cambiare l'icona (control panel)
        final PrimaryDrawerItem controlpanel = new PrimaryDrawerItem().withName("Control Panel")
                .withIcon(GoogleMaterial.Icon.gmd_menu)
                .withIdentifier(CONTROL_PANEL)
                .withSelectable(true);

        final PrimaryDrawerItem about = new PrimaryDrawerItem()
                .withName(getString(R.string.about))
                .withIcon(GoogleMaterial.Icon.gmd_info)
                .withIdentifier(ABOUT);

        //CREAZIONE DRAWER
        result = new DrawerBuilder()
                .withActivity(this)
                .addDrawerItems(
                        //aggiungo elementi al mio Drawer
                        controlpanel,
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("Activity Recognition").withIcon(GoogleMaterial.Icon.gmd_directions_walk).withIdentifier(ACTIVITY_RECOGNITION),
                        new PrimaryDrawerItem().withName("Sensor Data").withIcon(FontAwesome.Icon.faw_bar_chart).withIdentifier(SENSOR_DATA),
                        new PrimaryDrawerItem().withName("Dashboard").withIcon(GoogleMaterial.Icon.gmd_dashboard).withIdentifier(DASHBOARD),
                        new PrimaryDrawerItem().withName("Log").withIcon(FontAwesome.Icon.faw_file_text_o).withIdentifier(LOG),
                        //Setto collapsable per i settings
                        new ExpandableDrawerItem().withName(getString(R.string.settings)).withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(SETTINGS).withSelectable(false).withSubItems(
                                new SecondarySwitchDrawerItem().withName("Enable Voice Recognition").withIcon(GoogleMaterial.Icon.gmd_mic).withChecked(true).withOnCheckedChangeListener(onCheckedChangeListener)
                                //new SecondaryDrawerItem().withName("CollapsableItem 2").withLevel(2).withIcon(GoogleMaterial.Icon.gmd_8tracks).withIdentifier(2001)

                        ),
                        //Setto dummy per i settings (che rimuovo in automatico durante il crossfade
                        dummy,
                        about)
                        .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {
                           // Toast.makeText(MiniDrawerActivity.this, ((Nameable) drawerItem).getName().getText(MiniDrawerActivity.this), Toast.LENGTH_SHORT).show();
                        }
                        //Log.d("position",position+"");
                        switch (position){

                            case CONTROL_PANEL:
                                crossFader.crossFade();
                                result.setSelection(-1);
                                break;

                            case ACTIVITY_RECOGNITION:

                                //Faccio partire il service e faccio comparire il fragment

                                if(!wakeupBoundToActivity) {
                                    Intent servIntent = new Intent(HomeActivity.this, WakeUpService.class);
                                    bindService(servIntent, wakeupConnection, Context.BIND_AUTO_CREATE);
                                    wakeupBoundToActivity = true;
                                }

                                //Inflato il fragment


                                if(findViewById(R.id.fragmentcontainer).getVisibility() == View.GONE){
                                    findViewById(R.id.fragmentcontainer).setVisibility(View.VISIBLE);
                                }
                                // Check that the activity is using the layout version with
                                // the fragment_container FrameLayout
                                if (findViewById(R.id.fragmentcontainer) != null) {

                                    // Create a new Fragment to be placed in the activity layout
                                    firstFragment = new ActivityRecognitionFragment();

                                    //Se era stata fatta partire la webview, la nascondo
                                    if(webView.getVisibility()==View.VISIBLE){
                                        webviewVisibleBefore = true;
                                        webView.setVisibility(View.GONE);
                                    }

                                    //Se il fragment è già presente, lo attacco. altrimenti lo creo
                                    if(firstFragment.isDetached()){
                                        getFragmentManager().beginTransaction().attach(firstFragment).commit();
                                    }
                                    else {
                                        //faccio comparire il fragmentcontainer e creo il fragment
                                        findViewById(R.id.fragmentcontainer).setVisibility(View.VISIBLE);
                                        getFragmentManager().beginTransaction()
                                                .add(R.id.fragmentcontainer, firstFragment).commit();
                                    }
                                }

                                    result.setSelection(-1);


                                Log.d("HomeActivity","activityrec "+position);
                                break;

                            case DASHBOARD:
                                Log.d("HomeActivity","dashboard "+position);
                                result.setSelection(-1);

                                //Stacco fragment (se è connesso)
                                if(firstFragment != null && firstFragment.isVisible()){
                                    getFragmentManager().beginTransaction().detach(firstFragment);
                                }

                                loadPage(DASHBOARD);

                                break;

                            case SENSOR_DATA:
                                //Activity dati sensori
                                Log.d("HomeActivity","sensordata "+position);
                                result.setSelection(-1);

                                //Stacco fragment (se è connesso)
                                if(firstFragment != null && firstFragment.isVisible()){
                                    getFragmentManager().beginTransaction().detach(firstFragment);
                                }

                                loadPage(SENSOR_DATA);
                                break;

                            case LOG:
                                Log.d("HomeActivity","log "+position);
                                result.setSelection(-1);

                                //Stacco fragment (se è connesso)
                                if(firstFragment != null && firstFragment.isVisible()){
                                    getFragmentManager().beginTransaction().detach(firstFragment);
                                }

                                loadPage(LOG);
                                break;

                            case SETTINGS:
                                //Apro drawer completo e apro expandable
                                Log.d("HomeActivity","settings "+position);
                                crossFader.crossFade();
                                result.setSelection(-1);

                                //Stacco fragment (se è connesso)
                                if(firstFragment != null && firstFragment.isVisible()){
                                    getFragmentManager().beginTransaction().detach(firstFragment);
                                }
                               // result.setSelectionAtPosition(4);
                                //startActivity(new Intent(HomeActivity.this,SettingsActivity.class));
                                break;

                            case SETTINGS_DUMMY:
                                Log.d("HomeActivity","settingsDummy "+position);

                                //Stacco fragment (se è connesso)
                                if(firstFragment != null && firstFragment.isVisible()){
                                    getFragmentManager().beginTransaction().detach(firstFragment);
                                }
                                crossFader.crossFade();
                                result.setSelection(-1);
                                break;

                            case ABOUT:
                                //About
                                Log.d("HomeActivity","about "+position);

                                //Stacco fragment (se è connesso)
                                if(firstFragment != null && firstFragment.isVisible()){
                                    getFragmentManager().beginTransaction().detach(firstFragment);
                                }

                                result.setSelection(-1);
                                break;

                            default:
                                Log.d("HomeActivity",""+position);

                        }

                        return false;
                    }
                })
                .withSelectedItem(-1)
                .withGenerateMiniDrawer(true)
                .buildView();

        //Rimuovo animazione chiamando il recyclerview del drawer
        result.getRecyclerView().getItemAnimator().setChangeDuration(0);
        result.getRecyclerView().getItemAnimator().setMoveDuration(0);
        result.getRecyclerView().getItemAnimator().setAddDuration(0);
        result.getRecyclerView().getItemAnimator().setRemoveDuration(0);

        //the MiniDrawer is managed by the Drawer and we just get it to hook it into the Crossfader
        miniResult = result.getMiniDrawer();

        //get the widths in px for the first and second panel
        int firstWidth = (int) UIUtils.convertDpToPixel(300, this);
        int secondWidth = (int) UIUtils.convertDpToPixel(72, this);

        //create and build our crossfader (see the MiniDrawer is also builded in here, as the build method returns the view to be used in the crossfader)
        //the crossfader library can be found here: https://github.com/mikepenz/Crossfader
        crossFader = new Crossfader()
                .withContent(findViewById(R.id.homeTitle))
                .withFirst(result.getSlider(), firstWidth)
                .withSecond(miniResult.build(this), secondWidth)
                .withSavedInstance(savedInstanceState)
                .build();

        //definisco listener custom per il mio crossfader, in modo da gestire la rimozione dell'elemento
        crossFader.withPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

                if(slideOffset!=0){
                    //If per prevenire che vengano cancellati altri elementi
                    if(result.getDrawerItems().size()==9) {
                        about.withIcon(R.drawable.noicon);
                        about.withName("");


                        //Tolgo il dummy
                        result.removeItemByPosition(7);

                        //rimetto tutto
                        about.withIcon(GoogleMaterial.Icon.gmd_info);
                        about.withName(R.string.about);

                        //cambio icona controlpanel
                        controlpanel.withIcon(GoogleMaterial.Icon.gmd_arrow_back);
                        result.updateItem(controlpanel);
                    }

                }

            }

            @Override
            public void onPanelOpened(View panel) {
            }

            @Override
            public void onPanelClosed(View panel) {

                controlpanel.withIcon(GoogleMaterial.Icon.gmd_menu);
                result.updateItem(controlpanel);

                //collapso l'expandabledraweritem
                if(result.getDrawerItems().size()!=9) {
                    result.addItemAtPosition(dummy, 7);
                }
                result.getAdapter().collapse();
                result.setSelection(-1);
            }
        });

//imposto il wrapper custom
        crossfaderWrapper= new CrossfadeWrapper(crossFader);

        //define the crossfader to be used with the miniDrawer. This is required to be able to automatically toggle open / close
        miniResult.withCrossFader(crossfaderWrapper);

        //Definisco divider
        crossFader.getCrossFadeSlidingPaneLayout().setShadowResourceLeft(R.drawable.divider);


        //In questa parte inizializzo la webView.
        webView = (WebView) findViewById(R.id.webview);

        //Attivo il tablet a poter utilizzare codice javascript

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);

        progress = new ProgressDialog(this);
        progress.hide();

        //attivo cross origin per poter visualizzare il grafico delle attività
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        webView.setWebContentsDebuggingEnabled(true);

        //imposto handler per gli errori
        webView.setWebViewClient(new WebViewClient(){

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Error loading page")
                        .setMessage("Error loading page: "+description+"\n Code: "+errorCode)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show()
                        .setCanceledOnTouchOutside(false);
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                if(progress.isShowing()){
                    progress.hide();
                }
            }

        });

    }

    @Override
    public void onBackPressed(){
        if(crossFader.isCrossFaded()){
            crossFader.crossFade();
        }
        else {
            super.onBackPressed();
        }
    }

    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
            String nomeItem;
            if (drawerItem instanceof Nameable) {
                Log.i("material-drawer", "DrawerItem: " + ((Nameable) drawerItem).getName() + " - toggleChecked: " + isChecked);

                nomeItem = ((Nameable) drawerItem).getName().toString();

                switch (nomeItem){

                    case "Enable Voice Recognition":

                        //Preparo il preference manager
                        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        edit = pref.edit();

                        if(isChecked){
                            //Attivo
                            edit.putBoolean("voiceEnabled", true);


                            //controllo permission
                            if (ContextCompat.checkSelfPermission(HomeActivity.this,
                                    Manifest.permission.RECORD_AUDIO)
                                    != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(HomeActivity.this,
                                        new String[]{Manifest.permission.RECORD_AUDIO},
                                        0);

                            }
                        }
                        else{
                            //disattivo
                            edit.putBoolean("voiceEnabled", false);
                        }
                        edit.commit();
                        break;

                }

            } else {
                Log.i("material-drawer", "toggleChecked: " + isChecked);
            }
        }
    };

    public void loadPage(int drawerItemId){

        String sideBarItem="";

        //Se la webview non è stata inizalizzata, allora la inizalizzo (metto il progress, blablabla).
        //altrimenti carico l'url e basta

        if(webView.getVisibility()==View.GONE){

            if(!webviewVisibleBefore){
                progress.setMessage("Connecting...");
                progress.setIndeterminate(true);
                progress.setCanceledOnTouchOutside(false);
                progress.show();
            }

            webView.setVisibility(View.VISIBLE);
        }


        //controllo i valori di sidebarItem per vedere se è la dashboard oppure altri item della sidebar
        switch(drawerItemId){

            case SENSOR_DATA:
                sideBarItem = "custom";
                break;

            case LOG:
                sideBarItem = "server";
        }


        //Carico pagina
        webView.loadUrl("http://"+url+"/"+sideBarItem);

        //Se c'è il form, faccio automaticamente il login
        if(webView.getUrl().contains("login") || true){

            webView.loadUrl("javascript: {" +
                    "document.getElementsByName('username')[0].value = 'admin';" +
                    "document.getElementById('inputPassword3').value = 'nPrwsY7b';" +
                    "var frms = document.getElementsByClass('form-horizontal');" +
                    "document.getElementsByTagName('input')[2].checked = true;" +
                    "frms[0].submit(); };");
        }

        //gestisco touch per webview (per evitare che interferisca (???) con il drawer
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Quando tocco la webview blocco il crossfader, quando rilascio il dito lo sblocco
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    crossFader.withCanSlide(false);
                } else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    crossFader.withCanSlide(true);
                }

                return false;
            }
        });


    }

    @Override
    public void onDestroy(){
        progress.dismiss();


       if(notifica != null) {
           //tolgo notification
           notificationmanager.cancel(NOTIFICATION_SERVICE_RUNNING_ID);
       }

       disconnectService();
        super.onDestroy();
    }

    //Metodi custom

    static public JSONArray sortActivities(JSONObject dataFromService){
        //Prendo il dato ricevuto dal service e lo trasformo in un oggetto JSON
        JSONObject receivedData;
        JSONObject maxObj;
        JSONObject tempObj;
        JSONArray probActivities;
        JSONArray sortedActivities = new JSONArray();

        int maxIndex = 0;

        try {
            receivedData = dataFromService;
            probActivities = (JSONArray) receivedData.get("data");
            maxObj = new JSONObject("{'activity':'lol','probability':0.0}");
            tempObj = maxObj;

            //Costruisco un nuovo JSONArray con le attività ordinate in modo descrescente per la probabilità
            while (probActivities.length() != 0) {

                for (int i = 0; i < probActivities.length(); i++) {
                    if (probActivities.getJSONObject(i).getDouble("probability") >= maxObj.getDouble("probability")) {
                        maxObj = probActivities.getJSONObject(i);
                        maxIndex = i;
                    }
                }

                //Calcolo il max dell'array, e una volta inserito nel nuovo array, lo cancello dal vecchio
                sortedActivities.put(maxObj);
                maxObj = tempObj;
                probActivities.remove(maxIndex);

            }

            //Infine, ritorno l'array
            return sortedActivities;

        } catch (JSONException e) {
            Log.e("onServiceConnected", e.toString());
            return null;
        }
    }

    private void showNotification() {
        //creo notifica che rimarrà finchè sarà attivo il service

        /* Qui vi sono le istruzioni per fare in modo che cliccando sulla notifica si riapre l'activity*/
        Intent notificationIntent = new Intent(getApplicationContext(), HomeActivity.class);

        notificationIntent.setAction("android.intent.action.MAIN");
        notificationIntent.addCategory("android.intent.category.LAUNCHER");

        PendingIntent pendingIntent = PendingIntent.getActivity(HomeActivity.this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Qui inizia la costruzionee della notifica vera e propria

        notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifica = new Notification.Builder(this)
                .setContentTitle("Service Running")
                .setContentText("The Activity Recognition service is now running.")
                .setSmallIcon(R.drawable.ic_play_button_sing_colorato)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build();

        notificationmanager.notify(NOTIFICATION_SERVICE_RUNNING_ID, notifica);
    }

    public void disconnectService() {
        //Scollego wakeupservice
        if (wakeupBoundToActivity) {
            unbindService(wakeupConnection);
            wakeupBoundToActivity = false;
            wakeService.stopService(new Intent(HomeActivity.this, WakeUpService.class));
        }
    }


}

