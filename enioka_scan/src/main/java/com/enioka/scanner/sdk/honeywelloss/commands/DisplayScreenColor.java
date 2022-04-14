package com.enioka.scanner.sdk.honeywelloss.commands;

import com.enioka.scanner.api.ScannerLedColor;
import com.enioka.scanner.bt.api.Command;

public class DisplayScreenColor implements Command<Void> {
    private byte b1, b2;

    public DisplayScreenColor(ScannerLedColor color) {
        if (color == null) {
            b1 = 48;
            b2 = 48;
            return;
        }

        switch (color) {
            case GREEN:
                b1 = 49;
                b2 = 52;
                break;
            case RED:
                b1 = 49;
                b2 = 51;
                break;
            default:
                b1 = 0;
                b2 = 0;
                break;
        }
    }

    @Override
    public byte[] getCommand() {
        // GUITSTnn
        // 13 = red
        // 14 = green
        // SYN M CR ... 13/14 !
        return new byte[]{22, 77, 13, 71, 85, 73, 84, 83, 84, b1, b2, 33};
    }

    @Override
    public Class<? extends Void> getReturnType() {
        return null;
    }

    @Override
    public int getTimeOut() {
        return 1000;
    }
}
