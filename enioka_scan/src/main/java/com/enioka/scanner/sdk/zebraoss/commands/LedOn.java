package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.api.Color;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

public class LedOn extends CommandExpectingAck {
    public LedOn(Color color) {
        super(SsiCommand.LED_ON.getOpCode(), new byte[]{getColorMask(color)});
    }

    private static byte getColorMask(Color color) {
        byte colorMask = 0b00000000;
        switch (color) {
            case GREEN:
            default:
                colorMask |= 0b00000001;
                break;
            case RED:
                colorMask |= 0b00000010;
                break;
        }
        return colorMask;
    }
}
