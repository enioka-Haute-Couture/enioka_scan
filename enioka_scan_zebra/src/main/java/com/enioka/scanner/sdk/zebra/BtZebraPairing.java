package com.enioka.scanner.sdk.zebra;

import static com.enioka.scanner.helpers.Common.buildBarcode;
import static com.enioka.scanner.helpers.Common.findMacAddress;

import android.graphics.Bitmap;
import android.util.Log;

import com.enioka.scanner.bt.api.BarcodePairing;
import com.google.zxing.WriterException;

public class BtZebraPairing implements BarcodePairing.WithBarcodePairingSupport, BarcodePairing.WithNfcPairingSupport {
    private static final String LOG_TAG = "BtZebraProvider";
    private static final String BT_CLASSIC_HOST = "16";
    private final int DEFAULT_WIDTH = BarcodePairing.WithBarcodePairingSupport.DEFAULT_WIDTH;
    private final int DEFAULT_HEIGHT = BarcodePairing.WithBarcodePairingSupport.DEFAULT_HEIGHT;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BARCODE PAIRING
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public Bitmap createLegacyBarcode(String macAddress, int width, int height) {
        // Create a barcode with the MAC address
        String barcodeData = "B" + macAddress.replaceAll(":", "").toUpperCase();
        Log.i(LOG_TAG, "Legacy pairing barcode: " + barcodeData);

        try {
            return buildBarcode(barcodeData, width, height);
        } catch (WriterException e) {
            Log.e(LOG_TAG, "Error while creating barcode", e);
        }
        return null;
    }

    public Bitmap createExtendedBarcode(String macAddress, int width, int height) {
        // Create a barcode with the MAC address
        String barcodeData = "PH" + BT_CLASSIC_HOST + "A" + macAddress.replaceAll(":", "").toUpperCase();
        Log.i(LOG_TAG, "Extended pairing barcode: " + barcodeData);

        try {
            return buildBarcode(barcodeData, width, height);
        } catch (WriterException e) {
            Log.e(LOG_TAG, "Error while creating barcode", e);
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
        Log.i(LOG_TAG, "Mac address: " + macAddress);
        if (macAddress == null) {
            Log.e(LOG_TAG, "Could not find MAC address");
            return null;
        }
        return createExtendedBarcode(macAddress, width, height);
    }

    public Bitmap getLegacyPairingBarcode() {
        return getLegacyPairingBarcode(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public Bitmap getLegacyPairingBarcode(int width, int height) {
        // Check bluetooth permissions
        String macAddress = findMacAddress();
        Log.i(LOG_TAG, "Mac address: " + macAddress);
        if (macAddress == null) {
            Log.e(LOG_TAG, "Could not find MAC address");
            return null;
        }
        return createLegacyBarcode(macAddress, width, height);
    }

    @Override
    public Bitmap getPairingBarcode(BarcodePairing.WithBarcodePairingSupport.PairingType type) {
        return getPairingBarcode(type, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public Bitmap getPairingBarcode(BarcodePairing.WithBarcodePairingSupport.PairingType type, int width, int height) {
        switch (type) {
            case LEGACY:
                return getLegacyPairingBarcode(width, height);
            case NORMAL:
                return getPairingBarcode(width, height);
            default:
                return null;
        }
    }

    @Override
    public Bitmap zebraSetBluetoothHostBarcode() {
        return zebraSetBluetoothHostBarcode(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public Bitmap zebraSetBluetoothHostBarcode(int width, int height) {
        String barcodeData = "PH" + BT_CLASSIC_HOST;
        Log.i(LOG_TAG, "Bluetooth host barcode: " + barcodeData);

        try {
            return buildBarcode(barcodeData, width, height);
        } catch (WriterException e) {
            Log.e(LOG_TAG, "Error while creating barcode", e);
        }
        return null;
    }

    @Override
    public Bitmap zebraConnectToAddressBarcode() {
        return zebraConnectToAddressBarcode(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public Bitmap zebraConnectToAddressBarcode(int width, int height) {
        String macAddress = findMacAddress();
        Log.i(LOG_TAG, "Mac address: " + macAddress);
        if (macAddress == null) {
            Log.e(LOG_TAG, "Could not find MAC address");
            return null;
        }

        String barcodeData = "PA" + macAddress.replaceAll(":", "").toUpperCase();
        Log.i(LOG_TAG, "Connect to address barcode: " + barcodeData);

        try {
            return buildBarcode(barcodeData, width, height);
        } catch (WriterException e) {
            Log.e(LOG_TAG, "Error while creating barcode", e);
        }
        return null;
    }

    @Override
    public Bitmap defaultSettingsBarcode(Defaults type) {
        return defaultSettingsBarcode(type, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public Bitmap defaultSettingsBarcode(Defaults type, int width, int height) {
        String barcodeData = "PD" + type.getValue();
        Log.i(LOG_TAG, "Reset barcode: " + barcodeData);

        try {
            return buildBarcode(barcodeData, width, height);
        } catch (WriterException e) {
            Log.e(LOG_TAG, "Error while creating barcode", e);
        }
        return null;
    }
}
