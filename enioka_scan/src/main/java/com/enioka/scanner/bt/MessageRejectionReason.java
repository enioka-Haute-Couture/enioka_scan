package com.enioka.scanner.bt;

/**
 * Why a message cannot be processed. (normalization attempt).
 */
public enum MessageRejectionReason {
    CHECKSUM_FAILURE,
    UNDESIRED_MESSAGE,
    INVALID_PARAMETER,
    INVALID_OPERATION,
    CANNOT_PARSE,
    DENIED,
    OTHER,
    UNKNOWN
}
