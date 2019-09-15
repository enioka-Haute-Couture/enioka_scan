package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.CommandCallbackHolder;
import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.bt.MessageRejectionReason;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;

/**
 * A message or segment explicit NON acknowledgment.
 */
public class Nack extends SsiPacket implements ICommand<Void> {
    public Nack(MessageRejectionReason reason) {
        super((byte) 0xD1, new byte[1]);

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

    @Override
    public byte[] getCommand() {
        return this.getMessageData();
    }

    @Override
    public CommandCallbackHolder<Void> getCallback() {
        return null;
    }
}
