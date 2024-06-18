package com.enioka.scanner.sdk.koamtac;

import static com.enioka.scanner.helpers.Common.buildBarcode;
import static com.enioka.scanner.helpers.Common.findMacAddress;

import android.graphics.Bitmap;
import android.util.Log;

import com.enioka.scanner.bt.api.BarcodePairing;
import com.google.zxing.WriterException;

public class KoamtacPairing implements BarcodePairing.WithBarcodePairingSupport, BarcodePairing.WithNfcPairingSupport {
    final String LOG_TAG = "KoamtacPairing";

    @Override
    public Bitmap getPairingBarcode() {
        return getPairingBarcode(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public Bitmap getPairingBarcode(int width, int height) {
        String macAddress = findMacAddress();
        Log.i(LOG_TAG, "Mac address: " + macAddress);
        if (macAddress == null) {
            Log.e(LOG_TAG, "Could not find MAC address");
            return null;
        }
        // https://koamtac.com/wp-content/uploads/Connecting-your-KDC-to-an-Android-SmartphoneTablet.pdf
        String barcode = "\u00f3" + "65" + macAddress.replaceAll(":", "").toUpperCase();
        try {
            return buildBarcode(barcode, width, height);
        } catch (WriterException e) {
            Log.e(LOG_TAG, "Error while creating barcode", e);
        }
        return null;
    }

    @Override
    public Bitmap activateBluetoothBarcode() {
        return activateBluetoothBarcode(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public Bitmap activateBluetoothBarcode(int width, int height) {
        // https://koamtac.com/wp-content/uploads/Connecting-your-KDC-to-an-Android-SmartphoneTablet.pdf
        String barcode = "6A000";

        try {
            return buildBarcode(barcode, width, height);
        } catch (WriterException e) {
            Log.e(LOG_TAG, "Error while creating barcode", e);
        }
        return null;
    }
}
