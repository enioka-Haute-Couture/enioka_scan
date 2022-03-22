package com.enioka.scanner.sdk.zebraoss_2.commands;

/**
 * Set picklist.
 */
public class SetPickListMode extends ParamSend {
    public SetPickListMode(byte mode) {
        super(402, mode);
        if (mode < 0 || mode > 2) {
            throw new IllegalArgumentException("picklist mode can only be 0/1/2");
        }
    }
}
