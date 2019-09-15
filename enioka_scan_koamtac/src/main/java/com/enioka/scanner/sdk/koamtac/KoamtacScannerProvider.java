package com.enioka.scanner.sdk.koamtac;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerProviderBinder;
import com.enioka.scanner.api.ScannerSearchOptions;

import java.util.List;

import koamtac.kdc.sdk.KDCReader;

public class KoamtacScannerProvider extends Service implements ScannerProvider {
    static String PROVIDER_KEY = "Koamtac";

    private final IBinder binder = new ScannerProviderBinder(this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

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
