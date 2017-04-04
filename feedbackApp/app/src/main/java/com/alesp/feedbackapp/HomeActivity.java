package com.alesp.feedbackapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
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
    final static int SENSOR_DATA = 3;
    final static int SETTINGS_DUMMY = 5;
    final static int SETTINGS = 6;
    final static int ABOUT = 7;



    boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        //creo dummy
        final PrimaryDrawerItem dummy = new PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_settings)
                .withIdentifier(5)
                .withName("remove");

        //memorizzo un draweritem in una variable poichè poi devo cambiare l'icona (control panel)
        final PrimaryDrawerItem controlpanel = new PrimaryDrawerItem().withName("Control Panel")
                .withIcon(GoogleMaterial.Icon.gmd_menu)
                .withIdentifier(CONTROL_PANEL)
                .withSelectable(true);

        final PrimaryDrawerItem about = new PrimaryDrawerItem()
                .withName(getString(R.string.about))
                .withIcon(GoogleMaterial.Icon.gmd_info)
                .withIdentifier(ABOUT)
                .withSelectable(false);

        //CREAZIONE DRAWER
        result = new DrawerBuilder()
                .withActivity(this)
                .addDrawerItems(
                        //aggiungo elementi al mio Drawer
                        controlpanel,
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withDescription("Starts the activity recognition service").withName("Activity Recognition").withIcon(GoogleMaterial.Icon.gmd_directions_walk).withIdentifier(ACTIVITY_RECOGNITION),
                        new PrimaryDrawerItem().withName("Sensor Data").withIcon(FontAwesome.Icon.faw_bar_chart).withIdentifier(SENSOR_DATA),
                        //Setto collapsable per i settings
                        new ExpandableDrawerItem().withName(getString(R.string.settings)).withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(SETTINGS).withSelectable(false).withSubItems(
                                new SecondarySwitchDrawerItem().withName("Enable Voice Recognition").withIcon(GoogleMaterial.Icon.gmd_mic).withChecked(true).withOnCheckedChangeListener(onCheckedChangeListener)
                                //new SecondaryDrawerItem().withName("CollapsableItem 2").withLevel(2).withIcon(GoogleMaterial.Icon.gmd_8tracks).withIdentifier(2001)

                        ),
                        //Setto dummy per i settings (che rimuovo in automatico durante il crossfade

                        //NB: MAGARI UTILIZZA SETVISIBILITY SUL PRIMARYDRAWERITEM?
                        dummy,
                        about)
                        .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {
                           // Toast.makeText(MiniDrawerActivity.this, ((Nameable) drawerItem).getName().getText(MiniDrawerActivity.this), Toast.LENGTH_SHORT).show();
                        }
                        Log.d("position",position+"");
                        switch (position){

                            case CONTROL_PANEL:
                                Log.d("lol","what");
                                if(!isExpanded){
                                    //Apro drawer completo
                                    Log.d("HomeActivity","espando");
                                    crossfaderWrapper.crossfade();
                                    isExpanded = true;
                                    controlpanel.withIcon(GoogleMaterial.Icon.gmd_arrow_back);
                                }
                                else{
                                    //chiudo drawer
                                    crossfaderWrapper.crossfade();
                                    Log.d("HomeActivity","riduco");
                                    isExpanded = false;
                                    controlpanel.withIcon(GoogleMaterial.Icon.gmd_menu);

                                }
                                result.setSelection(-1);
                                break;

                            case ACTIVITY_RECOGNITION:
                                //Faccio partire il service
                                //Differenza tra mini e complete in termini di azioni?
                                Log.d("HomeActivity","activityrec");
                                startActivity(new Intent(HomeActivity.this,IdleActivity.class));
                                result.setSelection(-1);
                                break;

                            case SENSOR_DATA:
                                //Activity dati sensori
                                Log.d("HomeActivity","sensordata");
                                result.setSelection(-1);
                                break;

                            case SETTINGS:
                                //Apro drawer completo e apro expandable
                                Log.d("HomeActivity","settings");
                                crossFader.crossFade();
                               // result.setSelectionAtPosition(4);
                                //startActivity(new Intent(HomeActivity.this,SettingsActivity.class));
                                break;

                            case SETTINGS_DUMMY:
                                Log.d("HomeActivity","settingsDummy");
                                crossFader.crossFade();
                                break;

                            case ABOUT:
                                //About
                                Log.d("HomeActivity","about");
                                result.setSelection(-1);
                                break;

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
                    if(result.getDrawerItems().size()==7) {
                        about.withIcon(R.drawable.noicon);
                        about.withName("");


                        //Tolgo il dummy
                        result.removeItemByPosition(5);

                        //rimetto tutto
                        about.withIcon(GoogleMaterial.Icon.gmd_info);
                        about.withName(R.string.about);
                    }

                }

            }

            @Override
            public void onPanelOpened(View panel) {

            }

            @Override
            public void onPanelClosed(View panel) {
                Log.d("lolol",result.getDrawerItems().size()+"");
                if(result.getDrawerItems().size()!=7)
                result.addItemAtPosition(dummy,5);
            }
        });

//imposto il wrapper custom
        crossfaderWrapper= new CrossfadeWrapper(crossFader);

        //define the crossfader to be used with the miniDrawer. This is required to be able to automatically toggle open / close
        miniResult.withCrossFader(crossfaderWrapper);

        //Definisco divider
        crossFader.getCrossFadeSlidingPaneLayout().setShadowResourceLeft(R.drawable.divider);


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


}

