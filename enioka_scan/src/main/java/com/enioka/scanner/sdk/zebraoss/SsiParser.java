package com.enioka.scanner.sdk.zebraoss;

import android.util.Log;

import com.enioka.scanner.bt.BtInputHandler;
import com.enioka.scanner.bt.BtParsingResult;
import com.enioka.scanner.bt.MessageRejectionReason;
import com.enioka.scanner.sdk.zebraoss.parsers.PayloadParser;

/**
 * Responsible for cutting answers from incoming flow.<br>
 * SSI answers are all in the same form as the queries.
 */
public class SsiParser implements BtInputHandler {
    private static final String LOG_TAG = "OssBtZebraSsiProvider";

    private SsiMultiPacketMessage message = new SsiMultiPacketMessage();

    public BtParsingResult process(byte[] buffer, int offset, int dataLength) {
        if (!message.addData(buffer, offset, dataLength)) {
            // Done with extracting data from messages. Now do the op-code specific parsing.
            if (message.isDataUsable()) {
                // Find the message type.
                SsiMessage command = SsiMessage.GetValue(message.getOpCode());

                if (command == SsiMessage.NONE) {
                    Log.e(LOG_TAG, "Unsupported op code received: " + message.getOpCode() + ". Message is ignored.");
                    return new BtParsingResult(MessageRejectionReason.INVALID_OPERATION);
                }

                // Actual parsing.
                PayloadParser parser = command.getParser();
                byte[] data = message.getData();
                if (parser != null && data.length > 0) {
                    parser.parseData(data);

                    BtParsingResult res = new BtParsingResult<>(parser.parseData(data));
                    res.acknowledger = command.getAcknowledger();
                    this.message = new SsiMultiPacketMessage();
                    return res;
                } else if (parser == null && data.length > 0) {
                    Log.w(LOG_TAG, "Received data for opcode " + message.getOpCode() + " but no parser is known for this code - may be a connector limitation. Data is ignored.");
                }
                this.message = new SsiMultiPacketMessage();
                return new BtParsingResult((byte[]) null);
            } else {
                return new BtParsingResult(MessageRejectionReason.CHECKSUM_FAILURE);
            }
        } else {
            // If here, message is still incomplete.
            return new BtParsingResult();
        }
    }
}
