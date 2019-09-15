package com.enioka.scanner.sdk.zebraoss;

import android.util.Log;

import com.enioka.scanner.bt.BtInputHandler;
import com.enioka.scanner.bt.BtParsingResult;
import com.enioka.scanner.bt.MessageRejectionReason;
import com.enioka.scanner.sdk.zebraoss.commands.Ack;
import com.enioka.scanner.sdk.zebraoss.commands.Nack;
import com.enioka.scanner.sdk.zebraoss.parsers.GenericParser;
import com.enioka.scanner.sdk.zebraoss.parsers.PayloadParser;

/**
 * Responsible for cutting answers from incoming flow.<br>
 * SSI answers are all in the same form as the queries.
 */
public class SsiParser implements BtInputHandler {
    private static final String LOG_TAG = "OssBtZebraSsiProvider";

    private SsiMultiPacketMessage message = new SsiMultiPacketMessage();

    public BtParsingResult process(byte[] buffer, int offset, int dataLength) {
        BtParsingResult res = new BtParsingResult();

        boolean needMoreData = message.addData(buffer, offset, dataLength);
        if (!needMoreData) {
            // Done with extracting data from messages. Now do the op-code specific parsing.
            if (message.isDataUsable()) {
                res.expectingMoreData = false;
                res.rejected = false;

                // Find the message type.
                SsiMessage messageMeta = SsiMessage.GetValue(this.message.getOpCode());

                if (messageMeta == SsiMessage.NONE) {
                    Log.e(LOG_TAG, "Unsupported op code received: " + this.message.getOpCode() + ". Message is ignored.");
                    return new BtParsingResult(MessageRejectionReason.INVALID_OPERATION);
                }

                if (messageMeta.needsAck()) {
                    res.acknowledger = new Ack();
                }

                PayloadParser parser = messageMeta.getParser();
                if (parser == null) {
                    Log.w(LOG_TAG, "Received data for opcode " + this.message.getOpCode() + " but no parser is known for this code - may be a connector limitation. Using default parser instead - this will only log.");
                    parser = new GenericParser();
                }

                // Actual parsing.
                byte[] data = this.message.getData();

                // Note that data can be null - for example with an ACK. But we still want a parsing to occur in order to have a specific payload in the result.
                res.data = parser.parseData(data);

                // Start anew.
                this.message = new SsiMultiPacketMessage();

                // Return element with data.
                return res;
            } else {
                // Not expecting more data but not usable means: wrong checksum!
                res = new BtParsingResult(MessageRejectionReason.CHECKSUM_FAILURE); //TODO: sigh.
                res.acknowledger = new Nack(MessageRejectionReason.CHECKSUM_FAILURE);
                this.message = new SsiMultiPacketMessage();
                return res;
            }
        } else {
            // If here, message is still incomplete.
            res = new BtParsingResult();
            res.acknowledger = new Ack();
            return res;
        }
    }
}
