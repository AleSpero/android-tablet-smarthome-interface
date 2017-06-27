package com.alesp.feedbackapp.MetaWear;

import android.os.AsyncTask;

import com.alesp.feedbackapp.MetaWear.http.HttpAsyncTaskGet;
import com.alesp.feedbackapp.MetaWear.http.HttpAsyncTaskPost;
import com.google.gson.Gson;

import java.util.concurrent.ExecutionException;

/**
 * Created by Stefano on 17/12/2015.
 */

public class Utils {
    public static synchronized String sendObject(Object obj, String where){
        /*//check manipulation
        if(obj instanceof ClassifiedManipulation){
            System.out.println(((ClassifiedManipulation) obj).getIdentifier() + " " +((ClassifiedManipulation) obj).getAction());
        }*/
        Gson gson = new Gson();

        String json = gson.toJson(obj);
        AsyncTask<String, Void, String> s = new HttpAsyncTaskPost().execute(json, where);
        try {
            return s.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String sendRequest(String where){
        AsyncTask<String, Void, String> s = new HttpAsyncTaskGet().execute(where);
        try {
            return s.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return "";
    }
}
