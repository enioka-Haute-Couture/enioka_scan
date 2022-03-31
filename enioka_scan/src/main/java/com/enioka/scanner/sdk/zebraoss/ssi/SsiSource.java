package com.enioka.scanner.sdk.zebraoss.ssi;

import android.support.annotation.Nullable;

/**
 * Originator of a message, used both for SsiPackets (source-byte) or config (expected source-byte).
 */
public enum SsiSource {
    /** Packet: message is coming from the scanner. Checks: only the scanner can send this command. */
    DECODER((byte) 0x00),
    /** Packet: message is coming from the client. Checks: only the client can send this command. */
    HOST((byte) 0x04),
    /** Packet: invalid value. Checks: both the scanner and client can send this command. */
    BOTH((byte) 0xFF);

    private final byte id;

    SsiSource(final byte id) {
        this.id = id;
    }

    /**
     * Converts an SsiSource element to its byte value.
     * @return The byte value of a source.
     */
    public byte toByte() {
        if (this.equals(BOTH))
            throw new IllegalStateException("`BOTH` source value does not have a byte equivalent");
        return id;
    }

    /**
     * Converts a source byte to its SsiSource value.
     * @param id The source byte of an SSI packet.
     * @return The corresponding SsiSource value, or `null` if the byte does not match any source.
     */
    public static @Nullable SsiSource fromByte(final byte id) {
        switch (id) {
            case 0x00:
                return DECODER;
            case 0x04:
                return HOST;
            default:
                return null;
        }
    }

    /**
     * Checks if a packet's source byte matches the expected value from the config.
     * @param sourceByte The byte to check.
     * @param config The config (expected value, can be `BOTH`).
     * @return Whether the byte is valid or not.
     */
    public static boolean checkPacketMatchesConfig(final byte sourceByte, final SsiSource config) {
        if (sourceByte == 0x00)
            return config.equals(DECODER) || config.equals(BOTH);
        if (sourceByte == 0x04)
            return config.equals(HOST) || config.equals(BOTH);
        throw new IllegalArgumentException("Source byte does not match any source");
    }
}
