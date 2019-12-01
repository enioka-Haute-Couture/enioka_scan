package com.enioka.scanner.sdk.zebraoss.commands;

/**
 * Signal the device that we are a barcode scanning application host type.
 */
public class InitCommand extends CommandExpectingAck {
    public InitCommand() {
        super((byte) 0x90, new byte[]{(byte) (0), (byte) (2), (byte) (0), (byte) (5)});
    }

    @Override
    public int getTimeOut() {
        return 10000;
    }
}
