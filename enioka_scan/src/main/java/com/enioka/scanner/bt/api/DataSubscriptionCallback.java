package com.enioka.scanner.bt.api;

/**
 * A callback which should be called when a command has been fully interpreted.
 *
 * @param <T> the type of data expected as the result of the command.
 */
public interface DataSubscriptionCallback<T> {
    /**
     * Called when a data was returned (either after a successful command or at the scanner's initiative).
     *
     * @param data the data as parsed by the provider.
     */
    void onSuccess(T data);

    /**
     * Something went wrong with the SDK or the scanner.
     */
    void onFailure();

    /**
     * Command was sent, but an expected answer was not received before time out.
     */
    void onTimeout();
}
