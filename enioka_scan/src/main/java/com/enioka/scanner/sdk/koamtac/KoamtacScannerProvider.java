package com.enioka.scanner.sdk.koamtac;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;

import java.util.List;

import koamtac.kdc.sdk.KDCReader;

public class KoamtacScannerProvider implements ScannerProvider {
    static String PROVIDER_KEY = "Koamtac";

    @Override
    public void getScanner(Context ctx, ProviderCallback cb, ScannerSearchOptions options) {
        if (!options.useBlueTooth) {
            cb.onProviderUnavailable(PROVIDER_KEY);
            return;
        }

        List<BluetoothDevice> devices = KDCReader.GetAvailableDeviceList();
        if (devices.isEmpty()) {
            cb.onAllScannersCreated(PROVIDER_KEY);
            return;
        }

        for (BluetoothDevice device : devices) {
            cb.onScannerCreated(PROVIDER_KEY, device.getName(), new KoamtacScanner(device));
        }

        cb.onAllScannersCreated(PROVIDER_KEY);
    }

    @Override
    public String getKey() {
        return null;
    }
}
