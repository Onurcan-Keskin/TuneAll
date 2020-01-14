package com.onurcankeskin.looneytune;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class SplashScreen extends AppCompatActivity {

    public static final int SPLASH_DELAY = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_splash_screen);

        new Thread(new Runnable() {
            @Override
            public void run() {
                doWork();
                startApp();
                finish();
            }
        }).start();
    }

    private void doWork() {
        for (int prog = 0; prog < 100; prog += 1) {
            try {
                Thread.sleep(SPLASH_DELAY);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Splash Error", e.getMessage());
            }
        }
    }

    private void startApp() {
        Intent intent = new Intent(SplashScreen.this, Intro_Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
