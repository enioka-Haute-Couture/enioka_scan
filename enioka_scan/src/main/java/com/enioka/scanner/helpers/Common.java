package com.enioka.scanner.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;

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

    /**
     * Get the mac address of the network interface wlan0, cannot use Android SDK Api since 6.0 :(
     */
    public static String findMacAddress() {
        try {
            ArrayList<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) {
                    continue;
                }

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }

                StringBuilder mac = new StringBuilder();
                for (byte b : macBytes) {
                    mac.append(Integer.toHexString(b & 0xFF)).append(":");
                }

                if (mac.length() > 0) {
                    mac.deleteCharAt(mac.length() - 1);
                }
                return mac.toString();
            }
        } catch (Exception ex) {
            Log.e("GenPairingBarcode", "Error while getting MAC address", ex);
        }
        return null;
    }

    /**
     *  Generate a Bitmap barcode from a given string
     */
    public static Bitmap buildBarcode(String barcodeData, int width, int height) throws WriterException {
        final int BLACK = 0xFF000000;
        final int WHITE = 0xFFFFFFFF;

        MultiFormatWriter barcodeWriter = new MultiFormatWriter();

        BitMatrix bitMatrix = barcodeWriter.encode(barcodeData, BarcodeFormat.CODE_128, width, height);
        Bitmap result = Bitmap.createBitmap(bitMatrix.getWidth(), bitMatrix.getHeight(), Bitmap.Config.ARGB_8888);

        for (int x = 0; x < bitMatrix.getWidth(); x++) {
            for (int y = 0; y < bitMatrix.getHeight(); y++) {
                result.setPixel(x, y, bitMatrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return result;
    }
}
