package com.enioka.scanner.bt;

public interface ICommand<T> {
    /**
     * The command to send on the bluetooth socket.
     *
     * @return command.
     */
    byte[] getCommand();

    /**
     * A callback to use on command completion, or null if fire and forget. This allows to give data to th caller.
     *
     * @return callback to run when command is done.
     */
    CommandCallback<T> getCallback();
}
