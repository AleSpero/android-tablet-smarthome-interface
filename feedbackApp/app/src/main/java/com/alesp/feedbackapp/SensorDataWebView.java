package com.alesp.feedbackapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

/**
 * Created by alessandro on 26.04.17.
 */

public class SensorDataWebView extends Activity {

    WebView webView; //abbastanza autoesplicativo
    String url = "159.149.152.241";
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Setto webview come intera finestra
        webView = new WebView(this);
        setContentView(webView);

        //inizializzo progressdialog e imposto parametri
        progress = new ProgressDialog(this);
        progress.setMessage("Connecting...");
        progress.setIndeterminate(true);
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        //Attivo il tablet a poter utilizzare codice javascript

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebContentsDebuggingEnabled(true);

        //imposto handler per gli errori
        webView.setWebViewClient(new WebViewClient(){

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
                new AlertDialog.Builder(SensorDataWebView.this)
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

        //Carico pagina
        webView.loadUrl("http://"+url+"/");

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


}
