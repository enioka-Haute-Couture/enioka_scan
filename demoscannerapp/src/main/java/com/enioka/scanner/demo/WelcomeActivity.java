package com.enioka.scanner.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void onClickBt1(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onClickBt3(View v) {
        Intent intent = new Intent(this, Scan2Activity.class);
        startActivity(intent);
    }

    public void onClickBt4(View v) {
        Intent intent = new Intent(this, ScannerTesterActivity.class);
        startActivity(intent);
    }
}
