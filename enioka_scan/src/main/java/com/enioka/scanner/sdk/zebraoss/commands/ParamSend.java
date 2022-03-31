package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

/**
 * Set a given configuration parameter on the scanner. Usually not used directly but with a specific command overriding it.
 */
public class ParamSend extends CommandExpectingAck {
    public ParamSend(int parameter, byte value) {
        super(SsiCommand.PARAM_SEND.getOpCode(), getData(parameter, value));
    }

    private static byte[] getData(int parameter, byte value) {
        // In all cases, the param array begins with 00 which means short beep on prm change.
        // Also, we keep the flag to 0x00 (default in base class) - means prm changes are temporary.
        byte[] data = new byte[0];

        if (parameter > 0 && parameter <= 239) {
            data = new byte[]{0, (byte) parameter, value};
        } else if (parameter <= 495) {
            data = new byte[]{0, (byte) -16, (byte) (parameter - 256), value}; // 0x00 0xF0 <PRM - 256> <value> - 0x°° means short beep, F0 means from 256 to 495
        } else if (parameter <= 751) {
            data = new byte[]{0, (byte) -15, (byte) (parameter - 512), value};
        } else if (parameter <= 1007) {
            data = new byte[]{0, (byte) -14, (byte) (parameter - 768), value};
        }

        return data;
    }
}
