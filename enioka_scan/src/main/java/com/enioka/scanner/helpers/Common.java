package com.enioka.scanner.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * A set of helpers for scanner SDK providers.
 */
public final class Common {
    /**
     * Short high beep to indicate successful scan
     */
    public static void beepScanSuccessful() {
        ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tg.startTone(ToneGenerator.TONE_PROP_PROMPT, 100);
        tg.release();
    }

    /**
     * Long low beep to indicate unsuccessful scan
     */
    public static void beepScanFailure() {
        ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tg.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 300);
        tg.release();
    }

    /**
     * Different beep to indicate a completed barcode pairing
     */
    public static void beepPairingCompleted() {
        ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 300);
        tg.release();
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

    // TODO: move code from compat activity to here.
    public static void askForPermission(Activity ctx) {
        boolean arePermissionsGranted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ctx, "com.symbol.emdk.permission.EMDK") == PackageManager.PERMISSION_GRANTED;
        if (!arePermissionsGranted) {
            ActivityCompat.requestPermissions(ctx, new String[]{Manifest.permission.CAMERA, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, "com.symbol.emdk.permission.EMDK", Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 1789);
        }

    }
}
