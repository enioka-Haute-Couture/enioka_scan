package com.enioka.scanner.bt;

public abstract class BtCommand {
    protected static final String LOG_TAG = "BtCommand";

    /**
     * The command to send on the bluetooth socket.
     *
     * @return command, to be interpreted as ASCII text.
     */
    public abstract String getCommand();
}
