package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;

/**
 * Signal the device that we are a barcode scanning application host type.
 */
public class InitCommand extends CommandExpectingAck {
    public InitCommand(boolean isBle) {
        super(SsiCommand.SCANNER_INIT_COMMAND.getOpCode(), new byte[]{0x00, 0x02, 0x00, 0x05}, isBle);
    }

    @Override
    public int getTimeOut() {
        return 10000;
    }
}
