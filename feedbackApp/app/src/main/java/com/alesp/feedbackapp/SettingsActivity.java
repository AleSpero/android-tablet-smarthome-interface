package com.alesp.feedbackapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by alesp on 23/03/2017.
 */

public class SettingsActivity extends Activity {

    SharedPreferences pref;
    SharedPreferences.Editor edit;

    ImageButton about;
    ImageButton voice;

    boolean voiceEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        about = (ImageButton) findViewById(R.id.about);
        voice = (ImageButton) findViewById(R.id.voice);

        voice.setImageResource(R.drawable.ic_volume_up_interface_symbol);
        about.setImageResource(R.drawable.ic_info);

        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Preparo il preference manager
                pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                edit = pref.edit();

                if(!voiceEnabled){
                    //attivo voce
                    edit.putBoolean("voiceEnabled", true);

                    //cambio icona
                    voice.setImageResource(R.drawable.ic_volume_up_interface_symbol);

                    voiceEnabled = true;
                }
                else{
                    edit.putBoolean("voiceEnabled", false);

                    //cambio icona
                    voice.setImageResource(R.drawable.ic_volume_off);

                    voiceEnabled = false;
                }

                edit.commit();
            }
        });
    }
}
