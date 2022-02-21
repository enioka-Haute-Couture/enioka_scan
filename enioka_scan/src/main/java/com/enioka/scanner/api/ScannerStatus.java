package com.enioka.scanner.api;

/**
 * The interface to implement to describe a scanner's internal status and associated callbacks.
 * Replaces {@link Scanner.ScannerStatusCallback}.
 */
public interface ScannerStatus {
    /**
     * Enum describing the scanner's lifecycle and current status.
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
        DISCONNECTED
    }

    /**
     * Gets the current status of a scanner.
     * @return The scanner's status.
     */
    Status getStatus();

    /**
     * Gets a more detailed overview of a scanner's status (may contain sdk-specific messages).
     * @return The scanner's status details.
     */
    String getStatusDetails();

    /**
     * Callback used when the scanner is waiting for a connection.
     */
    void onWaiting();

    /**
     * Callback used when the scanner is connecting.
     */
    void onConnecting();

    /**
     * Callback used when the scanner is reconnecting.
     */
    void onReconnecting();

    /**
     * Callback used when the scanner is connected.
     */
    void onConnected();

    /**
     * Callback used when the scanner is initializing.
     */
    void onInitializing();

    /**
     * Callback used when the scanner is initialized.
     */
    void onInitialized();

    /**
     * Callback used when the scanner becomes ready to scan.
     */
    void onReady();

    /**
     * Callback used when the scanner is scanning.
     */
    void onScanning();

    /**
     * Callback used when the scanner goes idle.
     */
    void onIdle();

    /**
     * Callback used when the scanner gets disabled.
     */
    void onDisabled();

    /**
     * Callback used when a critical failure happens.
     */
    void onFailure();

    /**
     * Callback used when the scanner has disconnected.
     */
    void onDisconnected();
}
