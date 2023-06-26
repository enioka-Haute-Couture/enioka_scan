package com.enioka.scanner.api;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

/**
 * Methods to implement to be able to provide a Scanner instance.
 */
public interface ScannerProvider {
    /**
     * Return a new Scanner if the device is compatible with the type of scanners handled by this provider. Null otherwise.<br>
     * Must be callable from any device, even if not compatible - so beware of library loading (you may want to use reflection inside this method).
     */
    void getScanner(Context ctx, ProviderCallback cb, ScannerSearchOptions options);

    /**
     * The unique key which identifies this provider.
     *
     * @return the key
     */
    String getKey();

    interface ProviderCallback {
        /**
         * Called when the provider has finished creating a scanner
         *
         * @param providerKey a unique key identifying the provider.
         * @param scannerKey  a unique key identifying the connected scanner. Never empty not null.
         * @param s           the new scanner. Cannot be null.
         */
        void onScannerCreated(String providerKey, String scannerKey, Scanner s);

        /**
         * Send a localized status message to the end user.
         *
         * @param providerKey a unique key identifying the provider.
         * @param scannerKey  a unique key identifying the scanner being connected. Can be null if the message is about the provider as a whole and not a specific scanner.
         * @param message     the message (localized)
         */
        void connectionProgress(String providerKey, String scannerKey, String message);

        /**
         * Called when the provider has determined it cannot run (is not available) on this device and should not be revived.
         *
         * @param providerKey a unique key identifying the provider.
         */
        void onProviderUnavailable(String providerKey);

        /**
         * Should be called if the provider can run, and all scanners have already been created.
         *
         * @param providerKey a unique key identifying the provider.
         */
        void onAllScannersCreated(String providerKey);

        /**
         * Check if a bluetooth device was already taken by a previous provider.
         *
         * @param device the device to test
         * @return true if it should be ignored.
         */
        boolean isAlreadyConnected(BluetoothDevice device);
    }
}
