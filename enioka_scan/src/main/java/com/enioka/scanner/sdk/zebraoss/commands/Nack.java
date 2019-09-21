package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.api.MessageRejectionReason;

/**
 * A message or segment explicit NON acknowledgment.
 */
public class Nack extends CommandExpectingNothing {
    public Nack(MessageRejectionReason reason) {
        super((byte) 0xD1);

        byte reasonCode;
        switch (reason) {
            case CANNOT_PARSE:
            case INVALID_OPERATION:
            case INVALID_PARAMETER:
                reasonCode = 0x2;
                break;
            case CHECKSUM_FAILURE:
                reasonCode = 0x1;
                break;
            case UNDESIRED_MESSAGE:
                reasonCode = 0x10;
                break;
            default:
                reasonCode = 0x2;
        }
        this.data[0] = reasonCode;
    }
}
