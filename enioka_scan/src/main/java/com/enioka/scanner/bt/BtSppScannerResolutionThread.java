package com.enioka.scanner.bt;

import android.util.Log;

import com.enioka.scanner.api.Scanner;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Responsible for transforming {@link BtDevice} into {@link Scanner}. As this is a long and costly operation
 * (as providers may need to wait for device answer timeout to their "are you a type XXX device" questions)
 * there is one thread per BT device.
 */
class BtSppScannerResolutionThread implements Runnable, BtSppScannerProvider.ManagementCallback {
    private static final String LOG_TAG = "InternalBtDevice";

    private BtDevice device;
    private List<BtSppScannerProvider> scannerProviders;
    private ScannerResolutionCallback callback;
    private final Semaphore providerLock = new Semaphore(0);
    private boolean found = false;


    interface ScannerResolutionCallback {
        //TODO: return Scanner and not BtDevice.
        void onConnection(BtDevice scanner, BtSppScannerProvider compatibleProvider);

        void notCompatible(BtDevice device);
    }

    BtSppScannerResolutionThread(BtDevice device, List<BtSppScannerProvider> scannerProviders, ScannerResolutionCallback callback) {
        this.device = device;
        this.scannerProviders = scannerProviders;
        this.callback = callback;
    }

    @Override
    public void run() {
        for (BtSppScannerProvider provider : scannerProviders) {
            Log.d(LOG_TAG, "Testing compatibility of scanner " + device.getName() + " with provider " + provider.getClass().getSimpleName());
            device.setProvider(provider); // Analyse returned data with the current provider.
            provider.canManageDevice(device, this);
            try {
                providerLock.acquire(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (found) {
                Log.i(LOG_TAG, "Scanner " + device.getName() + " is compatible with provider " + provider.getClass().getSimpleName());
                callback.onConnection(device, provider);
                return;
            }

            Log.i(LOG_TAG, "Scanner " + device.getName() + " is not compatible with provider " + provider.getClass().getSimpleName());
        }

        callback.notCompatible(device);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Callbacks from provider
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void canManage() {
        found = true;
        providerLock.release(1);
    }

    @Override
    public void cannotManage() {
        providerLock.release(1);
    }

}
