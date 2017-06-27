package com.example.stefano.myapplication.stickers.Activity.DatasetCollection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.stefano.myapplication.R;

/**
 * Created by Stefano on 13/01/2016.
 */

public class StartExp extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_exp);

        // Uncomment manipulations in manipulations.xml

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartExp.this, ManipulationsAcquisition.class);
                startActivity(intent);
                StartExp.this.finish();
            }
        });
    }
}
