package com.alesp.feedbackapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
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
    final static int SETTINGS = 4;
    final static int ABOUT = 6;

    boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);



        //memorizzo un draweritem in una variable poichè poi devo cambiare l'icona (control panel)
        final PrimaryDrawerItem controlpanel = new PrimaryDrawerItem().withName("Control Panel")
                .withIcon(GoogleMaterial.Icon.gmd_menu)
                .withIdentifier(CONTROL_PANEL)
                .withSelectable(true);

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
                        new PrimaryDrawerItem().withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(5),
                        new PrimaryDrawerItem().withName(getString(R.string.about)).withIcon(GoogleMaterial.Icon.gmd_info).withIdentifier(ABOUT).withSelectable(false))
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
                                    crossfaderWrapper.crossfade(result,(PrimaryDrawerItem) result.getDrawerItem(5));
                                    isExpanded = true;
                                    controlpanel.withIcon(GoogleMaterial.Icon.gmd_arrow_back);
                                }
                                else{
                                    //chiudo drawer
                                    crossfaderWrapper.crossfade(result,(PrimaryDrawerItem) result.getDrawerItem(5));
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
                                result.setSelection(-1);
                                break;

                            case SENSOR_DATA:
                                //Activity dati sensori
                                Log.d("HomeActivity","sensordata");
                                result.setSelection(-1);
                                break;

                            case SETTINGS:
                                //Activity settings
                                Log.d("HomeActivity","settings");
                                result.setSelection(-1);
                                //startActivity(new Intent(HomeActivity.this,SettingsActivity.class));
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

//imposto il wrapper custom
        crossfaderWrapper= new CrossfadeWrapper(crossFader);

        //define the crossfader to be used with the miniDrawer. This is required to be able to automatically toggle open / close
        miniResult.withCrossFader(crossfaderWrapper);

        //Definisco divider
        crossFader.getCrossFadeSlidingPaneLayout().setShadowResourceLeft(R.drawable.divider);
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

