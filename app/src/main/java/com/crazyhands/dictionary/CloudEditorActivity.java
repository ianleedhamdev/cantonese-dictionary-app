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
import android.widget.TextView;
import android.widget.Toast;

import com.crazyhands.dictionary.App.Config;
import com.crazyhands.dictionary.data.MediaPlayeHelperClass;


import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

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
    // directory name to store captured images and videos

    private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";

    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_SOUND_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_SOUND = 2;

    MediaPlayeHelperClass mediaPlayerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_editor);





        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.
        Intent intent = getIntent();
        mCurrentWordUri = intent.getData();

        // If the intent DOES NOT contain a word content URI, then we know that we are
        // creating a new word.
        if (mCurrentWordUri == null) {
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
        }



        mEnglishEditText = (EditText) findViewById(R.id.cloud_edit_English);
        mJyutpingEditText = (EditText) findViewById(R.id.cloud_edit_Jyutping);
        mCantoneseEditText = (EditText) findViewById(R.id.cloud_edit_Cantonese);
        /** buttons for the sound recorder */

        buttonStart = (Button) findViewById(R.id.cloud_record_button);
        buttonStop = (Button) findViewById(R.id.cloud_stop_button);
        buttonPlayLastRecordAudio = (Button) findViewById(R.id.cloud_play_button);
        buttonStopPlayingRecording = (Button)findViewById(R.id.cloud_Rerecord_button);
        mSoundtextview = (TextView)findViewById(R.id.cloud_soundRecorderTextView);
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
                TextUtils.isEmpty(cantoneseString)) {
            // Since no fields were modified, we can return early without creating a new word.
            // No need to create ContentValues and no need to do any ContentProvider operations.
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
        String path = getFilesDir()+soundstring;//this may be wrong todo check
        Log.v("file address phone is:","//storage/emulated/0/Pictures/Hello Camera/SOUND_20170520_143612.3gp" );

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




    }
