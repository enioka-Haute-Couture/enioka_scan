package com.enioka.scanner.sdk.zebraoss_2.ssi;

/**
 * Helper class for the status-byte of an SSI packet.
 * These values are purely deduced from observations and do not match the 2015 documentation.
 */
public enum SsiStatus {
    /** Default value, the scanner does not seem to care about status so it may be suitable for all commands. */
    DEFAULT((byte) 0x00),
    /** Value present in all messages in direct response to a previous message in the opposite direction. */
    RESPONSE((byte) 0x80),
    /** Value present in all mono-packet DECODE_DATA messages. */
    NOTIFICATION((byte) 0x20),
    /** Value present as the third byte in every first packet of a multi-packet DECODE_DATA message. */
    MULTIPACKET_FIRST((byte) 0x02);

    private final byte id;

    SsiStatus(final byte id) {
        this.id = id;
    }

    public byte getByte() {
        return id;
    }
}
