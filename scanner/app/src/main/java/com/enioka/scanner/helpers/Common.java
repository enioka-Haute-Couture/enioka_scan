package com.enioka.scanner.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.enioka.scanner.camera.ZbarScanView;

/**
 * A set of helpers for scanner SDK providers.
 */
public final class Common {
    /**
     * Short high beep to indicate successful scan
     */
    public static void beepScanSuccessful() {
        ZbarScanView.beepOk();
    }

    /**
     * Long low beep to indicate unsuccessful scan
     */
    public static void beepScanFailure() {
        ZbarScanView.beepKo();
    }

    /**
     * Different beep to indicate a completed barcode pairing
     */
    public static void beepPairingCompleted() {
        ZbarScanView.beepWaiting();
    }

    /**
     * Returns true if there is a handler for this intent.
     *
     * @param url the intent to check.
     * @return true if a handler exists.
     */
    public static boolean checkIntentListener(String url, Context a) {
        PackageManager packageManager = a.getPackageManager();
        Intent i = new Intent(url);
        return packageManager.queryIntentServices(i, 0).size() > 0 || packageManager.queryIntentActivities(i, 0).size() > 0;
    }
}
