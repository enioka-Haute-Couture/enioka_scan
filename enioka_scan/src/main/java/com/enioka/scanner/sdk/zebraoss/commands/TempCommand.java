package com.enioka.scanner.sdk.zebraoss.commands;

/**
 * A message or segment acknowledgment.
 */
public class TempCommand extends CommandExpectingNothing {

    public TempCommand() {
        super((byte) 0x93, new byte[]{});
    }
}
