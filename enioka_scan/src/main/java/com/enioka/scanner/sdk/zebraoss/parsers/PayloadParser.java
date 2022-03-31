package com.enioka.scanner.sdk.zebraoss.parsers;

/**
 * Parser specific to a kind of message (opcode).
 *
 * @param <T>
 */
public interface PayloadParser<T> {

    /**
     * @param dataBuffer data content of the message to parse, headers and checksum excluded.
     * @return null means no meaningful data found.
     */
    T parseData(final byte[] dataBuffer);
}
