package com.enioka.scanner.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.enioka.scanner.LaserScanner;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.camera.ZbarScanView;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.exc.NoLaserScanner;
import com.enioka.scanner.sdk.zbar.ScannerZbarViewImpl;

import java.util.List;

public class MainActivity extends AppCompatActivity implements Scanner.ScannerDataCallback, Scanner.ScannerStatusCallback {

    private TextView dataView;
    private Scanner s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        s = new LaserScanner();
        try {
            setContentView(R.layout.activity_main);
            s.initialize(this, null, this, this, Scanner.Mode.BATCH);
        } catch (NoLaserScanner e) {
            setContentView(R.layout.activity_main_alt);
            ZbarScanView zbarView = (ZbarScanView) findViewById(R.id.zbar_scan_view);
            s = new ScannerZbarViewImpl(zbarView, this);
        }

        dataView = (TextView) findViewById(R.id.text_last_scan);
        dataView.setText(null);

        ImageButton bt = (ImageButton) findViewById(R.id.photo);
        if (bt != null) {
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    s.disconnect();
                    setContentView(R.layout.activity_main_alt);
                    ZbarScanView zbarView = (ZbarScanView) findViewById(R.id.zbar_scan_view);
                    s = new ScannerZbarViewImpl(zbarView, MainActivity.this);
                    dataView = (TextView) findViewById(R.id.text_last_scan);
                    dataView.setText(null);
                }
            });
        }
    }

    @Override
    public void onData(List<Barcode> data) {
        String res = "";
        for (Barcode b : data) {
            res += b.getBarcode() + "\n" + b.getBarcodeType().code + "\n";
        }
        dataView.setText(res);
        s.beepScanSuccessful();
    }

    @Override
    public void onStatusChanged(String newStatus) {
        ((TextView) findViewById(R.id.text_scanner_status)).setText(newStatus);
    }
}
