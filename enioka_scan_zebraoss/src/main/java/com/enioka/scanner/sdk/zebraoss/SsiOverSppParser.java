package com.enioka.scanner.sdk.zebraoss;

import com.enioka.scanner.bt.api.ParsingResult;
import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiParser;

/**
 * Wraps the handling of SSI packets contained in (possibly multiple) SPP packets.
 */
public class SsiOverSppParser implements ScannerDataParser {
    private static final SsiParser ssiParser = new SsiParser();

    private byte[] ssiBuffer = new byte[0];
    private int packetLength = 0;
    private int read = 0;

    @Override
    public ParsingResult parse(byte[] buffer, int offset, int dataLength) {
        ParsingResult res = new ParsingResult();

        if (packetLength == 0 || ssiBuffer.length == 0) { // new SSI packet
            packetLength = (char) (buffer[offset] & 0xFF);
            packetLength += 2; // include checksum
            ssiBuffer = new byte[packetLength];
        }

        System.arraycopy(buffer, offset, ssiBuffer, read, Math.min(dataLength - offset, packetLength - read));
        read += Math.min(dataLength, packetLength - read);
        res.read = read;

        if (read == packetLength) { // SSI packet is complete
            res = ssiParser.parse(ssiBuffer, 0, packetLength);
            ssiBuffer = new byte[0];
            packetLength = 0;
            read = 0;
        } else { // SSI packet is incomplete
            res.expectingMoreData = true;
        }

        return res;
    }
}
