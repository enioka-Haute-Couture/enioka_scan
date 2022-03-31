package com.enioka.scanner.sdk.zebraoss.parsers;

import com.enioka.scanner.sdk.zebraoss.data.ReplyRevision;

/**
 * Responsible for handling
 */
public class ReplyRevisionParser implements PayloadParser<ReplyRevision> {
    @Override
    public ReplyRevision parseData(final byte[] dataBuffer) {
        return new ReplyRevision(dataBuffer);
    }
}
