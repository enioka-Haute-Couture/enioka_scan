package com.enioka.scanner.bt;

/**
 * A callback which should be called when a command has been fully interpreted.
 *
 * @param <T> the type of data expected as the result of the command. Often Void for simple commands.
 */
public interface CommandCallback<T> {
    void onSuccess(T data);

    //void onFailure();
}
