package com.enioka.scanner.sdk.zebraoss.acknowledgers;

import com.enioka.scanner.bt.Acknowledger;
import com.enioka.scanner.bt.MessageRejectionReason;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;

/**
 * Answers either ACK or NACK depending on the parsing result. Thread safe.
 */
public class AckNack implements Acknowledger {
    private static SsiPacket ack;
    public static AckNack instance = new AckNack();

    static {
        ack = new SsiPacket();
        ack.setOpCode((byte) 0xD0);
        ack.updateComputedFields();
    }

    private AckNack() {
    }

    @Override
    public byte[] getOkCommand() {
        return ack.getMessageData();
    }

    @Override
    public byte[] getKoCommand(MessageRejectionReason reason) {
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
        SsiPacket nack = new SsiPacket();
        nack.setOpCode((byte) 0xD1);
        nack.setMessageData(new byte[]{
                reasonCode
        });
        nack.updateComputedFields();

        return nack.getMessageData();
    }
}
