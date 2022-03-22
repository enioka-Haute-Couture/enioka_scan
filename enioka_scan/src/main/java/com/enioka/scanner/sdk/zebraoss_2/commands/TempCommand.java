package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;

public class TempCommand extends CommandExpectingNothing {
    public TempCommand() {
        super(SsiCommand.TEMP_COMMAND.getOpCode());
    }
}
