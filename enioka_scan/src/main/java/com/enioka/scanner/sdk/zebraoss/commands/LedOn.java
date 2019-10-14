package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.api.Color;

public class LedOn extends CommandExpectingAck {
    public LedOn(Color color) {
        super((byte) 0xE7, new byte[]{getColorMask(color)});
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
