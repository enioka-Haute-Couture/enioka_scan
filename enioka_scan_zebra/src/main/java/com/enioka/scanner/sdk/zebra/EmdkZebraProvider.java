package com.enioka.scanner.sdk.zebra;

import android.content.Context;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;

/**
 * EMDK SDK provider. For Symbol (formerly Motorola subsidiary, now Zebra subsidiary) devices. Known to work with TC51 and WT6000.
 */
public class EmdkZebraProvider implements ScannerProvider {
    private final static String LOG_TAG = "EmdkProvider";
    public static final String PROVIDER_KEY = "Zebra EMDK";

    @Override
    public void getScanner(Context ctx, ProviderCallback cb, ScannerSearchOptions options) {
        try {
            // Support is present when the lib is present on the device (it is not bundled with the application).
            Log.d(LOG_TAG, "Looking for class com.symbol.emdk.EMDKManager");
            Class c = Class.forName("com.symbol.emdk.EMDKManager");
            Log.i(LOG_TAG, "EMDK class was found at " + c.getCanonicalName());

            // Note the use of reflection: otherwise, the class would be loaded even on platform without the Symbol lib available!
            // It would result in ClassNotFoundException at startup.
            cb.onScannerCreated(PROVIDER_KEY, "r", Class.forName("com.enioka.scanner.sdk.zebra.EmdkZebraScanner").asSubclass(Scanner.class).newInstance());
        } catch (ClassNotFoundException e) {
            // Symbol lib not found => this SDK is not supported on this platform.
            Log.i(LOG_TAG, "Not a Zebra EMDK device");
            cb.onProviderUnavailable(PROVIDER_KEY);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not create a Scanner instance - the implementation may lack a default constructor.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }
}
