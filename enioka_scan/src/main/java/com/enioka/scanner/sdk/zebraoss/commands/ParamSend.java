package com.enioka.scanner.sdk.zebraoss.commands;

/**
 * Set a given configuration parameter on the scanner. Usually not used directly but with a specific command overriding it.
 */
public class ParamSend extends CommandExpectingAck {
    public ParamSend(int parameter, byte value) {
        super((byte) 0); // Useless.

        this.opCode = (byte) -58; // 0xC6...

        // In all cases, the param array begins with 00 which means short beep on prm change.
        // Also, we keep the flag to 0x00 (default in base class) - means prm changes are temporary.

        if (parameter > 0 && parameter <= 239) {
            this.data = new byte[]{0, (byte) parameter, value};
        } else if (parameter <= 495) {
            this.data = new byte[]{0, (byte) -16, (byte) (parameter - 256), value}; // 0x00 0xF0 <PRM - 256> <value> - 0x°° means short beep, F0 means from 256 to 495
        } else if (parameter <= 751) {
            this.data = new byte[]{0, (byte) -15, (byte) (parameter - 512), value};
        } else if (parameter <= 1007) {
            this.data = new byte[]{0, (byte) -14, (byte) (parameter - 768), value};
        }

        this.updateComputedFields();
    }
}
