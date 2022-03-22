package com.enioka.scanner.sdk.zebraoss.commands;

/**
 * Set picklist.
 */
public class SetPickListMode extends ParamSend {
    public SetPickListMode(byte mode, boolean isBle) {
        super(402, mode, isBle);
        if (mode < 0 || mode > 2) {
            throw new IllegalArgumentException("picklist mode can only be 0/1/2");
        }
    }
}
