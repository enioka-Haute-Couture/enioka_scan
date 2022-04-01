package com.enioka.scanner.api.callbacks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.enioka.scanner.R;
import com.enioka.scanner.api.Scanner;

/**
 * Callback to deal with scanner status changes.
 */
public interface ScannerStatusCallback {
    /**
     * Enum describing the scanner's lifecycle and current status.
     * Some elements may not be used depending on which scanner SDK is used.
     * FIXME - 2022/03/02: Cannot get the resource reading to work, enum instantiation fails. Seems incompatible with unit tests.
     */
    enum Status {
        /** The scanner is waiting for a connection. */
        WAITING(R.string.scanner_status_WAITING),
        /** The scanner is in the process of connecting. */
        CONNECTING(R.string.scanner_status_CONNECTING),
        /** The scanner disconnected but is trying to reconnect. */
        RECONNECTING(R.string.scanner_status_RECONNECTING),
        /** The scanner has finished connecting. */
        CONNECTED(R.string.scanner_status_CONNECTED),
        /** The scanner is in the process of initializing. */
        INITIALIZING(R.string.scanner_status_INITIALIZING),
        /** The scanner has finished initializing. */
        INITIALIZED(R.string.scanner_status_INITIALIZED),
        /** The scanner is ready to scan and waiting to be used. */
        READY(R.string.scanner_status_READY),
        /** The scanner is in the process of scanning. */
        SCANNING(R.string.scanner_status_SCANNING),
        /** The scanner is connected, initialized and enabled but not ready to scan. */
        PAUSED(R.string.scanner_status_PAUSED),
        /** The scanner is connected and initialized but has been disabled and cannot be used. */
        DISABLED(R.string.scanner_status_DISABLED),
        /** The scanner is no longer available after a critical error occurred, usually during connection or initialization. */
        FAILURE(R.string.scanner_status_FAILURE),
        /** The scanner disconnected and can no longer be used. */
        DISCONNECTED(R.string.scanner_status_DISCONNECTED),
        /** The scanner is in an unknown status. */
        UNKNOWN(R.string.scanner_status_UNKNOWN),
        /** ScannerService's SDK search is over. */
        SERVICE_SDK_SEARCH_OVER(R.string.scanner_status_SERVICE_SDK_SEARCH_OVER), // TODO - 2022/03/02: ScannerStatusCallback may not be the appropriate way to communicate Service status
        /** ScannerService's SDK search found no scanner. **/
        SERVICE_SDK_SEARCH_NOCOMPATIBLE(R.string.scanner_status_SERVICE_SDK_SEARCH_NOCOMPATIBLE); // TODO - 2022/03/02: ScannerStatusCallback may not be the appropriate way to communicate Service status

        final private int resId;

        Status(final int resId) {
            this.resId = resId;
        }

        /**
         * Retrieves a localized message explaining the status.
         * @param context The application context, required to retrieve the message.
         * @return The localized message.
         */
        public String getLocalizedMessage(Context context) {
            try {
                return context.getString(resId);
            } catch (final Exception exception) {
                return "No details available";
            }
        }

        @Override
        @NonNull
        public String toString() {
            return name();
        }
    }

    /**
     * Called whenever the scanner has changed status.
     * @param scanner The updated scanner. May be null if the scanner has not yet been created.
     * @param newStatus The new status.
     */
    void onStatusChanged(@Nullable final Scanner scanner, final Status newStatus);
}
