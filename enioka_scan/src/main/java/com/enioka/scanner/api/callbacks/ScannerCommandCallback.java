package com.enioka.scanner.api.callbacks;

/**
 * Callback used to inform of the result of scanner commands, usually optional.
 */
public interface ScannerCommandCallback {
    /**
     * Called when the command was completed, and if it received a positive answer (when applicable).
     */
    void onSuccess();

    /**
     * Called when the command failed, either through or SDK error or negative answer (when applicable).
     */
    void onFailure();

    /**
     * Called when the command's expected answer did not arrive in time (usually only with BT scanners).
     */
    void onTimeout();
}
