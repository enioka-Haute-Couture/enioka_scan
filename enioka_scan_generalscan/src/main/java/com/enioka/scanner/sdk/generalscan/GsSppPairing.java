package com.enioka.scanner.sdk.generalscan;

import static com.enioka.scanner.helpers.Common.buildBarcode;
import static com.enioka.scanner.helpers.Common.findMacAddress;

import android.graphics.Bitmap;
import android.util.Log;

import com.enioka.scanner.bt.api.BarcodePairing;
import com.google.zxing.WriterException;


public class GsSppPairing implements BarcodePairing.WithBarcodePairingSupport, BarcodePairing.WithNfcPairingSupport {
    private final int DEFAULT_WIDTH = BarcodePairing.WithBarcodePairingSupport.DEFAULT_WIDTH;
    private final int DEFAULT_HEIGHT = BarcodePairing.WithBarcodePairingSupport.DEFAULT_HEIGHT;
    private final String LOG_TAG = "GsSppPairing";

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
        // Source: reverse engineering the GeneralScan pairing barcode from  the MData app
        String barcode = "<" + macAddress + ">";
        try {
            return buildBarcode(barcode, width, height);
        } catch (WriterException e) {
            Log.e("GenInitBarcode", "Error while creating barcode", e);
        }
        return null;
    }

    @Override
    public Bitmap activateBluetoothBarcode() {
        return activateBluetoothBarcode(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public Bitmap activateBluetoothBarcode(int width, int height) {
        String barcode = "00000008";

        try {
            return buildBarcode(barcode, width, height);
        } catch (WriterException e) {
            Log.e("GenInitBarcode", "Error while creating barcode", e);
        }
        return null;
    }

    @Override
    public Bitmap defaultSettingsBarcode(Defaults type) {
        return defaultSettingsBarcode(type, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public Bitmap defaultSettingsBarcode(Defaults type, int width, int height) {

        if (type == Defaults.FACTORY) {
            String barcode = "10000001";
            try {
                return buildBarcode(barcode, width, height);
            } catch (WriterException e) {
                Log.e(LOG_TAG, "Error while creating barcode", e);
            }
        } else if (type == Defaults.RESTORE) {
            String barcode = "10000001";
            try {
                return buildBarcode(barcode, width, height);
            } catch (WriterException e) {
                Log.e(LOG_TAG, "Error while creating barcode", e);
            }
        }
        return null;
    }
}
