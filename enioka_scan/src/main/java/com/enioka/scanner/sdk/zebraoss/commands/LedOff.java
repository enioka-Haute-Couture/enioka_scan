package com.enioka.scanner.sdk.zebraoss.commands;

public class LedOff extends CommandExpectingAck {
    public LedOff() {
        super((byte) 0xE8, new byte[]{(byte) 0xFF});
    }
}
