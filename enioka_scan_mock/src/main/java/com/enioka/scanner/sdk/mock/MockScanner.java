package com.enioka.scanner.sdk.mock;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.enioka.scanner.api.ScannerLedColor;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.callbacks.ScannerDataCallback;
import com.enioka.scanner.api.callbacks.ScannerInitCallback;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.api.proxies.ScannerCommandCallbackProxy;
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
public class MockScanner implements Scanner, Scanner.WithTriggerSupport, Scanner.WithBeepSupport, Scanner.WithIlluminationSupport, Scanner.WithLedSupport, Scanner.WithInventorySupport {
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
    public void pressScanTrigger(@Nullable ScannerCommandCallbackProxy cb) {
        Log.d(LOG_TAG, "Scanner trigger pressed");
        softTriggered = true;
        statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.SCANNING);

        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void releaseScanTrigger(@Nullable ScannerCommandCallbackProxy cb) {
        Log.d(LOG_TAG, "Scanner trigger released");
        softTriggered = false;
        statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.READY);

        if (cb != null) {
            cb.onSuccess();
        }
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
    public void disconnect(@Nullable ScannerCommandCallbackProxy cb) {
        Log.d(LOG_TAG, "Scanner disconnected");
        disconnected = true;
        statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.DISCONNECTED);

        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void pause(@Nullable ScannerCommandCallbackProxy cb) {
        Log.d(LOG_TAG, "Scanner paused");
        paused = true;
        statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.PAUSED);

        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void resume(@Nullable ScannerCommandCallbackProxy cb) {
        Log.d(LOG_TAG, "Scanner resumed");
        paused = false;
        statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.READY);

        if (cb != null) {
            cb.onSuccess();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Beep
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful(@Nullable ScannerCommandCallbackProxy cb) {
        Log.d(LOG_TAG, "Success beep");
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepScanFailure(@Nullable ScannerCommandCallbackProxy cb) {
        Log.d(LOG_TAG, "Failure beep");
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepPairingCompleted(@Nullable ScannerCommandCallbackProxy cb) {
        Log.d(LOG_TAG, "Pairing beep");
        if (cb != null) {
            cb.onSuccess();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Illumination
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void enableIllumination(@Nullable ScannerCommandCallbackProxy cb) {
        Log.d(LOG_TAG, "Illumination on");
        illuminated = true;
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void disableIllumination(@Nullable ScannerCommandCallbackProxy cb) {
        Log.d(LOG_TAG, "Illumination off");
        illuminated = false;
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void toggleIllumination(@Nullable ScannerCommandCallbackProxy cb) {
        Log.d(LOG_TAG, "Illumination switched");
        illuminated = !illuminated;
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public boolean isIlluminationOn() {
        return illuminated;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LED
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void ledColorOn(ScannerLedColor color, @Nullable ScannerCommandCallbackProxy cb) {
        Log.d(LOG_TAG, "LED color on with color " + color.name());
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void ledColorOff(ScannerLedColor color, @Nullable ScannerCommandCallbackProxy cb) {
        Log.d(LOG_TAG, "LED color off");
        if (cb != null) {
            cb.onSuccess();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // INVENTORY
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getStatus(String key) {
        Log.d(LOG_TAG, "Returning status " + key + " (null)");
        return null;
    }

    @Override
    public String getStatus(String key, boolean allowCache) {
        Log.d(LOG_TAG, "Returning status " + key + " with cache " + (allowCache ? "enabled" : "disabled") + " (null)");
        return null;
    }

    @Override
    public Map<String, String> getStatus() {
        Log.d(LOG_TAG, "Returning map of status values (empty map)");
        return new HashMap<>();
    }
}
