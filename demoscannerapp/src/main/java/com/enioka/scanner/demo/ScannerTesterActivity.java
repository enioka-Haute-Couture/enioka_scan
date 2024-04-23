package com.enioka.scanner.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.enioka.scanner.activities.ScannerCompatActivity;
import com.enioka.scanner.data.Barcode;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

public class ScannerTesterActivity extends ScannerCompatActivity {
    private static final String LOG_TAG = "ScannerTesterActivity";

    private Uri logFileUri = null;
    private int WRITE_REQUEST_CODE = 123;
    private int WRITE_PERMISSION_REQUEST_CODE = 124;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            createLog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WRITE_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createLog();
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void createLog() {
        if (!isExternalStorageWritable()) {
            return;
        }

        String fileName = (new Date()).getTime() + "_scanner_test_log.csv";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            logFileUri = data.getData();
            if (logFileUri != null) {
                Log.i(LOG_TAG, "Log file will be written at: " + logFileUri.toString());
            }
        }
    }

    private synchronized void writeResultToLog(Barcode data) {
        String dataLine = (new Date()).getTime() + "," + data.getBarcode() + "," + data.getBarcodeType().code;

        try (OutputStream os = getContentResolver().openOutputStream(logFileUri, "wa")) {
            if (os == null) {
                return;
            }
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, Charset.forName("UTF8")));
            writer.write(dataLine, 0, dataLine.length());
            writer.newLine();
            writer.flush();
            Log.d(LOG_TAG, dataLine);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onData(List<Barcode> data) {
        super.onData(data);

        if (logFileUri != null) {
            for (Barcode br : data) {
                writeResultToLog(br);
            }
        }
    }
}
