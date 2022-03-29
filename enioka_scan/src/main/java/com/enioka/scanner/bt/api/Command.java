package com.enioka.scanner.bt.api;

/**
 * A command which can be sent to the scanner.
 *
 * @param <T> Type of data expected in return (can be Void).
 */
public interface Command<T> {
    /**
     * The command to send on the bluetooth socket.
     *
     * @return command.
     */
    byte[] getCommand();

    /**
     * The command to send on the bluetooth socket.
     *
     * @param scanner The scanner that may be used to adapt some parameters in the command generation.
     * @return command.
     */
    default byte[] getCommand(final Scanner scanner) {
        return getCommand();
    }

    /**
     * The expected return type of the command (as in: the class returned by the parser when parsing an incoming stream from the scanner). Can be null.<br>
     * Only useful because of Java generic type erasure - otherwise, we know it's the generic parameter T!
     *
     * @return a type deriving from T
     */
    Class<? extends T> getReturnType();

    /**
     * Every command may have a different timeout waiting for an answer. 0 means no timeout. Ignored when the command expects no answer (T is Void).
     *
     * @return time out in milliseconds.
     */
    int getTimeOut();
}
