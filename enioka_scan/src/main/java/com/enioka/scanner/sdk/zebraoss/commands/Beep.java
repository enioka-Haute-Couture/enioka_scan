package com.enioka.scanner.sdk.zebraoss.commands;

public class Beep extends CommandExpectingAck {
    public Beep(byte beepCode) {
        super((byte) 0xE6, new byte[]{beepCode});
    }
}
