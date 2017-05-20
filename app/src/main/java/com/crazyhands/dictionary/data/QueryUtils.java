package com.crazyhands.dictionary.data;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crazyhands.dictionary.App.Config;
import com.crazyhands.dictionary.EditorActivity;
import com.crazyhands.dictionary.items.Cantonese_List_item;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by crazyhands on 08/05/2017.
 */

public class QueryUtils {

    public static List<Cantonese_List_item> extractDataFromJson(String response) {


        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(response)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding events to
        List<Cantonese_List_item> words = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(response);

            // Extract the JSONArray associated with the key called "features",
            // which represents a list of features (or events).
            JSONArray eventsarray = baseJsonResponse.getJSONArray("result");

            // For each earthquake in the eventsarray, create an {@link Event} object
            for (int i = 0; i < eventsarray.length(); i++) {

                // Get a single event at position i within the list of events
                JSONObject currentWord = eventsarray.getJSONObject(i);


                // Extract the value for the key called "id"
                int id = currentWord.getInt("id");

                // Extract the value for the key called "English"
                String english = currentWord.getString("English");

                // Extract the value for the key called "jyutping",
                String jyutping = currentWord.getString("jyutping");

                // Extract the value for the key called "cantonese"
                String cantonese = currentWord.getString("cantonese");

                // Extract the value for the key called "sound address"
                String soundAddress = currentWord.getString("soundAddress");

                // Create a new {@link List_item} object with the name, dste, time,
                // from the JSON response.
                Cantonese_List_item word = new Cantonese_List_item(id, english, jyutping, cantonese, soundAddress);

                // Add the new {@link Earthquake} to the list of events.
                words.add(word);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the JSON results", e);
        }

    // Return the list of events
        return words;
}



    public static void postWordData(final String english, final String jyutping, final String cantonese, final String soundAddress){
        final String TAG = "queryutils_addword";

        // Tag used to cancel the request
        String tag_string_req = "reqWord_register";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL_ADD_WORD, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Event Register Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {


                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());

            }
        }){

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                //params.put("profile_picture", new FileBody(new File("/storage/emulated/0/Pictures/VSCOCam/2015-07-31 11.55.14 1.jpg")));
                params.put("english", english);
                params.put("jyutping",  jyutping);
                params.put("cantonese",  cantonese);
                params.put("soundAddress",  soundAddress);

                return params;

            }

        };

        // Adding request to request queue
        //AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        //todo word out how to add the request...
        //todo add  error return

        return;
    }




};

