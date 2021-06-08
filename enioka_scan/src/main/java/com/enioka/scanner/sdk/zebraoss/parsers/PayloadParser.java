package com.enioka.scanner.sdk.zebraoss.parsers;

import com.enioka.scanner.sdk.zebraoss.SsiMultiPacketMessage;

/**
 * Parser specific to a kind of message (opcode).
 *
 * @param <T>
 */
public interface PayloadParser<T> {

    /**
     * @param message message to parse. May be multi-packet.
     * @return null means no meaningful data found.
     */
    T parseData(SsiMultiPacketMessage message);
}
