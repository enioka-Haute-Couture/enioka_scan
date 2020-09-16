package com.enioka.scanner.sdk.zebraoss.commands;

/**
 * Set a given configuration parameter on the scanner. Usually not used directly but with a specific command overriding it.
 */
public class ParamSend extends CommandExpectingAck {
    public ParamSend(int parameter, byte value) {
        super((byte) 0); // Useless.

        this.opCode = (byte) -58; // 0xC6...

        if (parameter > 0 && parameter <= 239) {
            this.data = new byte[]{(byte) parameter, value};
        } else if (parameter <= 495) {
            this.data = new byte[]{(byte) -16, (byte) (parameter - 256), value};
        } else if (parameter <= 751) {
            this.data = new byte[]{(byte) -15, (byte) (parameter - 512), value};
        } else if (parameter <= 1007) {
            this.data = new byte[]{(byte) -14, (byte) (parameter - 768), value};
        }

        this.updateComputedFields();
    }
}
