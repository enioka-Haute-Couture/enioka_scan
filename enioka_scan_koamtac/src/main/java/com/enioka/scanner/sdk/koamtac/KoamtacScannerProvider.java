package com.enioka.scanner.sdk.koamtac;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;

import java.util.List;

import koamtac.kdc.sdk.KDCReader;

public class KoamtacScannerProvider extends KoamtacPairing implements ScannerProvider {
    private static final String LOG_TAG = "KoamtacScannerProvider";
    public static final String PROVIDER_KEY = "Koamtac";

    @Override
    public void getScanner(Context ctx, ProviderCallback cb, ScannerSearchOptions options) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOG_TAG, "Missing BT permission");
            cb.onProviderUnavailable(PROVIDER_KEY);
            return;
        }

        try {
            this.getClass().getClassLoader().loadClass("koamtac.kdc.sdk.KDCReader");
        } catch (ClassNotFoundException e) {
            cb.onProviderUnavailable(PROVIDER_KEY);
            return;
        }
        //KDCReader.EnableDebug(KDCConstants.DebugCategory.ALL_CATEGORY, true);

        List<BluetoothDevice> devices = KDCReader.GetAvailableDeviceList();
        if (devices.isEmpty()) {
            cb.onAllScannersCreated(PROVIDER_KEY);
            return;
        }

        for (BluetoothDevice device : devices) {
            Log.i(LOG_TAG, "Koamtac provider may have found a scanner and will try to connect to it: " + device.getName());
            cb.onScannerCreated(PROVIDER_KEY, device.getName(), new KoamtacScanner(device));
        }

        cb.onAllScannersCreated(PROVIDER_KEY);
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }
}
