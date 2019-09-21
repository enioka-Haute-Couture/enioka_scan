package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;

/**
 * A message or segment acknowledgment.
 */
public class AimOn extends CommandExpectingAck {

    public AimOn() {
        super((byte) 0xC5);
    }
}
