package com.enioka.scanner.sdk.zebraoss;

import static com.enioka.scanner.helpers.Common.buildBarcode;
import static com.enioka.scanner.helpers.Common.findMacAddress;

import android.graphics.Bitmap;
import android.util.Log;

import com.enioka.scanner.bt.api.BarcodePairing;
import com.google.zxing.WriterException;

public class ZebraOssPairing implements BarcodePairing.WithBarcodePairingSupport, BarcodePairing.WithNfcPairingSupport {
    private static final String LOG_TAG = "SsiParser";
    private static final String PROVIDER_BT_CLASSIC = "BT_ZebraOssSPPProvider";
    private static final String BT_CLASSIC_HOST = "16";
    private static final String BLE_HOST = "17";
    private final String providerKey;

    private final int DEFAULT_WIDTH = BarcodePairing.WithBarcodePairingSupport.DEFAULT_WIDTH;
    private final int DEFAULT_HEIGHT = BarcodePairing.WithBarcodePairingSupport.DEFAULT_HEIGHT;

    public ZebraOssPairing(String providerKey) {
        this.providerKey = providerKey;
    }


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
        String bluetoothHost = this.providerKey.equals(PROVIDER_BT_CLASSIC) ? BT_CLASSIC_HOST : BLE_HOST;
        String barcodeData = "PH" + bluetoothHost + "A" + macAddress.replaceAll(":", "").toUpperCase();
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
        // Check bluetooth permissions
        String macAddress = findMacAddress();
        Log.i(LOG_TAG, "Mac address: " + macAddress);
        if (macAddress == null) {
            Log.e(LOG_TAG, "Could not find MAC address");
            return null;
        }
        return createLegacyBarcode(macAddress, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public Bitmap getPairingBarcode(PairingType type) {
       return getPairingBarcode(type, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    @Override
    public Bitmap getPairingBarcode(PairingType type, int width, int height) {
        switch (type) {
            case LEGACY:
                return getLegacyPairingBarcode();
            case NORMAL:
                return getPairingBarcode();
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
        String bluetoothHost = this.providerKey.equals(PROVIDER_BT_CLASSIC) ? BT_CLASSIC_HOST : BLE_HOST;
        String barcodeData = "PH" + bluetoothHost;
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
