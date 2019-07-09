package com.maciek.v2.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.maciek.v2.R;

import static com.maciek.v2.Activities.MediaPlayerActivity.TRACK_PROGRESS;
import static com.maciek.v2.Activities.TrackListActivity.TITLE;
import static com.maciek.v2.Activities.TrackListActivity.TYPE_ID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, Response.Listener<byte[]>, Response.ErrorListener {

    private Button touristButton;
    private Button homeChurchButton;
    private Button oazaYouthButton;
    private Button advancedButton;
    private Button helpButton;

    private long lastClickTime = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        touristButton = findViewById(R.id.button_tourist);
        homeChurchButton = findViewById(R.id.button_home_church);
        oazaYouthButton = findViewById(R.id.button_oaza_youth);
        advancedButton = findViewById(R.id.button_advanced);
        helpButton = findViewById(R.id.button_help);
        advancedButton.setOnClickListener(this);
        oazaYouthButton.setOnClickListener(this);
        touristButton.setOnClickListener(this);
        homeChurchButton.setOnClickListener(this);
        helpButton.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onClick(View view) {
        Intent mIntent = new Intent(this, MediaPlayerActivity.class);
        mIntent.putExtra(TRACK_PROGRESS, 0);
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            Toast.makeText(this, getText(R.string.click_on_button_too_fast), Toast.LENGTH_SHORT).show();
            return;
        }

        lastClickTime = SystemClock.elapsedRealtime();

        switch (view.getId()) {
            case R.id.button_tourist:
                mIntent.putExtra(TITLE, "turysta-wstep.mp3");
                mIntent.putExtra(TYPE_ID, "1");
                startActivity(mIntent);
                break;
            case R.id.button_home_church:
                mIntent.putExtra(TITLE, "domowy-kosciol-wstep.mp3");
                mIntent.putExtra("type_id", "3");
                startActivity(mIntent);
                break;
            case R.id.button_advanced:
                mIntent.putExtra(TITLE, "moderator-wstep.mp3");
                mIntent.putExtra("type_id", "4");
                startActivity(mIntent);
                break;
            case R.id.button_oaza_youth:
                mIntent.putExtra(TITLE, "oazowicz-wstep.mp3");
                mIntent.putExtra("type_id", "2");
                startActivity(mIntent);
                break;
            case R.id.button_help:
                if (isNetworkAvailable()) {
                    startActivity(new Intent(this, WebViewActivity.class));
                } else {
                    Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(byte[] response) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.was_download_succesfull), Context.MODE_PRIVATE);
        int isSuccesful = sharedPreferences.getInt(getString(R.string.was_download_succesfull), 0);
        if (isSuccesful == 4) {
            touristButton.setVisibility(View.VISIBLE);
            homeChurchButton.setVisibility(View.VISIBLE);
            oazaYouthButton.setVisibility(View.VISIBLE);
            advancedButton.setVisibility(View.VISIBLE);
            helpButton.setVisibility(View.VISIBLE);

        } else {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
