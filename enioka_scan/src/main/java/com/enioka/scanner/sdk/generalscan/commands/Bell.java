package com.enioka.scanner.sdk.generalscan.commands;

import com.bim.cmds.SendConstant;
//import com.generalscan.SendConstant;
import com.bim.bluetooth.Manager;

public class Bell extends BaseCommandNoAck {
    public Bell() {
        this.stringCommand = "{GB100}{G2014}";

    }
}
