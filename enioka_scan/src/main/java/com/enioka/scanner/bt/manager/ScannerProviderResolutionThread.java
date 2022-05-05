package com.enioka.scanner.bt.manager;

import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.manager.common.BluetoothScannerInternal;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Responsible for transforming {@link BluetoothScannerInternal} into {@link Scanner}. As this is a long and costly operation
 * (as providers may need to wait for device answer timeout to their "are you a type XXX device" questions)
 * there is one thread per BT device.
 */
class ScannerProviderResolutionThread implements Runnable, BtSppScannerProvider.ManagementCallback {
    private static final String LOG_TAG = "BtSppSdk";

    private final BluetoothScannerInternal device;
    private final List<BtSppScannerProvider> scannerProviders;
    private final ScannerResolutionCallback callback;
    private final Semaphore providerLock = new Semaphore(0);
    private Scanner foundLibraryScanner;

    /**
     * Methods to call at the end of the analysis of a given scanner by all provider.
     * ScannerResolutionThread uses this interface to communicate its asynchronous results.
     */
    interface ScannerResolutionCallback {
        /**
         * At least one provider is compatible with the scanner.
         *
         * @param scanner            the scanner which was analysed
         * @param compatibleProvider the compatible provider
         */
        void onConnection(Scanner scanner, BtSppScannerProvider compatibleProvider);

        /**
         * The scanner is not compatible with any known provider.
         *
         * @param device the scanner which failed to find a home provider. Very sad.
         */
        void notCompatible(BluetoothScannerInternal device);
    }

    /**
     * Create a new resolver for a given BT device (Classic or BLE).
     *
     * @param device           a connected device, ready to run commands.
     * @param scannerProviders the list of BT scanner providers against which to check the compatibility of device.
     * @param callback         what to do when resolution is done or failed
     */
    ScannerProviderResolutionThread(BluetoothScannerInternal device, List<BtSppScannerProvider> scannerProviders, ScannerResolutionCallback callback) {
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

            // We now wait. Because only one provider is allowed to do its analysis at a time - there is only one socket available to the scanner!
            try {
                providerLock.acquire(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Callbacks canManage and cannotManage do set foundLibraryScanner if provider is compatible.
            if (foundLibraryScanner != null) {
                Log.i(LOG_TAG, "Scanner " + device.getName() + " is compatible with provider " + provider.getClass().getSimpleName());
                callback.onConnection(foundLibraryScanner, provider);
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
    public void canManage(Scanner libraryScanner) {
        foundLibraryScanner = libraryScanner;
        providerLock.release(1);
    }

    @Override
    public void cannotManage() {
        foundLibraryScanner = null;
        providerLock.release(1);
    }

}
