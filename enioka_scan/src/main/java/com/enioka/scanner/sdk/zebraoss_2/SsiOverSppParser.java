package com.enioka.scanner.sdk.zebraoss_2;

import android.util.Log;

import com.enioka.scanner.bt.api.MessageRejectionReason;
import com.enioka.scanner.bt.api.ParsingResult;
import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.sdk.zebraoss_2.commons.SsiCommand;
import com.enioka.scanner.sdk.zebraoss_2.commons.SsiMonoPacket;
import com.enioka.scanner.sdk.zebraoss_2.commons.SsiMultiPacket;

import java.util.Arrays;

public class SsiOverSppParser implements ScannerDataParser {
    private static final String LOG_TAG = "SsiOverSppParser";
    private SsiMultiPacket multiPacketBuffer = null;

    @Override
    public ParsingResult parse(byte[] buffer, int offset, int dataLength) {
        ParsingResult res = new ParsingResult();
        res.read = dataLength;

        if (buffer[offset + 1] == 0x73) { // Multipacket
            if (multiPacketBuffer == null)
                multiPacketBuffer = new SsiMultiPacket();
            multiPacketBuffer.readPacket(buffer, offset, dataLength);

            res.expectingMoreData = multiPacketBuffer.expectingMoreData();
            if (!multiPacketBuffer.expectingMoreData()) {
                res.data = multiPacketBuffer.toBarcode();
                multiPacketBuffer = null;
            }

            // FIXME: needs ACK response, maybe a different opcode (0x74 instead of 0xD0)
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
            res.acknowledger = null; // FIXME: needs NACK response
            return res;
        }

        SsiCommand meta = SsiCommand.getCommand(ssiPacket.getOpCode());
        if (meta == SsiCommand.NONE) {
            Log.e(LOG_TAG, "Unsupported op code received: " + ssiPacket.getOpCode() + ". Message is ignored.");
            return new ParsingResult(MessageRejectionReason.INVALID_OPERATION);
        }
        if (meta.needsAck())
            res.acknowledger = null; // FIXME: needs ACK response

        res.data = meta.getParser();//.parseData(ssiPacket); FIXME
        return res;
    }
}
