package com.enioka.scanner.activities;

import static com.enioka.scanner.helpers.Permissions.PERMISSIONS_BT;
import static com.enioka.scanner.helpers.Permissions.PERMISSIONS_CAMERA;
import static com.enioka.scanner.helpers.Permissions.hasPermissionSet;
import static com.enioka.scanner.helpers.Permissions.requestPermissionSet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enioka.scanner.R;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerLedColor;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.helpers.Common;
import com.enioka.scanner.sdk.camera.CameraScannerProvider;
import com.enioka.scanner.service.ScannerClient;
import com.enioka.scanner.service.ScannerService;
import com.enioka.scanner.service.ScannerServiceApi;
import com.enioka.scanner.service.ScannerServiceBinderHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * A helper activity which implements all scan functions: laser, camera, HID.<br><br>Basic usage is trivial : just inherit this class, and that's all.<br>
 * You may want to override {@link #onData(List)} to get barcode data, and {@link #onStatusChanged(Scanner, ScannerStatusCallback.Status)} to display status messages from the scanners.<br>
 * It is also useful to change  inside onCreate {@link #layoutIdLaser} and layout_id_camera inside {@link #cameraResources} to a layout ID (from R.id...) corresponding to your application.
 * By default, a basic test layout is provided.<br>
 * Also, camera_view_id {@link #cameraResources} points to the camera view inside your camera layout.
 */
public class ScannerCompatActivity extends AppCompatActivity implements ScannerClient {
    protected final static String LOG_TAG = "ScannerActivity";
    protected final static String CAMERA_SDK_PACKAGE = "com.enioka.scanner.sdk.camera";
    protected final static int PERMISSION_REQUEST_ID_CAMERA = 1790;
    protected final static int PERMISSION_REQUEST_ID_BT = 1792;
    protected final static int PERMISSION_REQUEST_ID_WRITE = 124;
    protected final static int WRITE_REQUEST_CODE = 123;

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
     * Check the app's bluetooth permissions. This variable does not affect scanner search options.
     */
    protected boolean useBluetooth = true;

    /**
     * The layout to use when using a laser or external keyboard.
     */
    protected int layoutIdLaser = R.layout.activity_main;
    /**
     * Hashmap containing the ID resources from the camera scanner provider.
     * Contains the following keys:
     * - layout_id_camera: The ID of the layout containing the camera view.
     * - camera_view_id: The ID of the camera view in the layout.
     * - scanner_toggle_view_id: The ID of the view that toggles the scanner library reader.
     * - scanner_toggle_pause_id: The ID of the view that toggles the pause of the scanner.
     * - card_last_scan_id: ID of the card view that displays the last scan.
     * - constraint_layout_id: The ID of the constraint layout inside the camera layout.
     * - scanner_flashlight_id: The ID of the optional ImageButton on which to press to toggle the flashlight/illumination.
     * - scanner_bt_keyboard_id: The ID of the optional ImageButton on which to press to manually switch to keyboard mode.
     */
    protected HashMap<String, Integer> cameraResources = null;
    /**
     * Use camera_view_id inside the {@link #cameraResources} instead.
     */
    @Deprecated
    protected Integer zbarViewId = null;
    /**
     * The ID of the ImageButton on which to press to manually switch to camera mode.
     */
    protected int cameraToggleId = R.id.scanner_bt_camera;

    /**
     * The ID of the optional ImageButton on which to press to toggle the flashlight/illumination.
     */
    protected int flashlightViewId = R.id.scanner_flashlight;

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
     * Whether the camera scanner SDK is available.
     */
    private boolean hasCameraScannerSdk = false;

    /**
     * Optional camera scanner provider (if the camera scanner SDK is available).
     */
    protected CameraScannerProvider cameraScannerProvider = null;

    /**
     * Material card view for the scanner status.
     */
    private MaterialCardView scannerStatusCard;

    /**
     * Delay in milliseconds before resetting the scanner status card style.
     */
    private static final long STATUS_CARD_RESET_DELAY = 170;

    /**
     * Define if the log is enabled or not.
     */
    protected boolean loggingEnabled = false;

    /**
     * Define if the fallback to camera is allowed.
     */
    protected boolean allowCameraFallback = false;

    /**
     * The URI of the log file to write to.
     */
    private Uri logFileUri = null;

    /**
     * The ID of the open link button.
     */
    protected int openLinkId = R.id.open_link;

    /**
     * The String URL of the open link button.
     */
    private String openLinkUrl = null;

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
            cameraResources.put("camera_view_id", zbarViewId);
        }

        // Get the intent extras
        loggingEnabled = getIntent().getBooleanExtra("enableLogging", false);
        allowCameraFallback = getIntent().getBooleanExtra("allowCameraFallback", false);

        // Init logging if enabled
        if (loggingEnabled) {
            if (hasPermissionSet(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                createLog();
            } else {
                requestPermissionSet(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_ID_WRITE);
            }
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

        if (!useBluetooth || hasPermissionSet(this, PERMISSIONS_BT)) {
            bindAndStartService();
        } else if (useBluetooth && !hasPermissionSet(this, PERMISSIONS_BT)) {
            requestPermissionSet(this, PERMISSIONS_BT, PERMISSION_REQUEST_ID_BT);
        }

        // Check if the camera scanner SDK is available.
        try {
            cameraScannerProvider = (CameraScannerProvider) Class.forName(CAMERA_SDK_PACKAGE + ".CameraProvider").newInstance();
            hasCameraScannerSdk = true;
            setCameraViewId();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            Log.i(LOG_TAG, "Could not instantiate camera provider", e);
            hasCameraScannerSdk = false;
            // Disable switch to camera button
            findViewById(cameraToggleId).setVisibility(View.GONE);
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

        if (goToCamera && hasCameraScannerSdk) {
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

        // Hide the open link button
        findViewById(openLinkId).setVisibility(View.GONE);

        // Immediately set some buttons (which do no need to wait for scanners).
        displayCameraButton();
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
        if (cameraScannerProvider != null && cameraScannerProvider.isCameraScannerInitialized()) {
            cameraScannerProvider.disconnect();
            cameraScannerProvider.reset();
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
        int orientation = getResources().getConfiguration().orientation;

        if (cameraResources != null && enableScan && goToCamera && hasPermissionSet(this, PERMISSIONS_CAMERA) && hasCameraScannerSdk) {
            // Can only add/open a camera view if camera is allowed.
            Integer cameraLayoutId = cameraResources.get("layout_id_camera");

            if (cameraLayoutId == null) {
                throw new IllegalStateException("Camera layout not set");
            }

            setContentView(cameraLayoutId);
            switchCameraOrientation(orientation == Configuration.ORIENTATION_PORTRAIT);
        } else {
            setContentView(layoutIdLaser);

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                switchLaserOrientation(false);
            }
            scannerStatusCard = findViewById(R.id.scanner_card_last_scan);
        }
        // Init last scan card button
        InitCopyClipBoard();
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
                Integer cameraLayoutId = cameraResources.get("layout_id_camera");
                if (cameraLayoutId == null) {
                    throw new IllegalStateException("Camera layout not set");
                }
                setContentView(cameraLayoutId);
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

    private void setCameraViewId() {
        if (cameraResources == null) {
            cameraResources = cameraScannerProvider.getIdResources();
        }
    }

    private void initCameraScanner() {
        if (cameraScannerProvider != null && cameraScannerProvider.isCameraScannerInitialized()) {
            return;
        }
        // TODO: should be in camera constructor, not here...
        Integer cameraViewId = cameraResources.get("camera_view_id");
        View cameraView = cameraViewId != null ? findViewById(cameraViewId) : null;
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

        cameraScannerProvider.getCameraScanner(cameraView, new ScannerDataCallbackProxy((s, data) -> ScannerCompatActivity.this.onData(data)), new ScannerStatusCallbackProxy(this), symbologies);
        // Set the content view to the camera layout
        Integer cardLastScanId = cameraResources.get("card_last_scan_id");
        scannerStatusCard = cardLastScanId != null ? findViewById(cardLastScanId) : null;
        InitCopyClipBoard();

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
                    if (!useBluetooth || hasPermissionSet(this, PERMISSIONS_BT)) {
                        bindAndStartService();
                    }
                } else {
                    Toast.makeText(this, R.string.scanner_status_DISABLED, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case PERMISSION_REQUEST_ID_WRITE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createLog();
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
            tv.setText((scanner == null ? "" : (scanner.getProviderKey() + ": ")) + newStatus + (tv.getText().length() != 0 ? "\n" : "") + tv.getText());
        }
    }

    @Override
    public void onScannerInitEnded(int scannerCount) {
        Log.i(LOG_TAG, "Activity can now use all received scanners (" + scannerCount + ")");

        if (scannerCount == 0 && !laserModeOnly && enableScan && hasCameraScannerSdk && allowCameraFallback) {
            // In that case try to connect to a camera.
            initCamera();
        }

        if (scannerCount > 0) {
            displayTorch();
            displayToggleLedButton();
            displaySwitchScanButton();
            displayBellButton();
        } else {

        }
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
        if (scannerStatusCard != null) {
            scannerStatusCard.setStrokeColor(ContextCompat.getColor(ScannerCompatActivity.this, R.color.doneItemColor));
            scannerStatusCard.setStrokeWidth(4);

            resetStatusCardStyle();
        }

        StringBuilder res = new StringBuilder();
        for (Barcode b : data) {
            Log.d(LOG_TAG, "Received barcode from scanner: " + b.getBarcode() + " - " + b.getBarcodeType().code);
            res.append("TYPE: ").append(b.getBarcodeType().code).append(" ").append(b.getBarcode());

            if (logFileUri != null) {
                writeResultToLog(b);
            }

            try {
                // Check if content is an URL
                openLinkUrl = new URL(b.getBarcode()).toURI().toString();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(openLinkUrl));

                findViewById(openLinkId).setVisibility(View.VISIBLE);
                findViewById(openLinkId).setOnClickListener(v -> {
                    // Pause camera or laser scanner
                    startActivity(intent);
                });
            } catch (Exception ignored) {
                // Not a URL
                findViewById(openLinkId).setVisibility(View.GONE);
            }
        }
        if (findViewById(R.id.scanner_text_last_scan) != null) {
            ((TextView) findViewById(R.id.scanner_text_last_scan)).setText(res.toString());
        }

        // Disable the scannerSwitch when a barcode is found
        MaterialSwitch scannerSwitch = (MaterialSwitch) findViewById(R.id.scanner_trigger_on);
        if (scannerSwitch != null) {
            scannerSwitch.setChecked(false);
        }

        if (manualInputFragment != null) {
            manualInputFragment = null;
            scannerService.resume();
        }
    }

    /**
     * Reset stroke color and width of the scanner status card after a delay.
     */
    private void resetStatusCardStyle() {
        new Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    scannerStatusCard.setStrokeColor(ContextCompat.getColor(ScannerCompatActivity.this, R.color.cardBackgroundColor));
                    scannerStatusCard.setStrokeWidth(1);
                });
            }
        }, TimeUnit.MILLISECONDS.toMillis(STATUS_CARD_RESET_DELAY));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Button and input initialization
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Copy last scan text to clipboard when clicking on the scanner status card.
     */
    private void InitCopyClipBoard() {
        // Setup clipboard copy button
        scannerStatusCard.setOnClickListener(v -> {
            TextView lastScan = findViewById(R.id.scanner_text_last_scan);

            if (lastScan.getText().length() != 0) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("barcode", lastScan.getText());
                clipboard.setPrimaryClip(clip);

                Snackbar snackbar = Snackbar.make(v, R.string.last_scan_clipboard, Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        });
    }

    /**
     * Display the torch button "on" or "off" is the device has capability.
     **/
    private void displayTorch() {
        final ImageButton flashlight = findViewById(flashlightViewId);
        if (flashlight == null) {
            return;
        }

        flashlight.setVisibility(View.VISIBLE);
        toggleTorch();

        if (cameraScannerProvider != null && cameraScannerProvider.isCameraScannerInitialized()) {
            flashlight.setOnClickListener(v -> {
                cameraScannerProvider.toggleIllumination();
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

        if (!anyScannerSupportsIllumination() && cameraScannerProvider != null && !cameraScannerProvider.isCameraScannerInitialized()) {
            flashlight.setVisibility(View.GONE);
        } else {
            flashlight.setVisibility(View.VISIBLE);
        }

        boolean isOn = (!goToCamera && anyScannerHasIlluminationOn()) || (cameraScannerProvider != null && cameraScannerProvider.isIlluminationOn());

        int iconId;

        // If we are in camera mode, the icon should be the camera flash icon.
        // Otherwise, the icon should be the flashlight icon.
        if (goToCamera) {
            iconId = isOn ? R.drawable.flash : R.drawable.flash_off;
        } else {
            iconId = isOn ? R.drawable.flashlight : R.drawable.flashlight_off;

        }

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
    private void displayManualInputButton() {
        final View bt = findViewById(keyboardOpenViewId);
        if (bt == null) {
            return;
        }

        bt.setVisibility(View.VISIBLE);

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
    private void displayCameraButton() {
        View cameraButtonView = findViewById(cameraToggleId);
        if (cameraButtonView != null) {
            // Display the camera button if the camera scanner SDK is available.
            if (hasCameraScannerSdk) {
                cameraButtonView.setVisibility(View.VISIBLE);
            } else {
                cameraButtonView.setVisibility(View.GONE);
                return;
            }
            cameraButtonView.setOnClickListener(view -> initCamera());
        }
    }

    private void displayCameraReaderToggle() {
        Integer scannerToggleViewId = cameraResources.get("scanner_toggle_view_id");
        final SwitchCompat toggle = scannerToggleViewId != null ? findViewById(scannerToggleViewId) : null;
        if (toggle == null) {
            return;
        }

        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.i(LOG_TAG, "Changing reader mode");
            Integer cameraViewId = cameraResources.get("camera_view_id");
            View cameraView = cameraViewId != null ? findViewById(cameraViewId) : null;
            cameraScannerProvider.setReaderMode(cameraView, isChecked);
        });
    }

    private void displayCameraPauseToggle() {
        Integer scannerToggleViewId = cameraResources.get("scanner_toggle_pause_id");
        final SwitchCompat toggle = scannerToggleViewId != null ? findViewById(scannerToggleViewId) : null;
        if (toggle == null) {
            return;
        }

        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.i(LOG_TAG, "Toggling camera pause");
            Integer cameraViewId = cameraResources.get("camera_view_id");
            View cameraView = cameraViewId != null ? findViewById(cameraViewId) : null;
            if (isChecked) {
                cameraScannerProvider.pauseCamera(cameraView);
            } else {
                cameraScannerProvider.resumeCamera(cameraView);
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

    private void displaySwitchScanButton() {
        View scannerTriggerOff = findViewById(R.id.scanner_trigger_on);

        if (scannerTriggerOff != null) {
            scannerTriggerOff.setVisibility(View.VISIBLE);

            MaterialSwitch switchCompat = (MaterialSwitch) scannerTriggerOff;
            switchCompat.setOnCheckedChangeListener((view, checked) -> {
                for (final Scanner s : scannerService.getConnectedScanners()) {
                    if (s.getTriggerSupport() != null) {
                        if (checked) {
                            s.getTriggerSupport().pressScanTrigger();
                        } else {
                            s.getTriggerSupport().releaseScanTrigger();
                        }
                    }
                }
            });
        }
    }


    private void displayBellButton() {
        View scannerBellView = findViewById(R.id.scanner_bell);

        if (scannerBellView != null) {
            scannerBellView.setVisibility(View.VISIBLE);
            scannerBellView.setOnClickListener(view -> {
                for (final Scanner s : scannerService.getConnectedScanners()) {
                    if (s.getBeepSupport() != null)
                        s.getBeepSupport().beepScanSuccessful();
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Logger
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            logFileUri = data.getData();
            if (logFileUri != null) {
                Log.i(LOG_TAG, "Log file will be written at: " + logFileUri.toString());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Screen orientation
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        boolean switchCamera = enableScan && goToCamera && hasPermissionSet(this, PERMISSIONS_CAMERA) && hasCameraScannerSdk;
        int orientation = getResources().getConfiguration().orientation;

        if (switchCamera) {
            switchCameraOrientation(orientation == Configuration.ORIENTATION_PORTRAIT);
        } else {
            switchLaserOrientation(orientation == Configuration.ORIENTATION_PORTRAIT);
        }
    }

    private void switchCameraOrientation(boolean portrait) {
        Integer constraintLayoutId = cameraResources.get("constraint_layout_id");
        Integer scannerFlashlightId = cameraResources.get("scanner_flashlight_id");
        Integer scannerBtKeyboardId = cameraResources.get("scanner_bt_keyboard_id");

        if (constraintLayoutId == null || scannerFlashlightId == null || scannerBtKeyboardId == null) {
            Log.w(LOG_TAG, "Cannot switch to landscape mode: missing resources");
            return;
        }

        ConstraintLayout constraintLayout = findViewById(constraintLayoutId);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        int margin = getResources().getDimensionPixelSize(R.dimen.layout_margin_border);

        // Set left constraints
        if (portrait) {
            constraintSet.connect(scannerFlashlightId, ConstraintSet.RIGHT, constraintLayout.getId(), ConstraintSet.RIGHT, margin);
            constraintSet.connect(scannerBtKeyboardId, ConstraintSet.RIGHT, constraintLayout.getId(), ConstraintSet.RIGHT, margin);
        } else {
            constraintSet.connect(scannerFlashlightId, ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT, margin);
            constraintSet.connect(scannerBtKeyboardId, ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT, margin);

        }

        if (portrait) {
            // Delete left constraints
            constraintSet.clear(scannerFlashlightId, ConstraintSet.LEFT);
            constraintSet.clear(scannerBtKeyboardId, ConstraintSet.LEFT);

        } else {
            // Delete right constraints
            constraintSet.clear(scannerFlashlightId, ConstraintSet.RIGHT);
            constraintSet.clear(scannerBtKeyboardId, ConstraintSet.RIGHT);
        }

        // Apply constraints
        constraintSet.applyTo(constraintLayout);
    }

    public void switchLaserOrientation(boolean portrait) {
        ConstraintLayout mainConstraintLayout = findViewById(R.id.constraint_layout);
        LinearLayout linearLayout = findViewById(R.id.bottom_layout);
        View scannerFlashlight = findViewById(flashlightViewId);
        View scannerBell = findViewById(R.id.scanner_bell);
        View scannerRedLed = findViewById(R.id.scanner_red_led);

        if (portrait) {
            // Move elements to main constraint layout
            linearLayout.removeView(scannerFlashlight);
            linearLayout.removeView(scannerBell);
            linearLayout.removeView(scannerRedLed);

            LinearLayout.LayoutParams oldParamsFlashLight = (LinearLayout.LayoutParams) scannerFlashlight.getLayoutParams();
            LinearLayout.LayoutParams oldParamsBell = (LinearLayout.LayoutParams) scannerBell.getLayoutParams();
            LinearLayout.LayoutParams oldParamsRedLed = (LinearLayout.LayoutParams) scannerRedLed.getLayoutParams();

            ConstraintLayout.LayoutParams newParamsFlashLight = new ConstraintLayout.LayoutParams(oldParamsFlashLight.width, oldParamsFlashLight.height);
            newParamsFlashLight.setMargins(oldParamsFlashLight.leftMargin, oldParamsFlashLight.topMargin, oldParamsFlashLight.rightMargin, oldParamsFlashLight.bottomMargin);

            ConstraintLayout.LayoutParams newParamsBell = new ConstraintLayout.LayoutParams(oldParamsBell.width, oldParamsBell.height);
            newParamsBell.setMargins(oldParamsBell.leftMargin, oldParamsBell.topMargin, oldParamsBell.rightMargin, oldParamsBell.bottomMargin);

            ConstraintLayout.LayoutParams newParamsRedLed = new ConstraintLayout.LayoutParams(oldParamsRedLed.width, oldParamsRedLed.height);
            newParamsRedLed.setMargins(oldParamsRedLed.leftMargin, oldParamsRedLed.topMargin, oldParamsRedLed.rightMargin, oldParamsRedLed.bottomMargin);

            mainConstraintLayout.addView(scannerFlashlight, newParamsFlashLight);
            mainConstraintLayout.addView(scannerBell, newParamsBell);
            mainConstraintLayout.addView(scannerRedLed, newParamsRedLed);

            // Apply constraints
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(mainConstraintLayout);

            int margin = getResources().getDimensionPixelSize(R.dimen.layout_margin_border);

            constraintSet.connect(scannerFlashlight.getId(), ConstraintSet.TOP, R.id.scanner_enable_text, ConstraintSet.BOTTOM, margin);
            constraintSet.connect(scannerFlashlight.getId(), ConstraintSet.END, mainConstraintLayout.getId(), ConstraintSet.END, margin);

            constraintSet.connect(scannerBell.getId(), ConstraintSet.TOP, scannerFlashlight.getId(), ConstraintSet.BOTTOM, margin);
            constraintSet.connect(scannerBell.getId(), ConstraintSet.END, mainConstraintLayout.getId(), ConstraintSet.END, margin);

            constraintSet.connect(scannerRedLed.getId(), ConstraintSet.TOP, scannerBell.getId(), ConstraintSet.BOTTOM, margin);
            constraintSet.connect(scannerRedLed.getId(), ConstraintSet.END, mainConstraintLayout.getId(), ConstraintSet.END, margin);

            constraintSet.applyTo(mainConstraintLayout);
        } else {
            // Move elements to linear layout
            mainConstraintLayout.removeView(scannerFlashlight);
            mainConstraintLayout.removeView(scannerBell);
            mainConstraintLayout.removeView(scannerRedLed);

            ConstraintLayout.LayoutParams oldParamsFlashLight = (ConstraintLayout.LayoutParams) scannerFlashlight.getLayoutParams();
            ConstraintLayout.LayoutParams oldParamsBell = (ConstraintLayout.LayoutParams) scannerBell.getLayoutParams();
            ConstraintLayout.LayoutParams oldParamsRedLed = (ConstraintLayout.LayoutParams) scannerRedLed.getLayoutParams();

            LinearLayout.LayoutParams newParamsFlashLight = new LinearLayout.LayoutParams(oldParamsFlashLight.width, oldParamsFlashLight.height);
            newParamsFlashLight.setMargins(0, oldParamsFlashLight.topMargin, oldParamsFlashLight.rightMargin, oldParamsFlashLight.bottomMargin);

            LinearLayout.LayoutParams newParamsBell = new LinearLayout.LayoutParams(oldParamsBell.width, oldParamsBell.height);
            newParamsBell.setMargins(0, oldParamsBell.topMargin, oldParamsBell.rightMargin, oldParamsBell.bottomMargin);

            LinearLayout.LayoutParams newParamsRedLed = new LinearLayout.LayoutParams(oldParamsRedLed.width, oldParamsRedLed.height);
            newParamsRedLed.setMargins(0, oldParamsRedLed.topMargin, oldParamsRedLed.rightMargin, oldParamsRedLed.bottomMargin);

            linearLayout.addView(scannerFlashlight, newParamsFlashLight);
            linearLayout.addView(scannerBell, newParamsBell);
            linearLayout.addView(scannerRedLed, newParamsRedLed);
        }
    }
}
