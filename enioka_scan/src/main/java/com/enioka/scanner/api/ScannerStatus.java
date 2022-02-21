package com.enioka.scanner.api;

import android.support.annotation.Nullable;

/**
 * FIXME 21/02/2022 - Class kept as placeholder until it's decided whether individual callbacks are to be defined in it or by the user.
 *                    Remove it if callbacks are not needed, remove Scanner.ScannerStatusCallback otherwise.
 * The interface to implement to describe a scanner's internal status and associated callbacks.
 * Replaces {@link Scanner.ScannerStatusCallback}.
 */
public interface ScannerStatus {
    /**
     * Enum describing the scanner's lifecycle and current status.
     * Some elements may not be used depending on which scanner SDK is used.
     */
    enum Status {
        /** The scanner is waiting for a connection. */
        WAITING,
        /** The scanner is in the process of connecting. */
        CONNECTING,
        /** The scanner disconnected but is trying to reconnect. */
        RECONNECTING,
        /** The scanner has finished connecting. */
        CONNECTED,
        /** The scanner is in the process of initializing. */
        INITIALIZING,
        /** The scanner has finished initializing. */
        INITIALIZED,
        /** The scanner is ready to scan and waiting to be used. */
        READY,
        /** The scanner is in the process of scanning. */
        SCANNING,
        /** The scanner is connected, initialized and enabled but not ready to scan. */
        IDLE,
        /** The scanner is connected and initialized but has been disabled and cannot be used. */
        DISABLED,
        /** The scanner is no longer available after a critical error occurred, usually during connection or initialization. */
        FAILURE,
        /** The scanner disconnected and can no longer be used. */
        DISCONNECTED,
        /** The scanner is in an unknown status. */
        UNKNOWN
    }

    /**
     * Called whenever the scanner has changed status.
     * @param scanner The updated scanner. May be null if the scanner has not yet been created.
     * @param newStatus The new status.
     * @param statusDetails The associated status details.
     */
    default void onStatusChanged(@Nullable final Scanner scanner, final Status newStatus, final String statusDetails) {
        switch (newStatus) {
            case WAITING:
                onWaiting(scanner);
                break;
            case CONNECTING:
                onConnecting(scanner);
                break;
            case RECONNECTING:
                onReconnecting(scanner);
                break;
            case CONNECTED:
                onConnected(scanner);
                break;
            case INITIALIZING:
                onInitializing(scanner);
                break;
            case INITIALIZED:
                onInitialized(scanner);
                break;
            case READY:
                onReady(scanner);
                break;
            case SCANNING:
                onScanning(scanner);
                break;
            case IDLE:
                onIdle(scanner);
                break;
            case DISABLED:
                onDisabled(scanner);
                break;
            case FAILURE:
                onFailure(scanner);
                break;
            case DISCONNECTED:
                onDisconnected(scanner);
                break;
            default:
                onUnknown(scanner);
        }
    }

    /**
     * Callback used when the scanner is waiting for a connection.
     */
    void onWaiting(final Scanner scanner);

    /**
     * Callback used when the scanner is connecting.
     */
    void onConnecting(final Scanner scanner);

    /**
     * Callback used when the scanner is reconnecting.
     */
    void onReconnecting(final Scanner scanner);

    /**
     * Callback used when the scanner is connected.
     */
    void onConnected(final Scanner scanner);

    /**
     * Callback used when the scanner is initializing.
     */
    void onInitializing(final Scanner scanner);

    /**
     * Callback used when the scanner is initialized.
     */
    void onInitialized(final Scanner scanner);

    /**
     * Callback used when the scanner becomes ready to scan.
     */
    void onReady(final Scanner scanner);

    /**
     * Callback used when the scanner is scanning.
     */
    void onScanning(final Scanner scanner);

    /**
     * Callback used when the scanner goes idle.
     */
    void onIdle(final Scanner scanner);

    /**
     * Callback used when the scanner gets disabled.
     */
    void onDisabled(final Scanner scanner);

    /**
     * Callback used when a critical failure happens.
     */
    void onFailure(final Scanner scanner);

    /**
     * Callback used when the scanner has disconnected.
     */
    void onDisconnected(final Scanner scanner);

    /**
     * Callback used when the scanner enters an unknown status.
     */
    void onUnknown(final Scanner scanner);
}
