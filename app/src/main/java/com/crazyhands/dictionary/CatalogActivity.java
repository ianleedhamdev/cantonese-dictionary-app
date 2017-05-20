package com.crazyhands.dictionary;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.crazyhands.dictionary.data.Contract.WordEntry;
import com.crazyhands.dictionary.data.dictionaryDBHelper;

public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the word data loader */
    private static final int WORD_LOADER = 0;
    /** Adapter for the ListView */
    DictionaryCursorAdapter mCursorAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        // Find the ListView which will be populated with the word data
        ListView wordListView = (ListView) findViewById(R.id.list);
        // Setup an Adapter to create a list item for each row of word data in the Cursor.
        // There is no word data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new DictionaryCursorAdapter(this, null);
        wordListView.setAdapter(mCursorAdapter);
        //displayDatabaseInfo();

        // Kick off the loader
        getLoaderManager().initLoader(WORD_LOADER, null, this);
    }



    private void insertWord() {
        // Create a ContentValues object where column names are the keys,
        // and hearts word attributes are the values.
        ContentValues values = new ContentValues();
        values.put(WordEntry.COLUMN_DICTIONARY_ENGLISH, "heart");
        values.put(WordEntry.COLUMN_DICTIONARY_JYUTPING, "sam");
        values.put(WordEntry.COLUMN_DICTIONARY_CANTONESE, "心");
        values.put(WordEntry.COLUMN_DICTIONARY_SOUND_ID, "SOUND_20170507_220116.3gp" );
        getContentResolver().insert(WordEntry.CONTENT_URI, values);

        values.put(WordEntry.COLUMN_DICTIONARY_ENGLISH, "wood");
        values.put(WordEntry.COLUMN_DICTIONARY_JYUTPING, "jyutping");
        values.put(WordEntry.COLUMN_DICTIONARY_CANTONESE, "木");
        values.put(WordEntry.COLUMN_DICTIONARY_SOUND_ID, "SOUND_20170507_220116.3gp" );
        getContentResolver().insert(WordEntry.CONTENT_URI, values);

        values.put(WordEntry.COLUMN_DICTIONARY_ENGLISH, "gold");
        values.put(WordEntry.COLUMN_DICTIONARY_JYUTPING, "jyutping");
        values.put(WordEntry.COLUMN_DICTIONARY_CANTONESE, "金");
        values.put(WordEntry.COLUMN_DICTIONARY_SOUND_ID, "SOUND_20170507_220116.3gp" );
        getContentResolver().insert(WordEntry.CONTENT_URI, values);

        values.put(WordEntry.COLUMN_DICTIONARY_ENGLISH, "centre");
        values.put(WordEntry.COLUMN_DICTIONARY_JYUTPING, "Jong");
        values.put(WordEntry.COLUMN_DICTIONARY_CANTONESE, "中");
        values.put(WordEntry.COLUMN_DICTIONARY_SOUND_ID, "SOUND_20170507_220116.3gp" );
        getContentResolver().insert(WordEntry.CONTENT_URI, values);

        values.put(WordEntry.COLUMN_DICTIONARY_ENGLISH, "moon");
        values.put(WordEntry.COLUMN_DICTIONARY_JYUTPING, "jyutping");
        values.put(WordEntry.COLUMN_DICTIONARY_CANTONESE, "月");
        values.put(WordEntry.COLUMN_DICTIONARY_SOUND_ID, "SOUND_20170507_220116.3gp");
        getContentResolver().insert(WordEntry.CONTENT_URI, values);

        values.put(WordEntry.COLUMN_DICTIONARY_ENGLISH, "loyalty");
        values.put(WordEntry.COLUMN_DICTIONARY_JYUTPING, "zung");
        values.put(WordEntry.COLUMN_DICTIONARY_CANTONESE, "忠");
        values.put(WordEntry.COLUMN_DICTIONARY_SOUND_ID, "SOUND_20170507_220116.3gp" );
        getContentResolver().insert(WordEntry.CONTENT_URI, values);




        // Insert a new row for heart into the provider using the ContentResolver.
        // Use the {@link WordEntry#CONTENT_URI} to indicate that we want to insert
        // into the words database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        //Uri uri = getContentResolver().insert(WordEntry.CONTENT_URI, values);
        //displayDatabaseInfo();
    }


    private void deleteAllWords() {
        int rowsDeleted = getContentResolver().delete(WordEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the dictionary database.
     */

    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        dictionaryDBHelper mDbHelper = new dictionaryDBHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Perform this raw SQL query "SELECT * FROM dictionary"
        // to get a Cursor that contains all rows from the dictionary table.
        Cursor cursor = db.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME, null);
        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // dictionary table in the database).
            TextView displayView = (TextView) findViewById(R.id.text_view_word);
            displayView.setText("Number of rows in dictionary database table: " + cursor.getCount());

        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
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
                insertWord();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                Intent intent = new Intent(CatalogActivity.this, CantoneseCloudList.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                WordEntry._ID,
                WordEntry.COLUMN_DICTIONARY_ENGLISH,
                WordEntry.COLUMN_DICTIONARY_JYUTPING,
                WordEntry.COLUMN_DICTIONARY_SOUND_ID,
                WordEntry.COLUMN_DICTIONARY_CANTONESE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                WordEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link DictionaryCursorAdapter} with this new cursor containing updated words data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
