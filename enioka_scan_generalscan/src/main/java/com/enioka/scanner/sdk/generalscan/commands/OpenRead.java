package com.enioka.scanner.sdk.generalscan.commands;

/**
 * Signal the scanner that a command is done and normal operations can resume. Must be in its own packet, so different command.
 */
public class OpenRead extends BaseCommandNoAck {
    public OpenRead() {
        this.stringCommand = "{G1008}";
    }
}
