package com.enioka.scanner.sdk.zebra;

import android.content.Context;
import android.util.Log;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Provider for the BT Zebra SDK.
 */
public class BtZebraProvider extends BtZebraPairing implements ScannerProvider {
    private static final String LOG_TAG = "BtZebraProvider";
    public static final String PROVIDER_KEY = "BtZebraProvider";

    /**
     * We store the connector to avoid any GC on it, and therefore avoid scanner connection to fail randomly.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private Object connector;

    @Override
    public void getScanner(Context ctx, final ProviderCallback cb, ScannerSearchOptions options) {
        Log.i(LOG_TAG, "Starting scanner search");
        try {
            this.getClass().getClassLoader().loadClass("com.zebra.scannercontrol.SDKHandler");
        } catch (ClassNotFoundException e) {
            Log.i(LOG_TAG, "The Zebra bluetooth driver is not present in the project path, so provider is disabled");
            cb.onProviderUnavailable(PROVIDER_KEY);
            return;
        }

        try {
            Class<?> connectorClass = this.getClass().getClassLoader().loadClass("com.enioka.scanner.sdk.zebra.BtZebraProviderConnector");
            connector = connectorClass.newInstance();
            Method m = connectorClass.getMethod("startScannerSearch", Context.class, ProviderCallback.class, ScannerSearchOptions.class);
            m.invoke(connector, ctx, cb, options);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
                 NoSuchMethodException | InvocationTargetException e) {
            Log.e(LOG_TAG, "Could not instantiate the provider, even if the driver is present. Provider disabled.", e);
            cb.onProviderUnavailable(PROVIDER_KEY);
        }
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }
}
