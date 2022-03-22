package com.enioka.scanner.sdk.zebraoss_2.ssi;

import android.util.Log;

import com.enioka.scanner.bt.api.MessageRejectionReason;
import com.enioka.scanner.bt.api.ParsingResult;
import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.sdk.zebraoss_2.commands.Ack;
import com.enioka.scanner.sdk.zebraoss_2.commands.MultipacketAck;
import com.enioka.scanner.sdk.zebraoss_2.commands.Nack;

import java.util.Arrays;

public class SsiParser implements ScannerDataParser {
    private static final String LOG_TAG = "SsiParser";
    private final boolean isBle;

    private SsiMultiPacket multiPacketBuffer = null;

    public SsiParser(boolean isBle) {
        this.isBle = isBle;
    }

    @Override
    public ParsingResult parse(byte[] buffer, int offset, int dataLength) {
        ParsingResult res = new ParsingResult();
        res.read = dataLength;

        if (buffer[offset + 1] == SsiCommand.MULTIPACKET_SEGMENT.getOpCode()) { // Multipacket
            if (multiPacketBuffer == null)
                multiPacketBuffer = new SsiMultiPacket();
            multiPacketBuffer.readPacket(buffer, offset, dataLength);

            res.expectingMoreData = multiPacketBuffer.expectingMoreData();
            if (!multiPacketBuffer.expectingMoreData()) {
                res.data = multiPacketBuffer.toBarcode();
                multiPacketBuffer = null;
            }

            res.acknowledger = new MultipacketAck(isBle);
            return res;
        }

        SsiMonoPacket ssiPacket;
        try {
            ssiPacket = new SsiMonoPacket(buffer[offset],
                    buffer[offset + 1],
                    buffer[offset + 3],
                    Arrays.copyOfRange(buffer, offset + 4, offset + dataLength - 6),
                    buffer[offset + dataLength - 2],
                    buffer[offset + dataLength - 1]);
        } catch (final IllegalStateException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            res.acknowledger =  new Nack(MessageRejectionReason.CHECKSUM_FAILURE, isBle);
            return res;
        }

        SsiCommand meta = SsiCommand.getCommand(ssiPacket.getOpCode());
        if (meta == SsiCommand.NONE) {
            Log.e(LOG_TAG, "Unsupported op code received: " + ssiPacket.getOpCode() + ". Message is ignored.");
            return new ParsingResult(MessageRejectionReason.INVALID_OPERATION);
        }
        if (meta.needsAck())
            res.acknowledger = new Ack(isBle);

        if (meta.getSource() == SsiSource.HOST)
            throw new IllegalStateException("Should not receive host-sent messages");
        res.data = meta.getParser().parseData(ssiPacket.getData());
        return res;
    }
}
