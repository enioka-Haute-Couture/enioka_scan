package com.enioka.scanner.sdk.symbol;

import android.content.Context;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerProvider;

/**
 * EMDK SDK provider. For Symbol (formerly Motorola subsidiary, now Zebra subsidiary) devices. Known to work with TC51 and WT6000.
 */
public class SymbolProvider implements ScannerProvider {
    private final static String LOG_TAG = "ZebraInternalImpl";

    @Override
    public Scanner getScanner(Context ctx) {
        try {
            // Support is present when the lib is present on the device (it is not bundled with the application).
            Class c = Class.forName("com.symbol.emdk.EMDKManager");
            Log.i(LOG_TAG, "EMDK class was found at " + c.getCanonicalName());

            // Note the use of reflection: otherwise, the class would be loaded even on platform without the Symbol lib available!
            // It would result in ClassNotFoundException at startup.
            return Class.forName("com.enioka.scanner.sdk.symbol.SymbolScanner").asSubclass(Scanner.class).newInstance();
        } catch (ClassNotFoundException e) {
            // Symbol lib not found => this SDK is not supported on this platform.
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not create a Scanner instance - the implementation may lack a default constructor.", e);
            throw new RuntimeException(e);
        }
    }
}
