package com.enioka.scanner.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
        WAITING,//(Resources.getAppResources().getString(R.string.scanner_status_WAITING)),
        /** The scanner is in the process of connecting. */
        CONNECTING,//(Resources.getAppResources().getString(R.string.scanner_status_CONNECTING)),
        /** The scanner disconnected but is trying to reconnect. */
        RECONNECTING,//(Resources.getAppResources().getString(R.string.scanner_status_RECONNECTING)),
        /** The scanner has finished connecting. */
        CONNECTED,//(Resources.getAppResources().getString(R.string.scanner_status_CONNECTED)),
        /** The scanner is in the process of initializing. */
        INITIALIZING,//(Resources.getAppResources().getString(R.string.scanner_status_INITIALIZING)),
        /** The scanner has finished initializing. */
        INITIALIZED,//(Resources.getAppResources().getString(R.string.scanner_status_INITIALIZED)),
        /** The scanner is ready to scan and waiting to be used. */
        READY,//(Resources.getAppResources().getString(R.string.scanner_status_READY)),
        /** The scanner is in the process of scanning. */
        SCANNING,//(Resources.getAppResources().getString(R.string.scanner_status_SCANNING)),
        /** The scanner is connected, initialized and enabled but not ready to scan. */
        PAUSED,//(Resources.getAppResources().getString(R.string.scanner_status_PAUSED)),
        /** The scanner is connected and initialized but has been disabled and cannot be used. */
        DISABLED,//(Resources.getAppResources().getString(R.string.scanner_status_DISABLED)),
        /** The scanner is no longer available after a critical error occurred, usually during connection or initialization. */
        FAILURE,//(Resources.getAppResources().getString(R.string.scanner_status_FAILURE)),
        /** The scanner disconnected and can no longer be used. */
        DISCONNECTED,//(Resources.getAppResources().getString(R.string.scanner_status_DISCONNECTED)),
        /** The scanner is in an unknown status. */
        UNKNOWN,//(Resources.getAppResources().getString(R.string.scanner_status_UNKNOWN)),
        /** ScannerService's SDK search is over. */
        SERVICE_SDK_SEARCH_OVER,//(Resources.getAppResources().getString(R.string.scanner_status_SERVICE_SDK_SEARCH_OVER)), // FIXME - 2022/03/02: ScannerStatusCallback may not be the appropriate way to communicate Service status
        /** ScannerService's SDK search found no scanner. **/
        SERVICE_SDK_SEARCH_NOTHINGFOUND;//(Resources.getAppResources().getString(R.string.scanner_status_SERVICE_SDK_SEARCH_NOTHINGFOUND)); // FIXME - 2022/03/02: ScannerStatusCallback may not be the appropriate way to communicate Service status

        /*
        final private String message;

        Status(@NonNull final String message) {
            this.message = message;
        }
        */

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
