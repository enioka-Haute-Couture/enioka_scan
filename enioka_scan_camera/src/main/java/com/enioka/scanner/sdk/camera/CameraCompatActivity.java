package com.enioka.scanner.sdk.camera;

import static com.enioka.scanner.helpers.Permissions.PERMISSIONS_CAMERA;
import static com.enioka.scanner.helpers.Permissions.hasPermissionSet;
import static com.enioka.scanner.helpers.Permissions.requestPermissionSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.enioka.scanner.activities.ManualInputFragment;
import com.enioka.scanner.activities.ManualInputItem;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.helpers.Common;
import com.enioka.scanner.sdk.camera.R;
import com.enioka.scanner.service.ScannerClient;
import com.enioka.scanner.service.ScannerServiceApi;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A camera helper activity that implements the camera scanner. <br><br>Basic usage is trivial : just inherit this class, and that's all.<br>
 * The activity will display a camera view, and will scan barcodes. The barcode data will be displayed in a text view with the ID {@code R.id.scanner_text_last_scan}.<br>
 * You may want to override the {@link #onData(List)} to get barcode data, and the {@link #onStatusChanged(Scanner, ScannerStatusCallback.Status)} to display scanner status from the camera scanner.<br>
 * It is also useful to change {@link #layoutIdCamera} to use a custom layout.
 * By default, a basic test layout is provided. <br>
 * Also, {@link #cameraViewId} points to the camera view inside your camera layout.
 */
public class CameraCompatActivity extends AppCompatActivity implements ScannerClient {
    protected final static String LOG_TAG = "CameraActivity";

    protected final static int PERMISSION_REQUEST_ID_CAMERA = 1790;

    /**
     * The layout to use when using camera scanner.
     */
    protected int layoutIdCamera = R.layout.activity_main_alt;

    /**
     * Optional camera scanner
     */
    protected CameraBarcodeScanViewScanner cameraScanner;

    /**
     * The ID of the optional ImageButton on which to press to toggle the flashlight/illumination.
     */
    protected int flashlightViewId = R.id.scanner_flashlight;

    /**
     * The ID of the {@link CameraBarcodeScanView} inside the {@link #layoutIdCamera} layout.
     */
    protected int cameraViewId = R.id.camera_scan_view;

    /**
     * The ID of the optional ImageButton on which to press to toggle the zxing/zbar camera scan library.
     */
    protected int scannerModeToggleViewId = R.id.scanner_switch_zxing;

    protected int scannerModeTogglePauseId = R.id.scanner_switch_pause;

    protected int keyboardOpenViewId = R.id.scanner_bt_keyboard;

    /**
     * An optional fragment allowing to input a value with the soft keyboard (for cases when scanners do not work).
     */
    protected ManualInputFragment manualInputFragment;

    /**
     * Auto completion items for manual input (with manualInputFragment).
     */
    protected List<ManualInputItem> autocompletionItems = new ArrayList<>();

    /**
     * How many characters should be entered before auto-completion starts.
     */
    protected int threshold = 5;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Activity lifecycle callbacks
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Camera activity is created " + this.hashCode());

        if (hasPermissionSet(this, PERMISSIONS_CAMERA)) {
            setContentView(layoutIdCamera);
        } else {
            requestPermissionSet(this, PERMISSIONS_CAMERA, PERMISSION_REQUEST_ID_CAMERA);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "Camera activity is starting " + this.hashCode());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "Camera activity is resuming " + this.hashCode());
        initCamera();
    }

    @Override
    protected void onPause() {
        Log.i(LOG_TAG, "Camera activity is being paused " + this.hashCode());
        if (cameraScanner != null) {
            cameraScanner.disconnect();
            cameraScanner = null;
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "Camera activity is being stopped " + this.hashCode());
    }

    @Override
    protected void onDestroy() {
        Log.i(LOG_TAG, "Camera activity is being destroyed " + this.hashCode());
        super.onDestroy();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Camera
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void initCamera() {
        Log.i(LOG_TAG, "Giving up on laser, going to camera");
        if (!Common.hasCamera(this)) {
            Log.i(LOG_TAG, "No camera available on device");
            Toast.makeText(this, R.string.scanner_status_no_camera, Toast.LENGTH_SHORT).show();

            // Is it the right way to finish an activity?
            finish();

            return;
        }

        if (hasPermissionSet(this, PERMISSIONS_CAMERA)) {
            // The view needs permissions BEFORE initializing. And it initializes as soon as the layout is set.
            setContentView(layoutIdCamera);
            initCameraScanner();

            // Reinit text
            if (findViewById(R.id.scanner_text_scanner_status) != null) {
                TextView tv = findViewById(R.id.scanner_text_scanner_status);
                tv.setText("");
            }
        } else {
            requestPermissionSet(this, PERMISSIONS_CAMERA, PERMISSION_REQUEST_ID_CAMERA);
        }
    }

    private void initCameraScanner() {
        if (cameraScanner != null) {
            return;
        }
        // TODO: should be in camera constructor, not here...
        CameraBarcodeScanView cameraView = findViewById(cameraViewId);
        if (cameraView == null) {
            Toast.makeText(this, R.string.scanner_status_no_camera, Toast.LENGTH_SHORT).show();
            return;
        }

        final Set<BarcodeType> symbologies = new HashSet<>();
        if (getIntent().getExtras() != null && getIntent().getExtras().getStringArray(ScannerServiceApi.EXTRA_SYMBOLOGY_SELECTION) != null) {
            for (final String symbology : Objects.requireNonNull(getIntent().getExtras().getStringArray(ScannerServiceApi.EXTRA_SYMBOLOGY_SELECTION))) {
                symbologies.add(BarcodeType.valueOf(symbology));
            }
        }
        if (symbologies.isEmpty()) {
            symbologies.add(BarcodeType.CODE128);
        }
        cameraScanner = new CameraBarcodeScanViewScanner(cameraView, new ScannerDataCallbackProxy((s, data) -> CameraCompatActivity.this.onData(data)), new ScannerStatusCallbackProxy(this), symbologies);

        if (findViewById(R.id.scanner_text_last_scan) != null) {
            ((TextView) findViewById(R.id.scanner_text_last_scan)).setText(null);
        }
        displayTorch();
        displayManualInputButton();
        displayCameraReaderToggle();
        displayCameraPauseToggle();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Permissions
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ID_CAMERA) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //goToCamera = true; // in case the activity was paused by the permission request dialog
                setContentView(layoutIdCamera);
                initCameraScanner();
            } else {
                Toast.makeText(this, R.string.scanner_status_no_camera, Toast.LENGTH_SHORT).show();
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Scanner lifecycle callbacks
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressLint("SetTextI18n") // Text is already localized, only special characters remain.
    @Override
    public void onStatusChanged(final Scanner scanner, final ScannerStatusCallback.Status newStatus) {
        if (findViewById(R.id.scanner_text_scanner_status) != null) {
            TextView tv = findViewById(R.id.scanner_text_scanner_status);
            tv.setText((scanner == null ? "" : (scanner.getProviderKey() + ": ")) + newStatus + " --- " + newStatus.getLocalizedMessage(this) + "\n" + tv.getText());
        }
    }
    @Override
    public void onScannerInitEnded(int count) {
        // Do nothing
    }
    @Override
    public void onProviderDiscoveryEnded() {
        // Do nothing
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Scanner data callback
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onData(List<Barcode> data) {
        StringBuilder res = new StringBuilder();
        for (Barcode b : data) {
            Log.d(LOG_TAG, "Received barcode from scanner: " + b.getBarcode() + " - " + b.getBarcodeType().code);
            res.append(b.getBarcode()).append("\n").append(b.getBarcodeType().code).append("\n");
        }
        if (findViewById(R.id.scanner_text_last_scan) != null) {
            ((TextView) findViewById(R.id.scanner_text_last_scan)).setText(res.toString());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Button and input initialization
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Display a manual input (keyboard) button for manual input.
     */
    private void displayManualInputButton() {
        final View bt = findViewById(keyboardOpenViewId);
        if (bt == null) {
            return;
        }

        bt.setOnClickListener(view -> {
            manualInputFragment = ManualInputFragment.newInstance();
            manualInputFragment.setAutocompletionItems(autocompletionItems, threshold);
            manualInputFragment.setDialogInterface(new DialogInterface() {
                @Override
                public void cancel() {

                }

                @Override
                public void dismiss() {

                }
            });
            manualInputFragment.show(getSupportFragmentManager(), "manual");
        });
    }

    /**
     * Display the torch button "on" or "off" is the device has capability.
     **/
    private void displayTorch() {
        final ImageButton flashlight = findViewById(flashlightViewId);
        if (findViewById(flashlightViewId) == null) {
            return;
        }

        toggleTorch();

        if (cameraScanner != null) {
            flashlight.setOnClickListener(v -> {
                cameraScanner.toggleIllumination();
                toggleTorch();
            });
        }
    }

    private void toggleTorch() {
        final ImageButton flashlight = findViewById(flashlightViewId);
        if (findViewById(flashlightViewId) == null) {
            return;
        }

        flashlight.setVisibility(View.VISIBLE);

        boolean isOn = cameraScanner != null && cameraScanner.isIlluminationOn();
        int iconId = isOn ? R.drawable.icn_flash_off_on : R.drawable.icn_flash_off;

        final int newColor = getResources().getColor(R.color.flashButtonColor);
        flashlight.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);
        flashlight.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), iconId));
    }

    private void displayCameraReaderToggle() {
        final Switch toggle = findViewById(scannerModeToggleViewId);
        if (toggle == null) {
            return;
        }

        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.i(LOG_TAG, "Changing reader mode");
            CameraBarcodeScanView cameraView = findViewById(cameraViewId);
            cameraView.setReaderMode(isChecked ? CameraReader.ZXING : CameraReader.ZBAR);
        });
    }

    private void displayCameraPauseToggle() {
        final Switch toggle = findViewById(scannerModeTogglePauseId);
        if (toggle == null) {
            return;
        }

        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.i(LOG_TAG, "Toggling camera pause");
            CameraBarcodeScanView cameraView = findViewById(cameraViewId);
            if (isChecked) {
                cameraView.pauseCamera();
            } else {
                cameraView.resumeCamera();
            }
        });
    }
}
