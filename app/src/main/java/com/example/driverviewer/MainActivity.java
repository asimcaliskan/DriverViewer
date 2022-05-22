package com.example.driverviewer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int MULTIPLE_PERMISSIONS = 958;
    private String URL = "http://{ip}:{port}";

    private final String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    protected TextInputEditText textInputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();

        //URL SETTING <
        textInputEditText = findViewById(R.id.textinputedittext_url);
        textInputEditText.setText(URL);
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                URL = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        //URL SETTING >

        //BUTTONS AND THEIR ASSIGNMENTS STARTS
        Button streamButton = findViewById(R.id.button_stream);
        streamButton.setOnClickListener(view -> {
            Intent changeActivityIntent = new Intent(MainActivity.this, StreamActivity.class);
            changeActivityIntent.putExtra("URL", URL);
            MainActivity.this.startActivity(changeActivityIntent);
        });

        Button mapButton = findViewById(R.id.button_map);
        mapButton.setOnClickListener(view -> {
            Intent changeActivityIntent = new Intent(MainActivity.this, MapActivity.class);
            changeActivityIntent.putExtra("URL", URL);
            MainActivity.this.startActivity(changeActivityIntent);
        });
        //BUTTON ASSIGNMENTS ENDS
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
/*
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //getCallDetails(); // Now you call here what ever you want :)
                } else {
                    String perStr = "";
                    for (String per : permissions) {
                        perStr += "\n" + per;
                    }   // permissions list of don't granted permission
                }
                return;
            }
        }
*/
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
}