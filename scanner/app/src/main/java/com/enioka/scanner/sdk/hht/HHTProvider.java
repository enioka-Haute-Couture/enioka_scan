package com.enioka.scanner.sdk.hht;

import android.content.Context;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerProvider;

/**
 * Provider for the HHT Wrapper Layer
 */
public class HHTProvider implements ScannerProvider {
    private static final String LOG_TAG = "HHTProvider";

    @Override
    public Scanner getScanner(Context ctx) {
        // CHeck if SPA43
        return new HHTScanner();
    }
}
