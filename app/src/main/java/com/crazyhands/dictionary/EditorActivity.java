package com.crazyhands.dictionary;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import com.crazyhands.dictionary.data.Contract.WordEntry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


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
            getLoaderManager().initLoader(EXISTING_WORD_LOADER, null, this);
        }



        mEnglishEditText = (EditText) findViewById(R.id.edit_English);
        mJyutpingEditText = (EditText) findViewById(R.id.edit_Jyutping);
        mCantoneseEditText = (EditText) findViewById(R.id.edit_Cantonese);
        /** buttons for the sound recorder */

        buttonStart = (Button) findViewById(R.id.record_button);
        buttonStop = (Button) findViewById(R.id.stop_button);
        buttonPlayLastRecordAudio = (Button) findViewById(R.id.play_button);
        buttonStopPlayingRecording = (Button)findViewById(R.id.Rerecord_button);
        mSoundtextview = (TextView)findViewById(R.id.soundRecorderTextView);
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

                    Toast.makeText(EditorActivity.this, "Recording started",
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

                Toast.makeText(EditorActivity.this, "Recording Completed",
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
                Toast.makeText(EditorActivity.this, "Recording Playing",
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
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and word attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(WordEntry.COLUMN_DICTIONARY_ENGLISH, englishString);
        values.put(WordEntry.COLUMN_DICTIONARY_JYUTPING, jyutpingString);
        values.put(WordEntry.COLUMN_DICTIONARY_CANTONESE, cantoneseString);
        values.put(WordEntry.COLUMN_DICTIONARY_SOUND_ID, soundstring );

        // Determine if this is a new or existing word by checking if mCurrentWordUri is null or not
        if (mCurrentWordUri == null) {
            // This is a NEW word, so insert a new word into the provider,
            // returning the content URI for the new word.
            Uri newUri = getContentResolver().insert(WordEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_word_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_word_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING word, so update the word with content URI: mCurrentWordUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentWordUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentWordUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_word_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_word_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }




    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all word attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                WordEntry._ID,
                WordEntry.COLUMN_DICTIONARY_ENGLISH,
                WordEntry.COLUMN_DICTIONARY_JYUTPING,
                WordEntry.COLUMN_DICTIONARY_CANTONESE,
                WordEntry.COLUMN_DICTIONARY_SOUND_ID };

        // This loader will execute the DictionaryProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentWordUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of word attributes that we're interested in
            int englishColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_DICTIONARY_ENGLISH);
            int jyutpingColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_DICTIONARY_JYUTPING);
            int cantoneseColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_DICTIONARY_CANTONESE);
            int soundColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_DICTIONARY_SOUND_ID);
            // Extract out the value from the Cursor for the given column index
            String english = cursor.getString(englishColumnIndex);
            String jyutping = cursor.getString(jyutpingColumnIndex);
            String cantonese = cursor.getString(cantoneseColumnIndex);
            String soundAddress = cursor.getString(soundColumnIndex);
            // Update the views on the screen with the values from the database
            mEnglishEditText.setText(english);
            mJyutpingEditText.setText(jyutping);
            mCantoneseEditText.setText(cantonese);
            mSoundtextview.setText(soundAddress);


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mEnglishEditText.setText("");
        mJyutpingEditText.setText("");
        mCantoneseEditText.setText("");
        //mSoundAddressEditText.setText("");

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
        ActivityCompat.requestPermissions(EditorActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(EditorActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(EditorActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }


    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_SOUND) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "SOUND_" + timeStamp + ".3gp");
        } else {
            return null;
        }

        return mediaFile;
    }
}





