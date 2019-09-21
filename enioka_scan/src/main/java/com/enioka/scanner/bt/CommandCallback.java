package com.enioka.scanner.bt;

/**
 * A callback which should be called when a command has been fully interpreted.
 *
 * @param <T> the type of data expected as the result of the command. Often Void for simple commands.
 */
public interface CommandCallback<T> {
    /**
     * Called when a command has successfully ended and data was returned.
     *
     * @param data
     */
    void onSuccess(T data);

    /**
     * Something went wrong with the command.
     */
    void onFailure();

    /**
     * Command was sent, but an expected answer was not received before time out.
     */
    void onTimeout();
}
