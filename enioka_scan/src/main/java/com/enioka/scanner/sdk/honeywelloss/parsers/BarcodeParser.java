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
    private byte[] data = new byte[200];
    private int hwm = -1;
    private int expectedBytes = 0;
    private String messageType = null;

    private String latestData = null;
    private BarcodeType latestCodification = null;

    public ParsingResult parse(byte[] buffer, int offset, int length) {
        // Structure is always:
        // HEADER
        //   0x16 (SYN)
        //   0xFE
        //   Four bytes giving payload length, little endian. (e.g. 0x1B 0x00 0x00 0x00 for 0d27)
        //   0x06 (CR) - end of header.
        // MSGGET payload (other payloads possible, this is the only one which interest us for now)
        //   Payload identifier
        //     0x4D (M)
        //     0x53 (S)
        //     0x47 (G)
        //     0x47 (G)
        //     0x45 (E)
        //     0x54 (T) // End of identifier
        //   Data length in ASCII on four bytes. For example 0x30 0x30 0x31 0x33 => 0013. Weird.
        //   Codification used in Honeywell ID. E.g. 0x6A is Code 128.
        //   Codification in AIM code. Not used here.
        //   AIM modifier. Not used here.
        //   0x1D (GROUP SEPARATOR)
        //   Data! In natural order. ASCII.
        //   0x0D (CR) - end of message.

        ParsingResult<Barcode> res = new ParsingResult<>();
        //int previousHwm = hwm;

        if (length + hwm > data.length) {
            // Buffer must be enlarged.
            byte[] data2 = new byte[Math.max(data.length + 200, length + hwm)];
            System.arraycopy(data, 0, data2, 0, data.length);
            data = data2;
        }

        // Is it a new message?
        if (length > 0 && buffer[0] == 0x16) {
            hwm = -1;
            expectedBytes = 0;
        }

        // Checks
        if (expectedBytes > 0 && expectedBytes < hwm + length) {
            throw new IllegalArgumentException("Cannot decode data - too much data");
        }

        // Add data to buffer.
        System.arraycopy(buffer, offset, data, hwm + 1, length);
        hwm += length;

        // Header analysis
        if (expectedBytes == 0 && hwm > 6) {
            if (data[0] != 0x16) {
                throw new IllegalArgumentException("Byte 0 must be SYN");
            }
            if (data[1] != -2) {
                throw new IllegalArgumentException("Byte 0 must be 0xFE");
            }
            if (data[6] != 0x0D) {
                throw new IllegalArgumentException("Byte 6 must be CR");
            }

            expectedBytes = (data[5] & 0xFF) << 24 | (data[4] & 0xFF) << 16 | (data[3] & 0xFF) << 8 | (data[2] & 0xFF);
            if (expectedBytes < 7) {
                throw new IllegalArgumentException("Payload length must be positive");
            }
            expectedBytes += 7; // take header into account.
        }

        // Payload validation
        if (hwm + 1 >= expectedBytes) {
            messageType = new String(data, 7, 6, StandardCharsets.US_ASCII);

            if ("MSGGET".equals(messageType)) {
                int dataLength = Integer.parseInt(new String(data, 13, 4, StandardCharsets.US_ASCII));
                if (data[20] != 0x1D) {
                    throw new IllegalArgumentException("Byte 20 must be GROUP SEPARATOR");
                }
                if (data[dataLength + 20] != 0x0d) {
                    throw new IllegalArgumentException("Wrong data length or bad terminator");
                }

                latestData = new String(data, 21, dataLength, StandardCharsets.US_ASCII);
                latestCodification = HoneywellOssDataTranslator.sdk2Api(data[17]);
            }

            // We only read until the end of the message. WHat remains is not part of our current message.
            //return expectedBytes - previousHwm;
            res.data = toBarcode();
            res.expectingMoreData = false;
            res.rejected = false;
            res.acknowledger = null;
            return res;
        }

        // If here we have read all the bytes and still need more.
        //return length;
        res.expectingMoreData = true;
        return res;
    }

    private boolean isComplete() {
        return expectedBytes > 0 && hwm > 6 && data[hwm] == 0x0D && "MSGGET".equals(messageType);
    }

    private Barcode toBarcode() {
        if (!isComplete()) {
            throw new IllegalStateException("Cannot translate to barcode - parser has not finished readin a valid barcode");
        }
        return new Barcode(latestData, latestCodification);
    }
}
