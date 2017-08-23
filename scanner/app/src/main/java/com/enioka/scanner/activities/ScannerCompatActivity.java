package com.enioka.scanner.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.enioka.scanner.LaserScanner;
import com.enioka.scanner.R;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerConnectionHandler;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.camera.ZbarScanView;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.helpers.Common;
import com.enioka.scanner.sdk.zbar.ScannerZbarViewImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A helper activity which implements all scan functions: laser, camera, HID.<br><br>Basic usage is trivial : just inherit this class, and that's all.<br>
 * You may want to override {@link #onData(List)} to get barcode data, and {@link #onStatusChanged(String)} to display status messages from the scanners.<br>
 * It is also useful to change  inside onCreate {@link #layoutIdLaser} and {@link #layoutIdCamera} to a layout ID (from R.id...) corresponding to your application.
 * By default, a basic test layout is provided.<br>
 * Also, {@link #zbarViewId} points to the ZBar view inside your Camera layout.
 */
public class ScannerCompatActivity extends AppCompatActivity implements Scanner.ScannerDataCallback, Scanner.ScannerStatusCallback, ScannerConnectionHandler, Scanner.ScannerInitCallback {
    protected final static String LOG_TAG = "ScannerActivity";

    /**
     * If set to false, ScannerCompatActivity will behave like an standard AppCompatActivity
     */
    protected boolean willScan = true;

    /**
     * Scanner instance ; if none is set, activity will instantiate one
     */
    protected Scanner scanner;

    /**
     * The layout to use when using a laser or external keyboard.
     */
    protected int layoutIdLaser = R.layout.activity_main;
    /**
     * The layout to use when using ZBar.
     */
    protected int layoutIdCamera = R.layout.activity_main_alt;
    /**
     * The ID of the {@link ZbarScanView} inside the {@link #layoutIdCamera} layout.
     */
    protected int zbarViewId = R.id.zbar_scan_view;

    /**
     * The ID of the optional ImageButton on which to press to toggle the flashlight/illumination.
     */
    protected int flashlightViewId = R.id.scanner_flashlight;


    private String keyboardInput = "";
    protected ManualInputFragment df;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Init and destruction
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (willScan) {
            Common.askForPermission(this);
        }
    }

    @Override
    protected void onResume() {
        Log.i(LOG_TAG, "Resuming scanner activity - scanner will be connected");
        super.onResume();

        if (willScan) {
            // Set content immediately - that way our callbacks can draw on the layout.
            setContentView(layoutIdLaser);

            if (findViewById(R.id.scanner_text_last_scan) != null) {
                ((TextView) findViewById(R.id.scanner_text_last_scan)).setText(null);
            }
            if (findViewById(R.id.scanner_text_scanner_status) != null) {
                ((TextView) findViewById(R.id.scanner_text_scanner_status)).setText(null);
            }

            if (findViewById(R.id.scanner_bt_camera) != null) {
                findViewById(R.id.scanner_bt_camera).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        initCamera();
                    }
                });
            }

            // If no scanner instance exists, create one
            if (scanner == null) {
                // init laser scanner search. If none found this will go to the camera.
                LaserScanner.getLaserScanner(this, this, new ScannerSearchOptions());
            } else {
                // TODO
            }
        }
    }

    @Override
    protected void onPause() {
        Log.i(LOG_TAG, "Scanner activity is being paused");
        if (willScan && scanner != null) {
            Log.i(LOG_TAG, "Scanner is being disconnected");
            this.scanner.disconnect();
            this.scanner = null;
        }
        super.onPause();
    }

    @Override
    public void scannerConnectionProgress(String providerKey, String scannerKey, String message) {
        onStatusChanged(providerKey + " reports " + message);
    }

    @Override
    public void scannerCreated(String providerKey, String scannerKey, final Scanner s) {
        Log.d(LOG_TAG, "View has received a new scanner - key is: " + scannerKey);
        this.scanner = s;
        s.initialize(this, this, this, this, Scanner.Mode.BATCH);

        if (findViewById(flashlightViewId) != null) {
            final ImageButton flashlight = (ImageButton) findViewById(flashlightViewId);
            displayTorch(s, flashlight);

            if (s.supportsIllumination()) {
                flashlight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        s.toggleIllumination();
                        displayTorch(s, flashlight);
                    }
                });
            }
        }

        if (findViewById(R.id.scanner_bt_keyboard) != null) {
            final View bt = findViewById(R.id.scanner_bt_keyboard);
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ScannerCompatActivity.this.scanner.pause();
                    df = ManualInputFragment.newInstance();
                    df.show(getSupportFragmentManager(), "manual");
                }
            });
        }
    }

    @Override
    public void onConnectionSuccessful() {
        onStatusChanged(getResources().getString(R.string.scanner_status_initialized));
    }

    @Override
    public void onConnectionFailure() {
        onStatusChanged(getResources().getString(R.string.scanner_status_initialization_failure));
    }

    @Override
    public void noScannerAvailable() {
        if (getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
            // We may have a BT keyboard connected
            Log.i(LOG_TAG, "No real scanner available but BT keyboard connected");
            onStatusChanged(getResources().getString(R.string.scanner_using_bt_keyboard));
        } else {
            // In that case try to connect to a camera.
            initCamera();
        }
    }

    protected void initCamera() {
        Log.i(LOG_TAG, "Giving up on laser, going to camera");
        if (!Common.hasCamera(this)) {
            Log.i(LOG_TAG, "No camera available on device");
            Toast.makeText(this, R.string.scanner_status_no_camera, Toast.LENGTH_SHORT).show();
            return;
        }
        if (this.scanner != null) {
            this.scanner.disconnect();
            this.scanner = null;
        }

        setContentView(layoutIdCamera);

        ZbarScanView zbarView = (ZbarScanView) findViewById(zbarViewId);
        scanner = new ScannerZbarViewImpl(zbarView, this);
        scannerCreated("camera", "camera", scanner);

        if (findViewById(R.id.scanner_text_last_scan) != null) {
            ((TextView) findViewById(R.id.scanner_text_last_scan)).setText(null);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Scanner callbacks
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onStatusChanged(String newStatus) {
        Log.d(LOG_TAG, "Status change: " + newStatus);
        if (findViewById(R.id.scanner_text_scanner_status) != null) {
            TextView tv = (TextView) findViewById(R.id.scanner_text_scanner_status);
            tv.setText(newStatus + "\n" + tv.getText());
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Data callback
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onData(List<Barcode> data) {
        String res = "";
        for (Barcode b : data) {
            Log.d(LOG_TAG, "Received barcode from scanner: " + b.getBarcode() + " - " + b.getBarcodeType().code);
            res += b.getBarcode() + "\n" + b.getBarcodeType().code + "\n";
        }
        if (findViewById(R.id.scanner_text_last_scan) != null) {
            ((TextView) findViewById(R.id.scanner_text_last_scan)).setText(res);
        }
        if (df != null) {
            df = null;
            this.scanner.resume();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Keyboard input
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!willScan) {
            return false;
        }

        if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            // The ending CR is most often a simple UP without DOWN.
            Barcode b = new Barcode(this.keyboardInput, BarcodeType.UNKNOWN);
            this.onData(new ArrayList<>(Collections.singleton(b)));
            this.keyboardInput = "";
        } else if (!event.isPrintingKey()) {
            // Skip un-printable characters.
            return super.onKeyDown(event.getKeyCode(), event);
        } else if (event.getAction() == KeyEvent.ACTION_DOWN) {
            // Only use DOWN event - UP events are not synchronized with SHIFT events.
            this.keyboardInput += (char) event.getKeyCharacterMap().get(event.getKeyCode(), event.getMetaState());
        }

        return true;
    }

    /**
     * Display the torch button "on" or "off" is the device have capability.
     **/
    void displayTorch(Scanner scanner, ImageButton flashlight) {
        if (!scanner.supportsIllumination()) {
            flashlight.setVisibility(View.GONE);
        } else {
            flashlight.setVisibility(View.VISIBLE);
        }

        boolean isOn = scanner.isIlluminationOn();
        int iconId = isOn ? R.drawable.icn_flash_off_on : R.drawable.icn_flash_off;
        flashlight.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), iconId));
    }
}
