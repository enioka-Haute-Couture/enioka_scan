package com.enioka.scanner.sdk.generalscan.commands;

/**
 * Signal the scanner that a command is coming. Must be in its own packet, so different command.
 */
public class CloseRead extends BaseCommandNoAck {
    public CloseRead() {
        this.stringCommand = "{G1009}";
    }
}
