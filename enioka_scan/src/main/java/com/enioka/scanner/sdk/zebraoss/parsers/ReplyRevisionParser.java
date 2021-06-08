package com.enioka.scanner.sdk.zebraoss.parsers;

import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.sdk.zebraoss.SsiMultiPacketMessage;
import com.enioka.scanner.sdk.zebraoss.ZebraOssDataTranslator;
import com.enioka.scanner.sdk.zebraoss.data.ReplyRevision;

import java.nio.charset.Charset;

/**
 * Responsible for handling
 */
public class ReplyRevisionParser implements PayloadParser<ReplyRevision> {
    @Override
    public ReplyRevision parseData(SsiMultiPacketMessage message) {
        return new ReplyRevision(message.getData());
    }
}
