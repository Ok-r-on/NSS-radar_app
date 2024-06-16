package com.example.nss;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.nss.volunteer.Volunteer;

public class MainActivity extends AppCompatActivity {
    private static final long SPLASH_TIMEOUT = 3500;

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_FIRST_TIME_LOGIN = "FirstTimeLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(() -> {
        }, SPLASH_TIMEOUT);
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        boolean firstTimeLogin = sharedPref.getBoolean(KEY_FIRST_TIME_LOGIN, true);
        if (firstTimeLogin) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(KEY_FIRST_TIME_LOGIN, false);
            editor.apply();
            startActivity(new Intent(this, LogInActivity.class));
        } else {
            // Skip login/signup UI and navigate to main activity
            Toast.makeText(getApplicationContext(), "Welcome back!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Volunteer.class));
        }
    }
}