package com.maciek.v2.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.maciek.v2.DB.InsertPositionToList;
import com.maciek.v2.DB.TouristListContract;
import com.maciek.v2.DB.TuristListDbHelper;
import com.maciek.v2.DB.TuristListDbQuery;
import com.maciek.v2.R;
import com.maciek.v2.Utilities.DownloadService;
import com.maciek.v2.Utilities.VolleyGetRequest;

import java.util.Arrays;
import java.util.List;

public class DownloaderActivity extends AppCompatActivity implements Response.Listener<byte[]>, Response.ErrorListener, View.OnClickListener {

    private ProgressBar progressBar;
    private SQLiteDatabase db;
    private Button acceptButton, rejectButton, goToMainButton;
    private TextView textView;
    private TuristListDbHelper turistListDbHelper;
    private SharedPreferences.Editor editor;
    private TuristListDbQuery turistListDbQuery;
    private VolleyGetRequest volleyGetRequest;
    private ContentLoadingProgressBar loader;


    public static String DATABASE_VERSION = "DATABASE_VERSION";
    public static String LOCAL_DATABASE_VERSION = "LOCAL_DATABASE_VERSION";
    public static String SHOULD_UPDATE_POSITION_1 = "SHOULD_UPDATE_POSITION_1";
    public static String SHOULD_UPDATE_POSITION_2 = "SHOULD_UPDATE_POSITION_2";
    public static String SHOULD_UPDATE_POSITION_3 = "SHOULD_UPDATE_POSITION_3";
    public static String SHOULD_UPDATE_POSITION_4 = "SHOULD_UPDATE_POSITION_4";
    public static String SHOULD_UPDATE_AFTER_FIRST_DOWNLOAD_1 = "SHOULD_UPDATE_AFTER_FIRST_DOWNLOAD_1";
    public static String SHOULD_UPDATE_AFTER_FIRST_DOWNLOAD_2 = "SHOULD_UPDATE_AFTER_FIRST_DOWNLOAD_2";
    public static String SHOULD_UPDATE_AFTER_FIRST_DOWNLOAD_3 = "SHOULD_UPDATE_AFTER_FIRST_DOWNLOAD_3";
    public static String SHOULD_UPDATE_AFTER_FIRST_DOWNLOAD_4 = "SHOULD_UPDATE_AFTER_FIRST_DOWNLOAD_4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloader);
        turistListDbHelper = TuristListDbHelper.getInstance(this);
        textView = findViewById(R.id.downloader_textView);
        progressBar = findViewById(R.id.progress_bar_downloader);
        acceptButton = findViewById(R.id.accept_download_button);
        rejectButton = findViewById(R.id.reject_download_button);
        goToMainButton = findViewById(R.id.go_to_main_activity);
        acceptButton.setOnClickListener(this);
        rejectButton.setOnClickListener(this);
        goToMainButton.setOnClickListener(this);
        loader = findViewById(R.id.loader);
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.was_download_succesfull), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        db = turistListDbHelper.getWritableDatabase();
        turistListDbQuery = new TuristListDbQuery(db);
        volleyGetRequest = new VolleyGetRequest(this, db);
        startDownloadingContent();
//        downloadedContent();
    }


    @Override
    protected void onResume() {
        super.onResume();
//        boolean shouldDownload = true;
//        if (isNetworkAvailable()) {
//            Intent intent = getIntent();
//            shouldDownload = intent.getBooleanExtra("startUpdate", false);
//        } else {
//            textView.setText(getString(R.string.no_internet_connection_downloader));
//        }
//
//        if (shouldDownload) {
//            registerReceiver(receiver, new IntentFilter(
//                    DownloadService.NOTIFICATION));
//            downloadConent("1");
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt(DownloadService.RESULT);
                if (resultCode == RESULT_OK) {
                    progressBar.setProgress(bundle.getInt(DownloadService.COUNTER));
                    String message = bundle.getInt(DownloadService.COUNTER) + "/" + bundle.getInt(DownloadService.COUNTERMAX);
                    if (bundle.getString(DownloadService.DIRECTORY).equals("audio")) {
                        InsertPositionToList.insertAudioUri(db, bundle.getString(DownloadService.FILEPATH), bundle.getString(DownloadService.FILENAME), bundle.getString(DownloadService.TYPE_ID));
                    } else if (bundle.getString(DownloadService.DIRECTORY).equals("picture")) {
                        InsertPositionToList.insertPictureUri(db, bundle.getString(DownloadService.FILEPATH), bundle.getString(DownloadService.FILENAME), bundle.getString(DownloadService.TYPE_ID));
                    } else {
                        InsertPositionToList.insertVideoUri(db, bundle.getString(DownloadService.FILEPATH), bundle.getString(DownloadService.FILENAME), bundle.getString(DownloadService.TYPE_ID));
                    }
                    if ((bundle.getInt(DownloadService.COUNTER) == bundle.getInt(DownloadService.COUNTERMAX))) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Integer test = Integer.parseInt(bundle.getString(DownloadService.TYPE_ID));
                        test++;
                        if (test <= 4)
                            downloadConent(test.toString());
                        else {
                            textView.setText(getString(R.string.succesfull_download_downloader));
                            goToMainButton.setVisibility(View.VISIBLE);
                            unregisterReceiver(receiver);
                            editor.putInt(getString(R.string.was_download_succesfull), 4);
                            editor.commit();
                        }
                    }

                } else {
                    unregisterReceiver(receiver);
                    reCreateTable();
                    editor.putInt(getString(R.string.was_download_succesfull), 0);
                    editor.commit();
                    textView.setText(getString(R.string.download_failed_downloader));
                    progressBar.setVisibility(View.GONE);

                }
            }
        }
    };


    private void downloadConent(String typeId) {
        Cursor cursor;
        int progressStatus = 0;

        cursor = turistListDbQuery.getAudioCursor(typeId);
        switch (typeId) {
            case "1":
                textView.setText(getString(R.string.download_tourist_path));
                break;
            case "2":
                textView.setText(getString(R.string.download_oaza_path));
                break;
            case "3":
                textView.setText(getString(R.string.download_house_path));
                break;
            case "4":
                textView.setText(getString(R.string.download_advanced_path));
                break;
        }

        progressBar.setMax(cursor.getCount() * 3);
        progressBar.setVisibility(View.VISIBLE);
        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(cursor.getColumnIndex("AUDIO"));
                String mUrl = "http://assets.dnc.x25.pl/audio/" + data;
                Intent intent = new Intent(this, DownloadService.class);
                // add infos for the service which file to download and where to store
                intent.putExtra(DownloadService.FILENAME, data);
                intent.putExtra(DownloadService.URL,
                        mUrl);
                intent.putExtra(DownloadService.COUNTERMAX, cursor.getCount() * 3);
                intent.putExtra(DownloadService.DIRECTORY, "audio");
                intent.putExtra(DownloadService.TYPE_ID, typeId);
                intent.putExtra(DownloadService.COUNTER, progressStatus++);
                startService(intent);
            } while (cursor.moveToNext());
        }
        cursor = turistListDbQuery.getPictureCursor(typeId);
        if (cursor.moveToFirst()) {
            do {

                String data = cursor.getString(cursor.getColumnIndex("PICTURE"));
                String mUrl = "http://assets.dnc.x25.pl/foto/" + data;
                Intent intent = new Intent(this, DownloadService.class);
                // add infos for the service which file to download and where to store
                intent.putExtra(DownloadService.FILENAME, data);
                intent.putExtra(DownloadService.URL,
                        mUrl);
                intent.putExtra(DownloadService.COUNTERMAX, cursor.getCount() * 3);
                intent.putExtra(DownloadService.DIRECTORY, "picture");
                intent.putExtra(DownloadService.TYPE_ID, typeId);
                intent.putExtra(DownloadService.COUNTER, progressStatus++);
                startService(intent);
            } while (cursor.moveToNext());
        }
        cursor = turistListDbQuery.getVideoCursor(typeId);
        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(cursor.getColumnIndex("VIDEO"));
                if (data == null) {
                    data = "null";
                }
                String mUrl = "http://assets.dnc.x25.pl/video/" + data;
                Intent intent = new Intent(this, DownloadService.class);
                // add infos for the service which file to download and where to store
                intent.putExtra(DownloadService.FILENAME, data);
                intent.putExtra(DownloadService.URL,
                        mUrl);
                intent.putExtra(DownloadService.COUNTERMAX, cursor.getCount() * 3);
                intent.putExtra(DownloadService.DIRECTORY, "video");
                intent.putExtra(DownloadService.COUNTER, progressStatus++);
                intent.putExtra(DownloadService.TYPE_ID, typeId);
                startService(intent);
            } while (cursor.moveToNext());
        }
        cursor.close();

    }


    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(byte[] response) {

    }

    private void startDownloading() {
        volleyGetRequest.insertCurrentDbVersionToSharedPreferences(this, LOCAL_DATABASE_VERSION);
        reCreateTable();
        loader.setVisibility(View.VISIBLE);
        volleyGetRequest.getNameAndPosition(Arrays.asList(1, 2, 3, 4), loader, this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.accept_download_button:
                if (!isNetworkAvailable()) {
                    Toast.makeText(this, getString(R.string.toast_turn_internet_on_downloader), Toast.LENGTH_SHORT).show();
                    break;
                }
                registerReceiver(receiver, new IntentFilter(
                        DownloadService.NOTIFICATION));
                downloadConent("1");
                acceptButton.setVisibility(View.GONE);
                rejectButton.setVisibility(View.GONE);

                break;
            case R.id.reject_download_button:
                reCreateTable();
                finish();
                moveTaskToBack(true);
                break;
            case R.id.go_to_main_activity:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.launch_downloader_button:
                if (isNetworkAvailable()) {
                    startDownloading();
                } else {
                    Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.exit_app_button:
                finish();
                moveTaskToBack(true);
                break;
        }
    }

    private void reCreateTable() {
        db.execSQL("DROP TABLE IF EXISTS " + TouristListContract.TouristListEntry.TABLE_NAME);
        db.execSQL("CREATE TABLE " + TouristListContract.TouristListEntry.TABLE_NAME + " (" +
                TouristListContract.TouristListEntry._ID + " INTEGER PRIMARY KEY," +
                TouristListContract.TouristListEntry.COLUMN_POSITION + " NUMBER," +
                TouristListContract.TouristListEntry.COLUMN_AUDIO + " TEXT," +
                TouristListContract.TouristListEntry.COLUMN_NAME + " TEXT," +
                TouristListContract.TouristListEntry.COLUMN_AUDIO_URI + " TEXT," +
                TouristListContract.TouristListEntry.COLUMN_PICTURE + " TEXT," +
                TouristListContract.TouristListEntry.COLUMN_PICTURE_URI + " TEXT," +
                TouristListContract.TouristListEntry.COLUMN_VIDEO + " TEXT," +
                TouristListContract.TouristListEntry.COLUMN_VIDEO_URI + " TEXT," +
                TouristListContract.TouristListEntry.COLUMN_IS_ACTIVE + " BOOLEAN," +
                TouristListContract.TouristListEntry.COLUMN_CAN_TAKE_PHOTO + " BOOLEAN," +
                TouristListContract.TouristListEntry.COLUMN_TYPE_ID + " NUMBER);");
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void startDownloadingContent() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SHOULD_UPDATE_AFTER_FIRST_DOWNLOAD_1, true);
        editor.putBoolean(SHOULD_UPDATE_AFTER_FIRST_DOWNLOAD_2, true);
        editor.putBoolean(SHOULD_UPDATE_AFTER_FIRST_DOWNLOAD_3, true);
        editor.putBoolean(SHOULD_UPDATE_AFTER_FIRST_DOWNLOAD_4, true);
        editor.apply();
        if (isNetworkAvailable()) {
            startDownloading();
        }
    }

    private void downloadedContent() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.was_download_succesfull), Context.MODE_PRIVATE);
        int isSuccesfulMain = sharedPreferences.getInt(getString(R.string.was_download_succesfull), 0);
        if (isNetworkAvailable()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            if (prefs.getInt(LOCAL_DATABASE_VERSION, 0) < prefs.getInt(DATABASE_VERSION, 0)) {
                TuristListDbQuery turistListDbQuery = new TuristListDbQuery(db);
                List<String> list = turistListDbQuery.getActiveAudio();
                volleyGetRequest.getActiveAudioFromServerTable(list, findViewById(android.R.id.content), this);
                editor.putBoolean(SHOULD_UPDATE_POSITION_1, true);
                editor.putBoolean(SHOULD_UPDATE_POSITION_2, true);
                editor.putBoolean(SHOULD_UPDATE_POSITION_3, true);
                editor.putBoolean(SHOULD_UPDATE_POSITION_4, true);
                editor.apply();
            }
            volleyGetRequest.insertCurrentDbVersionToSharedPreferences(this, DATABASE_VERSION);
        } else if (!isNetworkAvailable() && isSuccesfulMain != 4) {
            TextView noInternetTextView = findViewById(R.id.text_view_no_internet);
            Button downloadButton = findViewById(R.id.launch_downloader_button);
            Button closeAppButton = findViewById(R.id.exit_app_button);
            downloadButton.setOnClickListener(this);
            closeAppButton.setOnClickListener(this);
            noInternetTextView.setVisibility(View.VISIBLE);
            downloadButton.setVisibility(View.VISIBLE);
            closeAppButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, getString(R.string.back_button_toast_on_downloader), Toast.LENGTH_SHORT).show();
    }
}
