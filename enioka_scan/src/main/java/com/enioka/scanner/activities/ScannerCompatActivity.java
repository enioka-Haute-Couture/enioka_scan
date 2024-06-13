package com.enioka.scanner.activities;

import static com.enioka.scanner.helpers.Permissions.PERMISSIONS_BT;
import static com.enioka.scanner.helpers.Permissions.PERMISSIONS_CAMERA;
import static com.enioka.scanner.helpers.Permissions.PERMISSIONS_INTERNET;
import static com.enioka.scanner.helpers.Permissions.hasPermissionSet;
import static com.enioka.scanner.helpers.Permissions.requestPermissionSet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.enioka.scanner.R;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerLedColor;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.bt.api.BluetoothScanner;
import com.enioka.scanner.bt.api.BtSppScannerProvider;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
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
    protected final static int PERMISSION_REQUEST_ID_INTERNET = 125;

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
     * - scanner_bt_provider_logs: The ID of the optional ImageButton on which to press to manually access available providers logs
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

    protected int providerLogOpenViewId = R.id.scanner_bt_provider_logs;

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
     * Camera preview ratio mode.
     */
    protected int previewRatioMode = -1;

    /**
     * Define if the activity should go back to the scanner view or main view.
     */
    private boolean backScannerView = true;

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

    /**
     * Logs and status of all detected providers
     */
    private String providerLogs = "";

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
        linkBackCallback();

        // Ascending compatibility
        if (zbarViewId != null) {
            cameraResources.put("camera_view_id", zbarViewId);
        }

        // Get the intent extras
        loggingEnabled = getIntent().getBooleanExtra("enableLogging", false);
        allowCameraFallback = getIntent().getBooleanExtra("allowCameraFallback", false);
        previewRatioMode = getIntent().getIntExtra("enableKeepAspectRatio", -1);

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

        if (findViewById(R.id.scanner_provider_status_card) != null) {
            findViewById(R.id.scanner_provider_status_card).setVisibility(View.GONE);
        }

        if (findViewById(R.id.scanner_provider_text) != null) {
            ((TextView) findViewById(R.id.scanner_provider_text)).setText("");
        }

        if (findViewById(R.id.scanner_provider_status_text) != null) {
            ((TextView) findViewById(R.id.scanner_provider_status_text)).setText("");
        }

        // Hide the open link button
        findViewById(openLinkId).setVisibility(View.GONE);

        // Set scanner card last scan to not clickable
        scannerStatusCard.setClickable(false);

        // Immediately set some buttons (which do no need to wait for scanners).
        displayCameraButton();
        displayManualProviderLogButton();

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

            View view = findViewById(flashlightViewId).getRootView();
            ViewSwitcher.switchCameraOrientation(this, view, cameraResources, orientation == Configuration.ORIENTATION_PORTRAIT);
        } else {
            setContentView(layoutIdLaser);

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                View view = findViewById(flashlightViewId).getRootView();
                ViewSwitcher.switchLaserOrientation(this, view, flashlightViewId, false);
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
                setViewContent();
            }
            initCameraScanner();

            // Reinit text
            if (findViewById(R.id.scanner_provider_text) != null) {
                TextView tv = findViewById(R.id.scanner_provider_text);
                tv.setText("");
            }
            if (findViewById(R.id.scanner_provider_status_text) != null) {
                TextView tv = findViewById(R.id.scanner_provider_status_text);
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

        // Set the content view to the camera layout
        Integer cardLastScanId = cameraResources.get("card_last_scan_id");
        scannerStatusCard = cardLastScanId != null ? findViewById(cardLastScanId) : null;

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
        // Set the preview ratio mode
        cameraScannerProvider.setPreviewRatioMode(cameraView, previewRatioMode);

        InitCopyClipBoard();

        if (findViewById(R.id.scanner_text_last_scan) != null) {
            ((TextView) findViewById(R.id.scanner_text_last_scan)).setText(null);
        }

        if (scannerStatusCard != null) {
            scannerStatusCard.setClickable(false);
        }

        displayTorch();
        displayManualProviderLogButton();
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
            case PERMISSION_REQUEST_ID_INTERNET: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    displayManualPairingBarcodeButton();
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
        if (scanner != null && newStatus != null) {
            providerLogs += scanner.getProviderKey() + " " + newStatus + "\n";
        }

        TextView providerText = findViewById(R.id.scanner_provider_text);
        TextView providerStatusText = findViewById(R.id.scanner_provider_status_text);

        if (scanner != null && providerText != null && providerStatusText != null && (newStatus == Status.CONNECTED || newStatus == Status.READY)) {
            if (goToCamera && newStatus == Status.READY) {
                return;
            }

            String connectedProvider = providerText.getText().toString();

            // Update text list of connect providers
            if (!connectedProvider.contains(scanner.getProviderKey())) {
                providerText.setText(connectedProvider + (connectedProvider.isEmpty() ? "" : "\n") + scanner.getProviderKey());
            }

            // Set status
            providerStatusText.setText(newStatus.toString());

            // Update visibility of the scanner status card
            findViewById(R.id.scanner_provider_status_card).setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onScannerInitEnded(int scannerCount) {
        Log.i(LOG_TAG, "Activity can now use all received scanners (" + scannerCount + ")");

        if (scannerCount == 0 && !laserModeOnly && enableScan && hasCameraScannerSdk && allowCameraFallback) {
            // In that case try to connect to a camera.
            initCamera();
            backScannerView = false;
        }

        if (scannerCount > 0) {
            displayTorch();
            displayToggleLedButton();
            displaySwitchScanButton();
            displayBellButton();
            displayManualPairingBarcodeButton();
        } else {
            displayManualPairingBarcodeButton();
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
            scannerStatusCard.setClickable(true);
            scannerStatusCard.setStrokeColor(ContextCompat.getColor(ScannerCompatActivity.this, R.color.doneItemColor));
            scannerStatusCard.setStrokeWidth(4);

            resetStatusCardStyle();
        }

        StringBuilder res = new StringBuilder();
        for (Barcode b : data) {

            Log.d(LOG_TAG, "Received barcode from scanner: " + b.getBarcode() + " - " + b.getBarcodeType().code);
            res.append(buildBarcodeText(b.getBarcodeType().code, b.getBarcode()));

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
                    findViewById(openLinkId).setVisibility(View.GONE);
                });
            } catch (Exception ignored) {
                // Not a URL
                findViewById(openLinkId).setVisibility(View.GONE);
            }
        }

        TextView textLastScan = findViewById(R.id.scanner_text_last_scan);
        if (textLastScan != null) {
            textLastScan.setText(Html.fromHtml(res.toString()));
            resizeScannerLastText(textLastScan);
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
     * Dynamically resize the last scan text view to fit the content.
     * Called when a new data is received, or on orientation change.
     */
    private void resizeScannerLastText(TextView text) {
        if (text == null) {
            return;
        }
        // Set the last scan text
        text.setSingleLine(false);

        text.post(() -> {
            float textSizeInSp = text.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
            float minTextSizeInSp = getResources().getDimension(R.dimen.min_text_size_last_scan) / getResources().getDisplayMetrics().scaledDensity;

            // Activate marquee effect if the text size is the minimum size
            if (textSizeInSp == minTextSizeInSp) {
                text.setSingleLine(true);
                text.setSelected(true);
            }
        });
    }

    /**
     *  Build the text to display in the scanner status card.
     */
    private String buildBarcodeText(String barcodeType, String barcodeData) {
        if (barcodeType.isEmpty() || barcodeData.isEmpty()) {
            return "";
        }

        return "TYPE: <b><font color='grey'>" + barcodeType + "</font></b> <b>" + barcodeData + "</b>";
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
     *
     */
    private void linkBackCallback() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Get name of current view

                if (backScannerView && goToCamera) {
                    goToCamera = false;

                    Log.i(LOG_TAG, "Scanner activity is being reset, going back to scanner view " + this.hashCode());
                    if (serviceBound) {
                        scannerService.pause();
                    }

                    if (cameraScannerProvider != null && cameraScannerProvider.isCameraScannerInitialized()) {
                        cameraScannerProvider.disconnect();
                        cameraScannerProvider.reset();
                    }

                    setViewContent();
                    onResume();
                } else {
                    finish();
                }
            }
        };

        // Add the callback to the activity's lifecycle
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    /**
     * Copy last scan text to clipboard when clicking on the scanner status card.
     */
    private void InitCopyClipBoard() {
        // Setup clipboard copy button
        scannerStatusCard.setOnClickListener(v -> {
            TextView lastScan = findViewById(R.id.scanner_text_last_scan);

            if (lastScan.getText().length() != 0) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String[] lastScanText = lastScan.getText().toString().split(" ");
                ClipData clip = ClipData.newPlainText("barcode", lastScanText[lastScanText.length - 1]);

                clipboard.setPrimaryClip(clip);

                Snackbar snackbar = Snackbar.make(v, R.string.last_scan_clipboard, Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        });
        scannerStatusCard.setClickable(false);
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

    protected boolean anyScannerHasBarcodeParing() {
        if (scannerService == null) {
            return true;
        }
        for (final Scanner s : scannerService.getConnectedScanners()) {
            if (s.getBarcodePairingSupport() != null) {
                return true;
            }
        }
        return false;
    }

    private void displayManualPairingBarcodeButton() {
        final ImageButton pairingBarcodeButton = findViewById(R.id.pairing_barcode);

        if (pairingBarcodeButton == null) {
            return;
        }

        if (!anyScannerHasBarcodeParing()) {
            pairingBarcodeButton.setVisibility(View.VISIBLE);
        } else {
            pairingBarcodeButton.setVisibility(View.VISIBLE);
        }

        if (!hasPermissionSet(this, PERMISSIONS_INTERNET)) {
            requestPermissionSet(this, PERMISSIONS_INTERNET, PERMISSION_REQUEST_ID_INTERNET);
        }

        try {
            // find if class exists
            Class<?> zebraDw = Class.forName("com.enioka.scanner.sdk.zebraoss.ZebraOssSppScannerProvider");
            Constructor<?> constructor = zebraDw.getDeclaredConstructor();
            Object test = constructor.newInstance();

            pairingBarcodeButton.setOnClickListener(v -> {

                Bitmap result = Objects.requireNonNull(((BtSppScannerProvider) test).getBarcodePairingSupport()).getPairingBarcode();
                ImageView imageView = new ImageView(this);
                imageView.setImageBitmap(result);
                ManualPairingBarcodeDialog.launchDialog(this, imageView);
                // Open dialog to display the barcode
            });

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
                 NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        for (final Scanner s : scannerService.getConnectedScanners()) {
//            if (s.getBarcodePairingSupport() != null) {
//                pairingBarcodeButton.setOnClickListener(v -> {
//
//                    Bitmap result = s.getBarcodePairingSupport().getPairingBarcode();
//                    ImageView imageView = new ImageView(this);
//                    imageView.setImageBitmap(result);
//                    ManualPairingBarcodeDialog.launchDialog(this, imageView);
//                    // Open dialog to display the barcode
//                });
//            }
//              if (s.getBarcodeNfcSupport() != null) {
//                  pairingBarcodeButton.setOnClickListener(v -> {
//                      if (!s.getBarcodeNfcSupport().isNfcEnabled(this)) {
//                          s.getBarcodeNfcSupport().askNfcActivation(this);
//                      }
//                  });
//              }
        }
    }

    /**
     * Display a manual input button to launch an alert dialog that contains provider logs.
     */
    private void displayManualProviderLogButton() {
        final View bt = findViewById(providerLogOpenViewId);
        if (bt == null) {
            return;
        }

        bt.setVisibility(View.VISIBLE);

        String providerLogTitle = getResources().getString(R.string.provider_log_dialog_title);
        String textButton = getResources().getString(R.string.provider_log_dialog_close);
        bt.setOnClickListener(view -> ManualLogDialog.launchDialog(this, providerLogTitle, providerLogs, textButton));
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

            // Show snackbar message informing the user of the change
            Snackbar.make(buttonView, isChecked ? R.string.snack_message_zxing : R.string.snack_message_zbar, Snackbar.LENGTH_SHORT).show();
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

        String fileName = "scans_scanner_test_" + (new Date()).getTime() + ".csv";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    private synchronized void writeResultToLog(Barcode data) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dataLine = formatter.format(new Date()) + "," + data.getBarcode() + "," + data.getBarcodeType().code;

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
        View view = findViewById(flashlightViewId).getRootView();

        if (switchCamera) {
            Integer cameraViewId = cameraResources.get("camera_view_id");

            if (cameraViewId != null && cameraScannerProvider != null) {
                cameraScannerProvider.orientationChanged(findViewById(cameraViewId));
            }

            ViewSwitcher.switchCameraOrientation(this, view, cameraResources, orientation == Configuration.ORIENTATION_PORTRAIT);
        } else {
            ViewSwitcher.switchLaserOrientation(this, view, flashlightViewId, orientation == Configuration.ORIENTATION_PORTRAIT);
        }

        resizeScannerLastText((TextView) findViewById(R.id.scanner_text_last_scan));
    }
}