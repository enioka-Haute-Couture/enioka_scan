package com.enioka.scanner.sdk.zebraoss;

/**
 * The originator of a message.
 */
enum SsiSource {
    /**
     * Message is coming from the scanner, going toward the host?
     **/
    DECODER,
    /**
     * Message is coming from the host, going toward the scanner.
     */
    HOST,
    /**
     * Used in configuration only - a message which can com from both side.
     */
    BOTH
}
