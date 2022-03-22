package com.enioka.scanner.sdk.mock;

import android.content.Context;
import android.util.Log;

import com.enioka.scanner.api.Color;
import com.enioka.scanner.api.ScannerBackground;
import com.enioka.scanner.api.ScannerStatusCallback;
import com.enioka.scanner.data.Barcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock scanner used to test callbacks and basic scanner interactions.
 * It does not bind to any bluetooth device or intent, "scanned" data has to be passed manually.
 */
public class MockScanner implements ScannerBackground {
    static final String LOG_TAG = "MockScanner";

    private boolean paused = false;
    private boolean disconnected = false;
    private boolean illuminated = false;
    private ScannerDataCallback dataCallback;
    private ScannerStatusCallback statusCallback;
    private Mode mode;

    @Override
    public String getProviderKey() {
        return MockProvider.PROVIDER_NAME;
    }

    /**
     * Send a barcode to the scanner to trigger onData callbacks.
     * @param barcode The barcode.
     * @throws RuntimeException When the scan method is called when the scanner is either paused or disconnected.
     */
    public void scan(final Barcode barcode) {
        if (paused || disconnected) {
            Log.d(LOG_TAG, "Received barcode while " + (paused ? "paused" : "disconnected"));
            throw new RuntimeException("Received barcode while the scanner was " + (paused ? "paused" : "disconnected"));
        }
        Log.d(LOG_TAG, "Received barcode of type " + barcode.getBarcodeType() + ": " + barcode.getBarcode());
        final List<Barcode> barcodeList = new ArrayList<>();
        barcodeList.add(barcode);
        dataCallback.onData(this, barcodeList);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialize(final Context ctx, final ScannerInitCallback scannerInitCallback, final ScannerDataCallback scannerDataCallback, final ScannerStatusCallback scannerStatusCallback, final Mode mode) {
        dataCallback = scannerDataCallback;
        statusCallback = scannerStatusCallback;
        this.mode = mode;

        statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.READY);
        scannerInitCallback.onConnectionSuccessful(this);
    }

    @Override
    public void setDataCallBack(ScannerDataCallback cb) {
        dataCallback = cb;
    }

    @Override
    public void disconnect() {
        Log.d(LOG_TAG, "Scanner disconnected");
        disconnected = true;
        statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.DISCONNECTED);
    }

    @Override
    public void pause() {
        Log.d(LOG_TAG, "Scanner paused");
        paused = true;
        statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.PAUSED);
    }

    @Override
    public void resume() {
        Log.d(LOG_TAG, "Scanner resumed");
        paused = false;
        statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.READY);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Beep
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful() {
        Log.d(LOG_TAG, "Success beep");
    }

    @Override
    public void beepScanFailure() {
        Log.d(LOG_TAG, "Failure beep");
    }

    @Override
    public void beepPairingCompleted() {
        Log.d(LOG_TAG, "Pairing beep");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Illumination
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void enableIllumination() {
        Log.d(LOG_TAG, "Illumination on");
        illuminated = true;
    }

    @Override
    public void disableIllumination() {
        Log.d(LOG_TAG, "Illumination off");
        illuminated = false;
    }

    @Override
    public void toggleIllumination() {
        Log.d(LOG_TAG, "Illumination switched");
        illuminated = !illuminated;
    }

    @Override
    public boolean isIlluminationOn() {
        return illuminated;
    }

    @Override
    public boolean supportsIllumination() {
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LED
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void ledColorOn(Color color) {
        Log.d(LOG_TAG, "LED color on with color " + color.name());
    }

    @Override
    public void ledColorOff(Color color) {
        Log.d(LOG_TAG, "LED color off");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // INVENTORY
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getStatus(String key) {
        return null;
    }

    @Override
    public String getStatus(String key, boolean allowCache) {
        return null;
    }

    @Override
    public Map<String, String> getStatus() {
        return new HashMap<>();
    }
}
