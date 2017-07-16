package com.enioka.scanner.demo;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.enioka.scanner.LaserScanner;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerConnectionHandler;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.camera.ZbarScanView;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.sdk.zbar.ScannerZbarViewImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Scanner.ScannerDataCallback, Scanner.ScannerStatusCallback, ScannerConnectionHandler {
    private Scanner s;
    private String keyboardInput = "";


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Init and destruction
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onResume() {
        super.onResume();

        // Set content immediately - that way our callbacks can draw on the layout.
        setContentView(R.layout.activity_main);

        // init laser scanner search. If none found this will go to the camera.
        LaserScanner.getLaserScanner(this, this, new ScannerSearchOptions());
    }

    @Override
    protected void onPause() {
        if (s != null) {
            this.s.disconnect();
            this.s = null;
        }
        super.onPause();
    }

    @Override
    public void scannerConnectionProgress(String providerKey, String scannerKey, String message) {
        ((TextView) findViewById(R.id.text_scanner_status)).setText(providerKey + " reports " + message);
    }

    @Override
    public void scannerCreated(String providerKey, String scannerKey, Scanner s) {
        this.s = s;
        s.initialize(this, null, this, this, Scanner.Mode.BATCH);

        // Enable possibility to manually use camera scanner.
        ImageButton bt = (ImageButton) findViewById(R.id.photo);
        if (bt != null) {
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.s.disconnect();
                    MainActivity.this.s = null;
                    initCamera();
                }
            });
        }
    }

    @Override
    public void noScannerAvailable() {
        if (getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
            // We may have a BT keyboard connected
            onStatusChanged("Using Bluetooth scanner or keyboard");
        } else {
            // In that case try to connect to a camera.
            initCamera();
        }
    }

    private void initCamera() {
        setContentView(R.layout.activity_main_alt);
        ZbarScanView zbarView = (ZbarScanView) findViewById(R.id.zbar_scan_view);
        s = new ScannerZbarViewImpl(zbarView, this);
        ((TextView) findViewById(R.id.text_last_scan)).setText(null);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Scanner callbacks
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onStatusChanged(String newStatus) {
        ((TextView) findViewById(R.id.text_scanner_status)).setText(newStatus);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Data callback
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onData(List<Barcode> data) {
        String res = "";
        for (Barcode b : data) {
            res += b.getBarcode() + "\n" + (b.getBarcodeType() != null ? b.getBarcodeType().code : "keyboard") + "\n";
        }
        ((TextView) findViewById(R.id.text_last_scan)).setText(res);
        //Common.beepScanSuccessful();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Keyboard input
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            // The ending CR is most often a simple UP without DOWN.
            Barcode b = new Barcode(this.keyboardInput, null);
            this.onData(new ArrayList<>(Arrays.asList(b)));
            this.keyboardInput = "";
        } else if (event.getAction() == KeyEvent.ACTION_UP && !event.isPrintingKey()) {
            // Skip un-printable characters.
            return super.onKeyDown(event.getKeyCode(), event);
        } else if (event.getAction() == KeyEvent.ACTION_DOWN) {
            // Only use DOWN event - UP events are not synchronized with SHIFT events.
            this.keyboardInput += (char) event.getKeyCharacterMap().get(event.getKeyCode(), event.getMetaState());
        }
        return true;
    }
}
