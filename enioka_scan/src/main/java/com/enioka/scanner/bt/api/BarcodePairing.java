package com.enioka.scanner.bt.api;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.provider.Settings;

import androidx.annotation.Nullable;


////////////////////////////////////////////////////////////////////////////////////////////////
// FEATURE: BARCODE PAIRING
////////////////////////////////////////////////////////////////////////////////////////////////


/**
 * Extra interface implemented by ring scanners that support Bluetooth barcode pairing.
 */
public interface BarcodePairing {

    /**
     * Casts the current scanner to the WithBarcodePairing interface if the feature is supported.
     * @return `this` if the feature is supported, or `null` if the feature is not supported.
     */
    default @Nullable WithBarcodePairingSupport getBarcodePairingSupport() {
        if (this instanceof WithBarcodePairingSupport) {
            return (WithBarcodePairingSupport) this;
        }
        return null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FEATURE: BARCODE PAIRING
    ////////////////////////////////////////////////////////////////////////////////////////////////

    interface WithBarcodePairingSupport {
        final int DEFAULT_WIDTH = 800;
        final int DEFAULT_HEIGHT = 160;

        public enum Defaults {
            FACTORY("F"),
            RESTORE("D");

            public final String value;

            Defaults(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }
        }

        public enum PairingType {
            NORMAL,
            LEGACY
        }

        /**
         * Get a barcode to pair the scanner with the Android device.
         */
        default Bitmap getPairingBarcode(PairingType type) {
            return null;
        }
        default Bitmap getPairingBarcode(PairingType type, int width, int height) {
            return null;
        }

        /**
         * Get a barcode to pair the scanner with the Android device.
         */
        Bitmap getPairingBarcode();
        Bitmap getPairingBarcode(int width, int height);


        /**
         * Activate bluetooth service on the scanner.
         */
        default Bitmap activateBluetoothBarcode() {
            return null;
        }
        default Bitmap activateBluetoothBarcode(int width, int height) {
            return null;
        }

        /**
         * Define bluetooth host for zebra scanner
         */
        default Bitmap zebraSetBluetoothHostBarcode() {
            return null;
        }
        default Bitmap zebraSetBluetoothHostBarcode(int width, int height) {
            return null;
        }

        /**
         * Connect to a specific mac address for zebra scanner
         */
        default Bitmap zebraConnectToAddressBarcode() {
            return null;
        }
        default Bitmap zebraConnectToAddressBarcode(int width, int height) {
            return null;
        }

        /**
         * Reset the scanner to factory / default settings
         */
        default Bitmap defaultSettingsBarcode(Defaults type) {
            return null;
        }
        default Bitmap defaultSettingsBarcode(Defaults type, int width, int height) {
            return null;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FEATURE: NFC PAIRING
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Extra interface implemented by ring scanners that support Bluetooth barcode pairing.
     */
    interface WithNfcPairingSupport {
        /**
         * Know if the NFC is enabled on the device.
         */
        default boolean isNfcEnabled(Context context) {
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);

            return nfcAdapter != null && nfcAdapter.isEnabled();
        }

        /**
         * Ask the user to enable the NFC.
         */
        default boolean askNfcActivation(Context context) {
            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
            context.startActivity(intent);

            return true;
        }
    }
}