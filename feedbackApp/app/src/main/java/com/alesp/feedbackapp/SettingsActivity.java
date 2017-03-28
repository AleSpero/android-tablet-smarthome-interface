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
    ImageButton microphone;
    ImageButton voice;

    boolean voiceEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        about = (ImageButton) findViewById(R.id.about);
        microphone = (ImageButton) findViewById(R.id.microphone);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplication());


        if(sp.getBoolean("voiceEnabled",true)){
            microphone.setImageResource(R.drawable.ic_microphone);
        }
        else{
            voiceEnabled = false;
            microphone.setImageResource(R.drawable.ic_muted);
        }

        about.setImageResource(R.drawable.ic_info);


        microphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Preparo il preference manager
                pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                edit = pref.edit();

                if(!voiceEnabled){
                    //attivo voce
                    edit.putBoolean("voiceEnabled", true);

                    //cambio icona
                    microphone.setImageResource(R.drawable.ic_microphone);

                    voiceEnabled = true;
                }
                else{
                    edit.putBoolean("voiceEnabled", false);

                    //cambio icona
                    microphone.setImageResource(R.drawable.ic_muted);

                    voiceEnabled = false;
                }

                edit.commit();
            }
        });
    }
}