package com.onurcankeskin.looneytune;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.util.Locale;

public class Intro_Activity extends AppCompatActivity {

    private Intent intent;
    private String currentlang = "en", getCurrentlang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(Intro_Activity.this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            openMainActivity();
        }

        setTheme(R.style.dark_theme);
        setContentView(R.layout.activity_intro);

        findViewById(R.id.intro_permission_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions();
            }
        });

        currentlang = getIntent().getStringExtra(currentlang);
        findViewById(R.id.lng_tr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intro_Activity.this.setLocale("tr");
                finish();
            }
        });

        findViewById(R.id.lng_en).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intro_Activity.this.setLocale("en");
                finish();
            }
        });
    }

    private void openMainActivity() {
        intent = new Intent(Intro_Activity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                11);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 11: {
                if (grantResults.length == 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                } else {
                    openMainActivity();
                }
                return;
            }
        }
    }

    public void setLocale(String localeName) {
        if (!localeName.equals(currentlang)) {
            Locale mylocale = new Locale(localeName);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration config = res.getConfiguration();
            config.locale = mylocale;
            res.updateConfiguration(config, dm);
            Intent refresh = new Intent(this, Intro_Activity.class);
            refresh.putExtra(currentlang, localeName);
            startActivity(refresh);
            finish();
        } else {
            Toast.makeText(Intro_Activity.this, R.string.error_lang_selected, Toast.LENGTH_SHORT).show();
        }
    }
}
