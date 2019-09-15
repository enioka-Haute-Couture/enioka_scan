package com.enioka.scanner.bt;

/**
 * A command which ca be sent to the scanner.
 *
 * @param <T> Type of data expected in return (can be Void).
 */
public interface ICommand<T> {
    /**
     * The command to send on the bluetooth socket.
     *
     * @return command.
     */
    byte[] getCommand();

    /**
     * A callback to use on command completion, or null if fire and forget. This allows to give data back to the caller and warn them of the actual end of the command.
     *
     * @return callback to run when command is done.
     */
    CommandCallbackHolder<T> getCallback();
}
