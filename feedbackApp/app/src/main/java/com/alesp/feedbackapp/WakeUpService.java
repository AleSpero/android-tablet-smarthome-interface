package com.alesp.feedbackapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by alesp on 20/03/2017.
 */

public class WakeUpService extends Service {

    //WakeUpService si occupa di chiamare, con un criterio non ancora deciso, l'attività QueryUser, per interrogarlo sull'attività
    //che sta facendo. Questo servizio girerà esclusivamente su IdleActivity.


    private final IBinder binder = new WakeUpBinder();

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent){
        return binder;
    }

    public class WakeUpBinder extends Binder {

        WakeUpService getService() {
            //In questo metodo ritorno un'istanza di Connectionservice in modo che le activity possano chiamare
            //i relativi metodi

            return WakeUpService.this;
        }

    }

    @Override
    public void onDestroy(){
        Log.d("WakeUpService","Servizio Chiuso");
    }

}
