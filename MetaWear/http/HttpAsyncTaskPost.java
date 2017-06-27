package com.alesp.feedbackapp.MetaWear.http;

import android.os.AsyncTask;

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

public class HttpAsyncTaskPost extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {

        String url = "http://159.149.152.241:3000/"+params[1];
        URL requestUrl = null;
        String res = "";
        try {
            requestUrl = new URL(url);
            HttpURLConnection conn;
            conn = (HttpURLConnection) requestUrl.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(params[0]);
            wr.flush();
            wr.close();

            InputStream is = conn.getInputStream();
            BufferedReader rd;
            rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine())!=null){
                response.append(line);
                response.append('\r');
            }
            rd.close();
            res = new String(response);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            res = "IOException";
        }
        return res;
    }
}
