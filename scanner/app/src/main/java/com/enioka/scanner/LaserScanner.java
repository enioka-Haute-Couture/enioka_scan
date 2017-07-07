package com.enioka.scanner;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.exc.NoLaserScanner;
import com.enioka.scanner.sdk.hht.HHTProvider;
import com.enioka.scanner.sdk.symbol.SymbolProvider;
import com.enioka.scanner.sdk.zebra.ZebraProvider;

/**
 * A proxy class - it will route the API calls to SDK-specific implementations.
 */
public class LaserScanner implements Scanner {
    private static final String LOG_TAG = "LaserScanner";

    /**
     * The list of available scanner providers. (manual for now => no useless complicated plugin system)
     */
    private static ScannerProvider[] laserProviders = new ScannerProvider[]{new SymbolProvider(), new ZebraProvider(), new HHTProvider()};

    /**
     * The SDK-specific scanner.
     */
    private Scanner scanner = null;

    /**
     * Throws {@link com.enioka.scanner.exc.ScannerException} if no external scanner present.
     */
    private static Scanner getLaserScanner(Activity ctx) {
        // Ask for permissions.
        boolean arePermissionsGranted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
        if (!arePermissionsGranted) {
            ActivityCompat.requestPermissions(ctx, new String[]{Manifest.permission.CAMERA, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, 1789);
        }

        // Now create a scanner.
        Scanner res;
        for (ScannerProvider sp : laserProviders) {
            res = sp.getScanner(ctx);
            if (res != null) {
                return res;
            }
        }
        throw new NoLaserScanner();
    }


    @Override
    public void initialize(Activity ctx, ScannerInitCallback cb0, ScannerDataCallback cb1, ScannerStatusCallback cb2, Mode mode) {
        this.scanner = LaserScanner.getLaserScanner(ctx);
        if (this.scanner == null) {
            throw new IllegalStateException("there is no available barcode scanner on this device");
        }
        this.scanner.initialize(ctx, cb0, cb1, cb2, mode);
        Log.i(LOG_TAG, "Scanner was initialized with implementation " + this.scanner.getClass().getCanonicalName());
    }


    @Override
    public void disconnect() {
        try {
            this.scanner.disconnect();
        } catch (Exception e) {
            Log.e(LOG_TAG, "could not disconnect scanner", e);
        }
    }

    @Override
    public void beepScanSuccessful() {
        this.scanner.beepScanSuccessful();
    }

    @Override
    public void beepScanFailure() {
        this.scanner.beepScanFailure();
    }

    @Override
    public void beepPairingCompleted() {
        this.scanner.beepPairingCompleted();
    }

    @Override
    public void enableIllumination() {
        this.scanner.enableIllumination();
    }

    @Override
    public void disableIllumination() {
        this.scanner.disableIllumination();
    }

    @Override
    public void toggleIllumination() {
        this.scanner.toggleIllumination();
    }

    @Override
    public boolean isIlluminationOn() {
        return this.scanner.isIlluminationOn();
    }

    @Override
    public boolean supportsIllumination() {
        return this.scanner.supportsIllumination();
    }
}
