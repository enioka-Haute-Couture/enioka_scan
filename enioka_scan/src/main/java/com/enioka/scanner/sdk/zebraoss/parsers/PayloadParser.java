package com.enioka.scanner.sdk.zebraoss.parsers;

/**
 * Parser specific to a kind of message (opcode).
 *
 * @param <T>
 */
public interface PayloadParser<T> {

    /**
     * @param buffer
     * @return null means no meaningful data found.
     */
    T parseData(byte[] buffer);
}
