package com.enioka.scanner.bt;

import com.enioka.scanner.api.Scanner;

import java.util.List;

/**
 * Responsible for transforming {@link BtDevice} into {@link Scanner}. As this is a long and costly operation
 * (as providers may need to wait for device answer timeout to their "are you a type XXX device" questions)
 * there is one thread per BT device.
 */
class BtSppScannerResolutionThread implements Runnable {

    private BtDevice device;
    private List<BtSppScannerProvider> scannerProviders;
    private ScannerResolutionCallback callback;

    interface ScannerResolutionCallback {
        void onConnection(Scanner scanner);

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
            if (provider.canManageDevice(device)) {
                callback.onConnection(null);
                return;
            }
        }
        callback.notCompatible(device);
    }
}
