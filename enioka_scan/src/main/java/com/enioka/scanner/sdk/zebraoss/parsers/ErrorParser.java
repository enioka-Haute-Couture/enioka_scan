package com.enioka.scanner.sdk.zebraoss.parsers;

import android.util.Log;

import com.enioka.scanner.bt.api.MessageRejectionReason;

/**
 * Responsible for handling NAK data
 */
public class ErrorParser implements PayloadParser<MessageRejectionReason> {
    private static final String LOG_TAG = "ErrorParser";

    @Override
    public MessageRejectionReason parseData(byte[] buffer) {
        if (buffer.length < 1) {
            return null;
        }

        switch (buffer[0]) {
            case 1:
                return MessageRejectionReason.CHECKSUM_FAILURE;
            case 2:
                return MessageRejectionReason.INVALID_OPERATION;
            case 6:
                return MessageRejectionReason.DENIED;
            case 10:
                return MessageRejectionReason.UNDESIRED_MESSAGE;
            default:
                Log.w(LOG_TAG, "Cannot determine SDK cause for SSI cause " + buffer[0]);
                return MessageRejectionReason.UNKNOWN;
        }
    }
}
