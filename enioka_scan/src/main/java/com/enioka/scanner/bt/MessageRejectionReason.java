package com.enioka.scanner.bt;

public enum MessageRejectionReason {
    CHECKSUM_FAILURE,
    UNDESIRED_MESSAGE,
    INVALID_PARAMETER,
    INVALID_OPERATION,
    CANNOT_PARSE,
    DENIED,
    UNKWOWN;
}
