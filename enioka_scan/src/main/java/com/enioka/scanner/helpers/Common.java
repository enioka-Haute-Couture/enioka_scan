package com.enioka.scanner.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;

/**
 * A set of helpers for scanner SDK providers.
 */
public final class Common {
    private static ToneGenerator tg;
    private static ToneGenerator toneGenerator() {
        if (tg == null) {
            tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        }
        return tg;
    }
    /**
     * Short high beep to indicate successful scan
     */
    public static void beepScanSuccessful() {
        toneGenerator().startTone(ToneGenerator.TONE_PROP_PROMPT, 100);
    }

    /**
     * Long low beep to indicate unsuccessful scan
     */
    public static void beepScanFailure() {
        toneGenerator().startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 300);
    }

    /**
     * Different beep to indicate a completed barcode pairing
     */
    public static void beepPairingCompleted() {
        toneGenerator().startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 300);
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

    public static boolean hasCamera(Context a) {
        PackageManager pm = a.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}
