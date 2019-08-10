package com.enioka.scanner.bt;

import java.nio.charset.Charset;

/**
 * A command which expects an answer on the wire.
 */
public abstract class BtCommandWithAnswer extends BtCommand implements BtDeviceInputHandler {
    protected String translateToString(byte[] data, int offset, int length) {
        return new String(data, offset, length, Charset.defaultCharset());
    }

    /**
     * Answers to commands may be terminated differently than barcode data. Default is to return null, which means use same temrinator as for barcode data.
     *
     * @return the terminator.
     */
    protected byte[] getAnswerTerminator() {
        return null;
    }
}
