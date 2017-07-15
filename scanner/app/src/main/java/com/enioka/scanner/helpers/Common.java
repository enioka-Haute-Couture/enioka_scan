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
        Intent i = new Intent(url);
        return checkIntentListener(i, a);
    }

    /**
     * Returns true if there is a handler for this intent.
     *
     * @param i the intent to check.
     * @return true if a handler exists.
     */
    public static boolean checkIntentListener(Intent i, Context a) {
        PackageManager packageManager = a.getPackageManager();
        return packageManager.queryIntentServices(i, 0).size() > 0 || packageManager.queryIntentActivities(i, 0).size() > 0 || packageManager.queryBroadcastReceivers(i, PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS).size() > 0;
    }
}
