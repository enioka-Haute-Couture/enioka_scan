package com.enioka.scanner.activities;

import static com.enioka.scanner.helpers.Permissions.PERMISSIONS_BT;
import static com.enioka.scanner.helpers.Permissions.PERMISSIONS_CAMERA;
import static com.enioka.scanner.helpers.Permissions.hasPermissionSet;
import static com.enioka.scanner.helpers.Permissions.requestPermissionSet;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.enioka.scanner.R;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerLedColor;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.camera.CameraBarcodeScanView;
import com.enioka.scanner.camera.CameraReader;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.helpers.Common;
import com.enioka.scanner.sdk.camera.CameraBarcodeScanViewScanner;
import com.enioka.scanner.service.ScannerClient;
import com.enioka.scanner.service.ScannerService;
import com.enioka.scanner.service.ScannerServiceApi;
import com.enioka.scanner.service.ScannerServiceBinderHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper activity which implements all scan functions: laser, camera, HID.<br><br>Basic usage is trivial : just inherit this class, and that's all.<br>
 * You may want to override {@link #onData(List)} to get barcode data, and {@link #onStatusChanged(Scanner, ScannerStatusCallback.Status)} to display status messages from the scanners.<br>
 * It is also useful to change  inside onCreate {@link #layoutIdLaser} and {@link #layoutIdCamera} to a layout ID (from R.id...) corresponding to your application.
 * By default, a basic test layout is provided.<br>
 * Also, {@link #cameraViewId} points to the camera view inside your camera layout.
 */
public class ScannerCompatActivity extends AppCompatActivity implements ScannerClient {
    protected final static String LOG_TAG = "ScannerActivity";
    protected final static int PERMISSION_REQUEST_ID_CAMERA = 1790;
    protected final static int PERMISSION_REQUEST_ID_BT = 1792;

    /**
     * Don't start camera mode, even if no lasers are available
     */
    protected boolean laserModeOnly = false;

    /**
     * If set to false, ScannerCompatActivity will behave like an standard AppCompatActivity
     */
    protected boolean enableScan = true;

    /**
     * Helper to go directly to camera (used on reload, such as after permission change).
     */
    protected boolean goToCamera = false;

    /**
     * Use bluetooth to look for scanners (if available).
     */
    protected boolean useBluetooth = true;

    /**
     * The layout to use when using a laser or external keyboard.
     */
    protected int layoutIdLaser = R.layout.activity_main;
    /**
     * The layout to use when using camera scanner.
     */
    protected int layoutIdCamera = R.layout.activity_main_alt;
    /**
     * Use {@link #cameraViewId} instead.
     */
    @Deprecated
    protected Integer zbarViewId = null;
    /**
     * The ID of the {@link CameraBarcodeScanView} inside the {@link #layoutIdCamera} layout.
     */
    protected int cameraViewId = R.id.camera_scan_view;

    /**
     * The ID of the optional ImageButton on which to press to toggle the flashlight/illumination.
     */
    protected int flashlightViewId = R.id.scanner_flashlight;

    /**
     * The ID of the optional ImageButton on which to press to toggle the flashlight/illumination.
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

    /**
     * Actual access to the scanners.
     */
    protected ScannerServiceApi scannerService;
    private boolean serviceBound = false;

    /**
     * Optional camera scanner
     */
    protected CameraBarcodeScanViewScanner cameraScanner;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Activity lifecycle callbacks
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Scanner activity is created " + this.hashCode());
        //Common.askForPermission(this); // NO: this actually pauses then resumes the activity.

        // Set content immediately - that way our callbacks can draw on the layout.
        setViewContent();

        // Ascending compatibility
        if (zbarViewId != null) {
            cameraViewId = zbarViewId;
        }

        // init fields
        serviceBound = false;
        scannerService = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "Scanner activity is starting " + this.hashCode());

        if (!enableScan) {
            return;
        }

        if (useBluetooth && hasPermissionSet(this, PERMISSIONS_BT)) {
            bindAndStartService();
        } else {
            requestPermissionSet(this, PERMISSIONS_BT, PERMISSION_REQUEST_ID_BT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "Scanner activity is resuming " + this.hashCode());

        if (!enableScan) {
            Log.i(LOG_TAG, "Resuming scanner activity with all scanning modes disabled");
            return;
        }

        if (goToCamera) {
            Log.i(LOG_TAG, "Resuming scanner activity in camera mode");
            initCamera();
            return;
        }
        Log.i(LOG_TAG, "Resuming scanner activity - scanners will be (re)connected");

        // Reset data fields
        if (findViewById(R.id.scanner_text_last_scan) != null) {
            ((TextView) findViewById(R.id.scanner_text_last_scan)).setText(null);
        }
        if (findViewById(R.id.scanner_text_scanner_status) != null) {
            ((TextView) findViewById(R.id.scanner_text_scanner_status)).setText(null);
        }

        // Immediately set some buttons (which do no need to wait for scanners).
        resetCameraButton();
        displayManualInputButton();

        // Register this activity on the scanner service (hooks onData) and ask it to hook possible scanners needing foreground control onto this activity.
        // If no scanners are available at all, this will still call onForegroundScannerInitEnded with 0 scanners, and the activity will launch the camera.
        if (serviceBound) {
            scannerService.resume(); // does nothing if not init.
            scannerService.registerClient(this);
        } else {
            bindAndStartService();
        }
    }

    @Override
    protected void onPause() {
        Log.i(LOG_TAG, "Scanner activity is being paused " + this.hashCode());
        if (serviceBound) {
            scannerService.pause();
        }
        if (cameraScanner != null) {
            cameraScanner.disconnect();
            cameraScanner = null;
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "Scanner activity is being stopped " + this.hashCode());
    }

    @Override
    protected void onDestroy() {
        Log.i(LOG_TAG, "Scanner activity is being destroyed " + this.hashCode());
        super.onDestroy();
        if (serviceBound) {
            scannerService.unregisterClient(this);
            unbindService(connection);
        }
        serviceBound = false;
    }

    private void setViewContent() {
        if (enableScan && goToCamera && hasPermissionSet(this, PERMISSIONS_CAMERA)) {
            // Can only add/open a camera view if camera is allowed.
            setContentView(layoutIdCamera);
        } else {
            setContentView(layoutIdLaser);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Scanner service init
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void bindAndStartService() {
        if (serviceBound) {
            return;
        }

        // Bind to ScannerService service
        Intent intent = new Intent(this, ScannerService.class);
        if (getServiceInitExtras() != null) {
            intent.putExtras(getServiceInitExtras());
        }
        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    protected Bundle getServiceInitExtras() {
        return ScannerServiceBinderHelper.defaultServiceConfiguration();
    }

    /**
     * Defines callbacks for service binding
     */
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to ScannerService, cast the IBinder and get the ScannerServiceApi instance
            Log.d(LOG_TAG, "Service is connected to activity");
            ScannerService.LocalBinder binder = (ScannerService.LocalBinder) service;
            scannerService = binder.getService();
            serviceBound = true;
            scannerService.registerClient(ScannerCompatActivity.this);
            scannerService.resume(); // may have been paused before bind by another activity.
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(LOG_TAG, "Service is disconnected from activity");
            serviceBound = false;
            scannerService = null;
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Configuration hooks
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unused")
    public void setAutocompletion(List<String> autocompletion, int threshold) {
        for (String item : autocompletion) {
            this.autocompletionItems.add(new ManualInputItem(item, false));
        }
        this.threshold = threshold;
    }

    @SuppressWarnings("unused")
    public void setAutocompletionItems(List<ManualInputItem> items, int threshold) {
        this.autocompletionItems = items;
        this.threshold = threshold;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Camera
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void initCamera() {
        Log.i(LOG_TAG, "Giving up on laser, going to camera");
        if (!Common.hasCamera(this)) {
            Log.i(LOG_TAG, "No camera available on device");
            Toast.makeText(this, R.string.scanner_status_no_camera, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean activityStartedInCameraMode = goToCamera;
        goToCamera = true;

        if (hasPermissionSet(this, PERMISSIONS_CAMERA)) {
            if (!activityStartedInCameraMode) {
                // The view needs permissions BEFORE initializing. And it initializes as soon as the layout is set.
                setContentView(layoutIdCamera);
            }
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

        cameraScanner = new CameraBarcodeScanViewScanner(cameraView, new ScannerDataCallbackProxy((s, data) -> ScannerCompatActivity.this.onData(data)), new ScannerStatusCallbackProxy(this));

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
        switch (requestCode) {
            case PERMISSION_REQUEST_ID_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goToCamera = true; // in case the activity was paused by the permission request dialog
                    setViewContent();
                    initCameraScanner();
                } else {
                    Toast.makeText(this, R.string.scanner_status_no_camera, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case PERMISSION_REQUEST_ID_BT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (useBluetooth && hasPermissionSet(this, PERMISSIONS_BT)) {
                        bindAndStartService();
                    }
                } else {
                    Toast.makeText(this, R.string.scanner_status_DISABLED, Toast.LENGTH_SHORT).show();
                }
                break;
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
    public void onScannerInitEnded(int scannerCount) {
        Log.i(LOG_TAG, "Activity can now use all received scanners (" + scannerCount + ")");

        if (scannerCount == 0 && !laserModeOnly && enableScan) {
            // In that case try to connect to a camera.
            initCamera();
        }

        displayTorch();
        displayToggleLedButton();
        displayEnableScanButton();
        displayDisableScanButton();
        displayBellButton();
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
        if (manualInputFragment != null) {
            manualInputFragment = null;
            scannerService.resume();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Button and input initialization
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Display the torch button "on" or "off" is the device has capability.
     **/
    protected void displayTorch() {
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
        } else {
            flashlight.setOnClickListener(v -> {
                for (final Scanner s : scannerService.getConnectedScanners()) {
                    if (s.getIlluminationSupport() != null)
                        s.getIlluminationSupport().toggleIllumination();
                }
                toggleTorch();
            });
        }
    }

    private void toggleTorch() {
        final ImageButton flashlight = findViewById(flashlightViewId);
        if (findViewById(flashlightViewId) == null) {
            return;
        }

        if (!anyScannerSupportsIllumination() && cameraScanner == null) {
            flashlight.setVisibility(View.GONE);
        } else {
            flashlight.setVisibility(View.VISIBLE);
        }

        boolean isOn = anyScannerHasIlluminationOn() || (cameraScanner != null && cameraScanner.isIlluminationOn());
        int iconId = isOn ? R.drawable.icn_flash_off_on : R.drawable.icn_flash_off;

        final int newColor = getResources().getColor(R.color.flashButtonColor);
        flashlight.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);
        flashlight.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), iconId));
    }

    protected boolean anyScannerSupportsIllumination() {
        if (scannerService == null) {
            return true;
        }
        for (final Scanner s : scannerService.getConnectedScanners()) {
            if (s.getIlluminationSupport() != null) {
                return true;
            }
        }
        return false;
    }

    protected boolean anyScannerHasIlluminationOn() {
        if (scannerService == null) {
            return false;
        }
        for (final Scanner s : scannerService.getConnectedScanners()) {
            if (s.getIlluminationSupport() != null && s.getIlluminationSupport().isIlluminationOn()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Display a manual input (keyboard) button for manual input.
     */
    protected void displayManualInputButton() {
        final View bt = findViewById(keyboardOpenViewId);
        if (bt == null) {
            return;
        }

        bt.setOnClickListener(view -> {
            // Pause camera or laser scanner during manual input.
            scannerService.pause();

            manualInputFragment = ManualInputFragment.newInstance();
            manualInputFragment.setAutocompletionItems(autocompletionItems, threshold);
            manualInputFragment.setDialogInterface(new DialogInterface() {
                @Override
                public void cancel() {
                    if (serviceBound) {
                        scannerService.resume();
                    }
                }

                @Override
                public void dismiss() {
                    if (serviceBound) {
                        scannerService.resume();
                    }
                }
            });
            manualInputFragment.show(getSupportFragmentManager(), "manual");
        });
    }

    /**
     * Display a "use camera" button to allow using camera input even when a laser is available.
     */
    protected void resetCameraButton() {
        if (findViewById(R.id.scanner_bt_camera) != null) {
            findViewById(R.id.scanner_bt_camera).setOnClickListener(view -> initCamera());
        }
    }

    protected void displayCameraReaderToggle() {
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

    protected void displayCameraPauseToggle() {
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

    private boolean ledToggle = false;

    private void displayToggleLedButton() {
        // Button present in layout?
        if (findViewById(R.id.scanner_red_led) == null) {
            return;
        }
        View v = findViewById(R.id.scanner_red_led);

        // Check if we should display the button
        boolean anySupport = false;
        for (final Scanner s : ScannerCompatActivity.this.scannerService.getConnectedScanners()) {
            if (s.getLedSupport() != null) {
                anySupport = true;
                break;
            }
        }
        if (!anySupport) {
            v.setVisibility(View.GONE);
            return;
        }

        // Set the event handler
        v.setOnClickListener(view -> {
            if (!ledToggle) {
                for (final Scanner s : ScannerCompatActivity.this.scannerService.getConnectedScanners()) {
                    if (s.getLedSupport() != null)
                        s.getLedSupport().ledColorOn(ScannerLedColor.RED);
                }
            } else {
                for (final Scanner s : ScannerCompatActivity.this.scannerService.getConnectedScanners()) {
                    if (s.getLedSupport() != null)
                        s.getLedSupport().ledColorOff(ScannerLedColor.RED);
                }
            }
            ledToggle = !ledToggle;
        });

    }

    private void displayDisableScanButton() {
        if (findViewById(R.id.scanner_trigger_off) != null) {
            findViewById(R.id.scanner_trigger_off).setOnClickListener(view -> {
                for (final Scanner s : scannerService.getConnectedScanners()) {
                    if (s.getTriggerSupport() != null)
                        s.getTriggerSupport().releaseScanTrigger();
                }
            });
        }
    }

    private void displayEnableScanButton() {
        if (findViewById(R.id.scanner_trigger_on) != null) {
            findViewById(R.id.scanner_trigger_on).setOnClickListener(view -> {
                for (final Scanner s : scannerService.getConnectedScanners()) {
                    if (s.getTriggerSupport() != null)
                        s.getTriggerSupport().pressScanTrigger();
                }
            });
        }
    }

    private void displayBellButton() {
        if (findViewById(R.id.scanner_bell) != null) {
            findViewById(R.id.scanner_bell).setOnClickListener(view -> {
                for (final Scanner s : scannerService.getConnectedScanners()) {
                    if (s.getBeepSupport() != null)
                        s.getBeepSupport().beepScanSuccessful();
                }
            });
        }
    }
}
