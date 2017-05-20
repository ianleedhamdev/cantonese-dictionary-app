package com.crazyhands.dictionary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crazyhands.dictionary.Adapters.CantoneseListAdapter;
import com.crazyhands.dictionary.data.QueryUtils;
import com.crazyhands.dictionary.items.Cantonese_List_item;

import java.util.ArrayList;
import java.util.List;

import static com.android.volley.VolleyLog.TAG;
import static com.crazyhands.dictionary.App.Config.URL_GET_CANTONESE;

public class CantoneseCloudList extends Activity {

    private CantoneseListAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CantoneseCloudList.this, CloudEditorActivity.class);
                startActivity(intent);


                // Find the ListView which will be populated with the word data
                ListView wordListView = (ListView) findViewById(R.id.list);
            }
        });




        final RequestQueue requestque = Volley.newRequestQueue(CantoneseCloudList.this);

        StringRequest request = new StringRequest(Request.Method.GET, URL_GET_CANTONESE,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Events Response: " + response.toString());

                        // Extract relevant fields from the JSON response and create a list of  List_items
                        final List<Cantonese_List_item> eventss = QueryUtils.extractDataFromJson(response);

                        // Find the ListView which will be populated with the word data
                        ListView wordListView = (ListView) findViewById(R.id.list);

                        // Create a new adapter that takes an empty list of events as input
                        mAdapter = new CantoneseListAdapter(CantoneseCloudList.this, new ArrayList<Cantonese_List_item>());

                        // Set the adapter on the {@link ListView}
                        // so the list can be populated in the user interface
                        wordListView.setAdapter(mAdapter);


                        if (eventss != null && !eventss.isEmpty()) {
                            mAdapter.addAll(eventss);
                           // View loadingIndicator = findViewById(R.id.loading_indicator);
                           // loadingIndicator.setVisibility(View.GONE);
                        }

                        requestque.stop();


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //textview.setText("someshit gone down!");
                        volleyError.printStackTrace();
                        Log.e(TAG, "Response error" + volleyError.getMessage());
                        Toast.makeText(CantoneseCloudList.this,
                                volleyError.getMessage(), Toast.LENGTH_LONG).show();
                        String message = null;
                        if (volleyError instanceof NetworkError) {
                            message = getString(R.string.ConnectionErrorMessage);
                        } else if (volleyError instanceof ServerError) {
                            message = "The server could not be found. Please try again after some time!!";
                        } else if (volleyError instanceof AuthFailureError) {
                            message = "Cannot connect to Internet...Please check your connection!";
                        } else if (volleyError instanceof ParseError) {
                            message = "Parsing error! Please try again after some time!!";
                        } else if (volleyError instanceof NoConnectionError) {
                            message = "Cannot connect to Internet...Please check your connection!";
                        } else if (volleyError instanceof TimeoutError) {
                            message = "Connection TimeOut! Please check your internet connection.";
                        }

                        Toast.makeText(CantoneseCloudList.this, message, Toast.LENGTH_SHORT).show();
                        requestque.stop();
                    }
                });
        requestque.add(request);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:

                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:

                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
