package com.crazyhands.dictionary;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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
import com.crazyhands.dictionary.App.Config;
import com.crazyhands.dictionary.data.MediaPlayeHelperClass;
import com.crazyhands.dictionary.data.QueryUtils;
import com.crazyhands.dictionary.items.Cantonese_List_item;


import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.android.volley.VolleyLog.TAG;
import static com.crazyhands.dictionary.App.Config.URL_GET_CANTONESE_WHERE;
import static com.crazyhands.dictionary.App.Config.URL_GET_CANTONESE_WHERE_ID;

public class CloudEditorActivity extends AppCompatActivity {



    /** Identifier for the word data loader */
    private static final int EXISTING_WORD_LOADER = 0;

    /** Content URI for the existing word (null if it's a new word) */
    private Uri mCurrentWordUri;

    /** EditText field to enter the words's english */
    private EditText mEnglishEditText;

    /** EditText field to enter the word's Jyutping */
    private EditText mJyutpingEditText;

    /** EditText field to enter the word's Cantonese */
    private EditText mCantoneseEditText;

    /** variables for the sound recorder */

    Button buttonStart, buttonStop, buttonPlayLastRecordAudio,
            buttonStopPlayingRecording ;
    File AudioSavePathInDevice = null;
    TextView mSoundtextview;
    MediaRecorder mediaRecorder ;

    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer ;


    // Activity request codes

    public static final int MEDIA_TYPE_SOUND = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_editor);





        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.
        Intent intent = getIntent();
        mCurrentWordUri = intent.getData();
        int wordid = intent.getIntExtra("wordid", 0);


        mEnglishEditText = (EditText) findViewById(R.id.cloud_edit_English);
        mJyutpingEditText = (EditText) findViewById(R.id.cloud_edit_Jyutping);
        mCantoneseEditText = (EditText) findViewById(R.id.cloud_edit_Cantonese);
        mSoundtextview = (TextView)findViewById(R.id.cloud_soundRecorderTextView);

        // If the intent DOES NOT contain a word content URI, then we know that we are
        // creating a new word.
        if (wordid == 0) {
            // This is a new pet, so change the app bar to say "Add a Word"
            setTitle(getString(R.string.editor_activity_title_new_word));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a word that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing word, so change app bar to say "Edit Word"
            setTitle(getString(R.string.editor_activity_title_edit_word));

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            //getLoaderManager().initLoader(EXISTING_WORD_LOADER, null, CloudEditorActivity.this);
            //Todo do I need a loader?
            addValuesToFields(wordid);
        }




        /** buttons for the sound recorder */

        buttonStart = (Button) findViewById(R.id.cloud_record_button);
        buttonStop = (Button) findViewById(R.id.cloud_stop_button);
        buttonPlayLastRecordAudio = (Button) findViewById(R.id.cloud_play_button);
        buttonStopPlayingRecording = (Button)findViewById(R.id.cloud_Rerecord_button);
        buttonStop.setEnabled(false);
        buttonPlayLastRecordAudio.setEnabled(false);
        buttonStopPlayingRecording.setEnabled(false);

        /** onclick listeners for the sound recorder */


        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkPermission()) {

                    AudioSavePathInDevice = getOutputMediaFile(MEDIA_TYPE_SOUND);
                    mSoundtextview.setText(AudioSavePathInDevice.getName());
                    MediaRecorderReady();

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    buttonStart.setEnabled(false);
                    buttonStop.setEnabled(true);

                    Toast.makeText(CloudEditorActivity.this, "Recording started",
                            Toast.LENGTH_LONG).show();
                } else {
                    requestPermission();
                }

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorder.stop();
                buttonStop.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(true);
                buttonStart.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);

                Toast.makeText(CloudEditorActivity.this, "Recording Completed",
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonPlayLastRecordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException,
                    SecurityException, IllegalStateException {

                buttonStop.setEnabled(false);
                buttonStart.setEnabled(false);
                buttonStopPlayingRecording.setEnabled(true);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice.getPath());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Log.v("the audio path is: ",AudioSavePathInDevice.getPath() );

                Toast.makeText(CloudEditorActivity.this, "Recording Playing",
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonStopPlayingRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonStop.setEnabled(false);
                buttonStart.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(true);

                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    MediaRecorderReady();
                }
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveWord();
                // Exit activity
                //finish();
                Intent intent = new Intent(CloudEditorActivity.this, CantoneseCloudList.class);
                startActivity(intent);
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void saveWord() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String englishString = mEnglishEditText.getText().toString().trim();
        String jyutpingString = mJyutpingEditText.getText().toString().trim();
        String cantoneseString = mCantoneseEditText.getText().toString().trim();
        String soundstring = mSoundtextview.getText().toString().trim();
        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        if (mCurrentWordUri == null &&
                TextUtils.isEmpty(englishString) && TextUtils.isEmpty(jyutpingString) &&
                TextUtils.isEmpty(cantoneseString)&&
                TextUtils.isEmpty(soundstring)) {
            // Since no fields were modified, we can return early without creating a new word.
            // No need to create ContentValues and no need to do any ContentProvider operations. todo why dont these toasts work?
            Toast.makeText(CloudEditorActivity.this, "noo", Toast.LENGTH_LONG);
            return;
        } else {
            uploadMultipart(englishString, jyutpingString, cantoneseString, soundstring);
            //todo add toast of success or failure
            Toast.makeText(CloudEditorActivity.this, "yay", Toast.LENGTH_LONG);
        }
    }
    /*
    * This is the method responsible for image upload
    * We need the full image path and the name for the image in this method
    * */
    public void uploadMultipart(String englishString, String jyutpingString, String cantoneseString, String soundstring) {
        //getting name for the image
        //getting the actual path of the image
        String path = getFilesDir()+"/"+soundstring;//this may be wrong todo check
        //Log.v("file plus sound str is:",getFilesDir()+"/"+soundstring );
        Log.v("file get path is:",AudioSavePathInDevice.getPath() );


        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();
            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, Config.URL_ADD_WORD)
                    .addFileToUpload(path, "userfile") //Adding file
                    .addParameter("name", soundstring) //Adding text parameter to the request
                    .addParameter("english", englishString)
                    .addParameter("jyutping", jyutpingString)
                    .addParameter("english", englishString)
                    .addParameter("cantonese", cantoneseString)
                    .addParameter("soundAddress", soundstring)
                    .setMaxRetries(2)
                    .setUtf8Charset()
                    .startUpload(); //Starting the upload

        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

//methods for the sound recording

    public void MediaRecorderReady(){
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice.getPath());
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(CloudEditorActivity.this, new
                String[]{RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (RecordPermission) {
                        Toast.makeText(CloudEditorActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CloudEditorActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return  result1 == PackageManager.PERMISSION_GRANTED;
    }


    private File getOutputMediaFile(int type) {

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;

         if (type == MEDIA_TYPE_SOUND) {
            String jyutpingString = mJyutpingEditText.getText().toString().trim();
            mediaFile = new File(getFilesDir() + File.separator
                    + jyutpingString + timeStamp + ".3gp");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void addValuesToFields(int wordid){
        mEnglishEditText.setText("things english");
        mJyutpingEditText.setText("things juytping");
        mCantoneseEditText.setText("things cantonese");
        //mSoundtextview.setText("things sound location");

        Toast.makeText(CloudEditorActivity.this, "word id is: "+ wordid,
                Toast.LENGTH_LONG).show();
        final RequestQueue requestque = Volley.newRequestQueue(CloudEditorActivity.this);

        StringRequest request = new StringRequest(Request.Method.GET, URL_GET_CANTONESE_WHERE_ID+"/?Wordid="+wordid,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Events Response: " + response.toString());

                        // Extract relevant fields from the JSON response

                        // If the JSON string is empty or null, then return early.
                        if (TextUtils.isEmpty(response)) {
                            return;
                        }



                        // Try to parse the JSON response string. If there's a problem with the way the JSON
                        // is formatted, a JSONException exception object will be thrown.
                        // Catch the exception so the app doesn't crash, and print the error message to the logs.
                        try {

                            // Create a JSONObject from the JSON response string
                            JSONObject baseJsonResponse = new JSONObject(response);

                            // Extract the JSONArray associated with the key called "result",
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

                                mEnglishEditText.setText(english);
                                mJyutpingEditText.setText(jyutping);
                                mCantoneseEditText.setText(cantonese);
                                mSoundtextview.setText(soundAddress);

                            }

                        } catch (JSONException e) {
                            // If an error is thrown when executing any of the above statements in the "try" block,
                            // catch the exception here, so the app doesn't crash. Print a log message
                            // with the message from the exception.
                            Log.e("QueryUtils", "Problem parsing the JSON results", e);
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
                        Toast.makeText(CloudEditorActivity.this,
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

                        Toast.makeText(CloudEditorActivity.this, message, Toast.LENGTH_SHORT).show();
                        requestque.stop();
                    }
                });
        requestque.add(request);


    }




    }
