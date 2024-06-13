package com.enioka.scanner.sdk.honeywelloss.spp;

import static com.enioka.scanner.helpers.Common.buildBarcode;
import static com.enioka.scanner.helpers.Common.findMacAddress;

import android.graphics.Bitmap;
import android.util.Log;

import com.enioka.scanner.bt.api.BarcodePairing;
import com.google.zxing.WriterException;

public class HoneyWellOssSppPairing implements BarcodePairing.WithBarcodePairingSupport, BarcodePairing.WithNfcPairingSupport {
    private final int DEFAULT_WIDTH = BarcodePairing.WithBarcodePairingSupport.DEFAULT_WIDTH;
    private final int DEFAULT_HEIGHT = BarcodePairing.WithBarcodePairingSupport.DEFAULT_HEIGHT;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BARCODE PAIRING
    ////////////////////////////////////////////////////////////////////////////////////////////////


    public Bitmap createBarcode(String macAddress, int width, int height) {
        // Create a barcode with the MAC address
        Log.i("GenPairingBarcode", "Mac address: " + macAddress);

        // Should be this format (https://sps-support.honeywell.com/s/article/How-to-connect-a-3820-4820-1202-1902-to-Bluetooth-module-using-a-barcode)
        String barcodeData = "BT_ADR" + macAddress.replaceAll(":", "").toUpperCase() + ".FNC3";
        try {
            return buildBarcode(barcodeData, width, height);
        } catch (WriterException e) {
            Log.e("GenPairingBarcode", "Error while creating barcode", e);
        }
        return null;
    }

    @Override
    public Bitmap getPairingBarcode() {
        return getPairingBarcode(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public Bitmap getPairingBarcode(int width, int height) {
        // Check bluetooth permissions
        String macAddress = findMacAddress();
        if (macAddress == null) {
            Log.e("GenPairingBarcode", "Could not find MAC address");
            return null;
        }
        return createBarcode(macAddress, width, height);
    }

    @Override
    public Bitmap activateBluetoothBarcode() {
        return activateBluetoothBarcode(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public Bitmap activateBluetoothBarcode(int width, int height) {
        // Should be this format (https://sps-support.honeywell.com/s/article/How-to-connect-a-3820-4820-1202-1902-to-Bluetooth-module-using-a-barcode)
        String barcode = "BT_DNG6";

        try {
            return buildBarcode(barcode, width, height);
        } catch (WriterException e) {
            Log.e("GenInitBarcode", "Error while creating barcode", e);
        }
        return null;
    }
}
