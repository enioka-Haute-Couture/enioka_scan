package com.enioka.scanner.sdk.mock;

import android.content.Context;
import android.util.Log;

import com.enioka.scanner.api.Color;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.callbacks.ScannerDataCallback;
import com.enioka.scanner.api.callbacks.ScannerInitCallback;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerInitCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.data.Barcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock scanner used to test callbacks and basic scanner interactions.
 * It does not bind to any bluetooth device or intent, "scanned" data has to be passed manually.
 */
public class MockScanner implements Scanner {
    static final String LOG_TAG = "MockScanner";

    private boolean paused = false;
    private boolean disconnected = false;
    private boolean illuminated = false;
    private boolean softTriggered = false;
    private ScannerDataCallback dataCallback;
    private ScannerStatusCallback statusCallback;
    private Mode mode;

    @Override
    public String getProviderKey() {
        return MockProvider.PROVIDER_KEY;
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
    // SOFTWARE TRIGGERS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void pressScanTrigger() {
        Log.d(LOG_TAG, "Scanner trigger pressed");
        softTriggered = true;
        statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.SCANNING);
    }

    @Override
    public void releaseScanTrigger() {
        Log.d(LOG_TAG, "Scanner trigger released");
        softTriggered = false;
        statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.READY);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Finishes the initialization of the Mock. This version of the method is best suited for unit tests as it will not require callback proxies (the context may be null).
     */
    @Override
    public void initialize(final Context applicationContext, final ScannerInitCallback initCallback, final ScannerDataCallback dataCallback, final ScannerStatusCallback statusCallback, final Mode mode) {
        Log.w(LOG_TAG, "The Mock was initialized with non-proxy callbacks, UI-thread won't be used (best suitable for unit tests)");
        this.dataCallback = dataCallback;
        this.statusCallback = statusCallback;
        this.mode = mode;

        statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.READY);
        initCallback.onConnectionSuccessful(this);
    }

    /**
     * Finishes the initialization of the Mock. This version of the method is best suited for integrated tests as proxy callbacks are used, meaning jumps to UI-thread which are not mocked.
     */
    @Override
    public void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, final Mode mode) {
        Log.w(LOG_TAG, "The Mock was initialized with proxy callbacks, UI-thread will be used (only suitable for android integrated tests)");
        this.dataCallback = dataCallback;
        this.statusCallback = statusCallback;
        this.mode = mode;

        statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.READY);
        initCallback.onConnectionSuccessful(this);
    }

    @Override
    public void setDataCallBack(ScannerDataCallbackProxy cb) {
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
