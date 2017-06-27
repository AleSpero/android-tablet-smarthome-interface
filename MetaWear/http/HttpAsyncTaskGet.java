package com.alesp.feedbackapp.MetaWear.http;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Stefano on 06/11/2015.
 */

public class HttpAsyncTaskGet extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {
        String url = "http://159.149.152.241:3000/"+params[0];
        URL requestUrl = null;
        String res = "";
        try {
            requestUrl = new URL(url);
            HttpURLConnection conn;
            conn = (HttpURLConnection) requestUrl.openConnection();
            conn.setRequestMethod("GET");

            InputStream is = conn.getInputStream();
            BufferedReader reader;
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();

            while((line = reader.readLine())!=null){
                response.append(line);
            }
            reader.close();
            res = new String(response);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

}
