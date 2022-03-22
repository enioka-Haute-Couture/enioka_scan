package com.enioka.scanner.sdk.zebraoss;

import com.enioka.scanner.bt.api.ParsingResult;
import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiParser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Wraps the handling of SSI packets contained in ATT packets.
 *
 * Unlike with SPP where the entire payload is the entirerity of an SSI packet, BLE works with
 * much smaller payloads meaning a single SSI packet will usually be split over multiple ATT packets.
 * Only the first ATT packet of the batch will contain the information necessary to correctly concatenate
 * the SSI packet: the total size of the SSI packet.
 */
public class SsiOverAttParser implements ScannerDataParser {
    private static final SsiParser ssiParser = new SsiParser(true);

    private byte[] ssiBuffer = new byte[0];
    private short packetLength = 0;
    private short read = 0;

    @Override
    public ParsingResult parse(byte[] buffer, int offset, int dataLength) {
        ParsingResult res = new ParsingResult();
        res.read = dataLength;

        if (packetLength == 0 || ssiBuffer.length == 0) { // new SSI packet
            packetLength = ByteBuffer.wrap(new byte[]{buffer[offset], buffer[offset + 1]}).order(ByteOrder.LITTLE_ENDIAN).getShort();
            packetLength -= 2;
            ssiBuffer = new byte[packetLength - 2];
            offset += 2;
            dataLength -= 2;
        }

        System.arraycopy(buffer, offset, ssiBuffer, read, Math.min(dataLength, packetLength - read));
        read += Math.min(dataLength, packetLength - read);

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
