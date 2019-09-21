package com.enioka.scanner.bt.api;

/**
 * Represents a connected SPP device, and can be used to run commands on it.
 */
public interface Scanner {
    /**
     * Run a command on the scanner. Asynchronous - this call returns before the command is actually sent to the scanner.<br>
     * If the command expects an answer, it will be received as any data from the scanner and sent to the registered {@link ScannerDataParser}
     * (there is no direct "link" between command and response).
     *
     * @param command      what should be run
     * @param subscription an optional subscription waiting for an asnwer to the command
     * @param <T>          expected return type of the command (implicit, found from command argument)
     */
    <T> void runCommand(Command<T> command, DataSubscriptionCallback<T> subscription);

    /**
     * @return the BT name
     */
    String getName();
}
