package com.enioka.scanner.sdk.honeywelloss.parsers;

import com.enioka.scanner.bt.api.ParsingResult;
import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;

import java.nio.charset.StandardCharsets;

/**
 * Parser for one the two main data structures: data, inside a MSGGET message.
 */
class BarcodeParser implements ScannerDataParser {
    private static final String LOG_TAG = "HonOssDriver";
    private static final int HEADER_OVERHEAD = 21; // 7 bytes for the header, 14 bytes for the payload header

    private int dataLength;             // Expected bytes to read for the barcode data.
    private int dataRead;               // How much data was read over all messages.
    private byte[] data;                // Buffer for read barcode data.
    private BarcodeType barcodeType;    // Type of barcode to read.


    public ParsingResult parse(final byte[] buffer, final int offset, final int length) {
        // Structure is always:
        // HEADER
        //   0x16 (SYN)             = Start of header
        //   0xFE                   = Padding
        //   0x?? 0x?? 0x?? 0x??    = Payload length, little endian.
        //   0x0D (CR)              = End of header
        // PAYLOAD
        //   0x4D 0x53 0x47 0x47 0x45 0x54 (MSGGET) = Payload type (other payloads possible, this is the only one which interest us for now, the 'T' character marks the end of the payload type)
        //   0x?? 0x?? 0x?? 0x??                    = Data length (each byte = ASCII character of digit, read in natural order, e.g. 0x30 0x30 0x31 0x33 => 0013)
        //   0x??                                   = Honeywell ID for symbology
        //   0x?? 0x??                              = AIM ID for symbology (each byte = ASCII character, first is symbology family, second is modifier to identify the exact symbology)
        //   0x1D (GS)                              = End of payload description / data starts next
        //   Data! In natural order. ASCII.
        //   0x0D (CR)                              = End of data (included in the data length, expecting more data until found).

        int readOffset = offset;
        ParsingResult<Barcode> res = new ParsingResult<>();

        if (buffer[offset] == 0x16) { // New message: initialize parser
            // Validate headers
            if (buffer[offset + 1] != (byte) 0xFE)
                throw new IllegalArgumentException("Invalid header: byte 1 should be 0xFE");
            if (buffer[offset + 6] != 0x0D)
                throw new IllegalArgumentException("Invalid header: byte 6 should be 0x0D");
            if (!new String(buffer, offset + 7, 6, StandardCharsets.US_ASCII).equals("MSGGET"))
                throw new IllegalArgumentException("Invalid payload: type should be MSGGET");
            if (buffer[offset + 20] != 0x1D)
                throw new IllegalArgumentException("Invalid payload: byte 20 should be 0x1D");
            // Payload length is always dataLength + 14, no point in verifying
            // Cannot verify that the last byte is 0x0D yet as it may be in another message

            // Get payload information
            dataLength = 1000 * (buffer[offset + 13] - 0x30) + 100 * (buffer[offset + 14] - 0x30) + 10 * (buffer[offset + 15] - 0x30) + (buffer[offset + 16] - 0x30);
            barcodeType = HoneywellOssDataTranslator.sdk2Api(buffer[offset + 17]);

            // Prepare data buffer
            data = new byte[dataLength];
            dataRead = 0;
            readOffset += HEADER_OVERHEAD;
        }

        // Read data
        int toRead = Math.min(length - readOffset, dataLength - dataRead);
        System.arraycopy(buffer, readOffset, data, dataRead, toRead);
        dataRead += toRead;

        // Setup result information
        res.read += length; // We consume the entire buffer no matter what
        res.expectingMoreData = dataRead < dataLength;
        res.rejected = false;
        res.acknowledger = null;

        if (!res.expectingMoreData && !(dataRead == dataLength && data[dataLength - 1] == 0x0D))
            throw new IllegalStateException("Invalid payload: last byte should be 0x0D");
        if (!res.expectingMoreData)
            res.data = new Barcode(new String(data, 0, dataLength, StandardCharsets.US_ASCII), barcodeType);

        return res;
    }
}
