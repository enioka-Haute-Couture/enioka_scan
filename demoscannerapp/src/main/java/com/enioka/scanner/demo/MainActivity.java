package com.enioka.scanner.demo;

import static com.enioka.scanner.helpers.Permissions.PERMISSIONS_CAMERA;
import static com.enioka.scanner.helpers.Permissions.hasPermissionSet;

import android.content.res.Configuration;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.enioka.scanner.activities.ScannerCompatActivity;
import com.enioka.scanner.activities.ViewSwitcher;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.sdk.camera.CameraScannerProvider;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class MainActivity extends ScannerCompatActivity {
    /**
     * Delay in milliseconds before resetting the scanner status card style.
     */
    private static final long STATUS_CARD_RESET_DELAY = 170;

    /**
     * Reset stroke color and width of the scanner status card after a delay.
     */
    private void resetStatusCardStyle() {
        MaterialCardView scannerStatusCard = findViewById(scannerStatusCardViewId);
        new Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    scannerStatusCard.setStrokeColor(ContextCompat.getColor(MainActivity.this, com.enioka.scanner.R.color.cardBackgroundColor));
                    scannerStatusCard.setStrokeWidth(1);
                });
            }
        }, TimeUnit.MILLISECONDS.toMillis(STATUS_CARD_RESET_DELAY));
    }

    @Override
    public void initCamera() {
        super.initCamera();

        if (goToCamera && cameraScannerProvider != null) {
            int modeId = getIntent().getIntExtra("enableKeepAspectRatio", -1);

            if (modeId == -1) {
                Log.i(LOG_TAG, "No aspect ratio mode extra found.");
                return;
            }

            try {
                CameraScannerProvider.AspectRatioMode mode = CameraScannerProvider.AspectRatioMode.fromValue(modeId);

                Integer cameraViewId = cameraResources.get("camera_view_id");
                View cameraView = cameraViewId != null ? findViewById(cameraViewId) : null;
                cameraScannerProvider.setPreviewRatioMode(cameraView, mode);
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "Invalid value for AspectRatioMode: " + modeId);
            }
        }
    }

    @Override
    public void onData(List<Barcode> data) {

        StringBuilder res = new StringBuilder();
        for (Barcode b : data) {
            if (!enabledSymbologies.contains(b.getBarcodeType())) {
                Log.d(LOG_TAG, "Barcode type not enabled: " + b.getBarcodeType().code + " skipping.");
                continue;
            }

            Log.d(LOG_TAG, "Received barcode from scanner: " + b.getBarcode() + " - " + b.getBarcodeType().code);
            res.append(buildBarcodeText(b.getBarcodeType().code, b.getBarcode()));

            writeResultToLog(b);

            try {
                // Check if content is an URL
                String url = new URL(b.getBarcode()).toURI().toString();
                displayOpenLinkButton(url);
            } catch (Exception ignored) {
                // Not a URL
                findViewById(openLinkId).setVisibility(View.GONE);
            }
        }

        // Nothing to show
        if (res.toString().isEmpty()) {
            return;
        }

        MaterialCardView scannerStatusCard = findViewById(scannerStatusCardViewId);
        if (scannerStatusCard != null) {
            scannerStatusCard.setClickable(true);
            scannerStatusCard.setStrokeColor(ContextCompat.getColor(MainActivity.this, com.enioka.scanner.R.color.doneItemColor));
            scannerStatusCard.setStrokeWidth(4);

            resetStatusCardStyle();
        }

        TextView textLastScan = findViewById(com.enioka.scanner.R.id.scannerTextLastScan);
        if (textLastScan != null) {
            textLastScan.setText(Html.fromHtml(res.toString()));
            resizeScannerLastText(textLastScan);
        }

        // Disable the scannerSwitch when a barcode is found
        MaterialSwitch scannerSwitch = (MaterialSwitch) findViewById(com.enioka.scanner.R.id.scannerTriggerOn);
        if (scannerSwitch != null) {
            scannerSwitch.setChecked(false);
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
            float minTextSizeInSp = getResources().getDimension(com.enioka.scanner.R.dimen.min_text_size_last_scan) / getResources().getDisplayMetrics().scaledDensity;

            // Activate marquee effect if the text size is the minimum size
            if (textSizeInSp == minTextSizeInSp) {
                text.setSingleLine(true);
                text.setSelected(true);
            }
        });
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

        resizeScannerLastText((TextView) findViewById(com.enioka.scanner.R.id.scannerTextLastScan));
    }
}
