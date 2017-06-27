package com.example.stefano.myapplication.stickers.Activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.stefano.myapplication.MainActivity;
import com.example.stefano.myapplication.R;
import com.example.stefano.myapplication.stickers.beanInfoSticker.BeanInfoSticker;
import com.example.stefano.myapplication.stickers.beanInfoSticker.SingletonInfoSticker;

public class RequestInformation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_information);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getActionBar();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        /*******************************************************************************************
         * With this part of code we create the singleton with all sticker information received from
         * server.
         *******************************************************************************************/
        BeanInfoSticker infoSticker = null;


        infoSticker = SingletonInfoSticker.getInstance().getInfoSticker();




        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
