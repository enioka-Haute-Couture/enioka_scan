package com.enioka.scanner.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.enioka.scanner.demo.slip.SlipScanActivity;

public class WelcomeActivity extends AppCompatActivity {
    protected final static int PERMISSION_REQUEST_ID_CAMERA_SLIP_SCAN = 1790;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void onClickBt1(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onClickBt2(View v) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_ID_CAMERA_SLIP_SCAN);
        } else {
            openSlipScan();
        }
    }

    public void onClickBt3(View v) {
        Intent intent = new Intent(this, Scan2Activity.class);
        startActivity(intent);
    }

    public void onClickBt4(View v) {
        Intent intent = new Intent(this, ScannerTesterActivity.class);
        startActivity(intent);
    }

    private void openSlipScan() {
        Intent intent = new Intent(this, SlipScanActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_ID_CAMERA_SLIP_SCAN: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSlipScan();
                } else {
                    Toast.makeText(this, com.enioka.scanner.R.string.scanner_status_no_camera, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}
