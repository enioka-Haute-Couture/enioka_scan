package com.enioka.scanner.sdk.zebra;

/**
 * A helper to interact between main thread and connection thread.
 */
public interface BtZebraConnectionCallback {
    void onSuccess();

    void onFailure();
}
