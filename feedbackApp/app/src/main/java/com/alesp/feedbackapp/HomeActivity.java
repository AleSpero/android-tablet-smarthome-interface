package com.alesp.feedbackapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

/**
 * Created by alesp on 31/03/2017.
 */

public class HomeActivity extends Activity {

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
                        new PrimaryDrawerItem().withName("Dashboard").withIcon(GoogleMaterial.Icon.gmd_dashboard).withIdentifier(DASHBOARD),
                        new PrimaryDrawerItem().withName("Sensor Data").withIcon(FontAwesome.Icon.faw_bar_chart).withIdentifier(SENSOR_DATA),
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
                                //Faccio partire il service
                                //Se ho cliccato sul minidrawer, faccio partire l'app senza farla vedere (così parte in automatico il service)
                                Intent intent = new Intent(HomeActivity.this,ActivityRecognition.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                result.setSelection(-1);
                                startActivity(intent);


                                Log.d("HomeActivity","activityrec "+position);
                                break;

                            case DASHBOARD:
                                Log.d("HomeActivity","dashboard "+position);
                                result.setSelection(-1);
                                loadPage(DASHBOARD);
                                break;

                            case SENSOR_DATA:
                                //Activity dati sensori
                                Log.d("HomeActivity","sensordata "+position);
                                result.setSelection(-1);
                                loadPage(SENSOR_DATA);
                                break;

                            case LOG:
                                Log.d("HomeActivity","log "+position);
                                result.setSelection(-1);
                                loadPage(LOG);
                                break;

                            case SETTINGS:
                                //Apro drawer completo e apro expandable
                                Log.d("HomeActivity","settings "+position);
                                crossFader.crossFade();
                                result.setSelection(-1);
                               // result.setSelectionAtPosition(4);
                                //startActivity(new Intent(HomeActivity.this,SettingsActivity.class));
                                break;

                            case SETTINGS_DUMMY:
                                Log.d("HomeActivity","settingsDummy "+position);
                                crossFader.crossFade();
                                result.setSelection(-1);
                                break;

                            case ABOUT:
                                //About
                                Log.d("HomeActivity","about "+position);
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
            progress.setMessage("Connecting...");
            progress.setIndeterminate(true);
            progress.setCanceledOnTouchOutside(false);
            progress.show();

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


    }

    @Override
    public void onDestroy(){
        progress.dismiss();
        super.onDestroy();
    }

}

