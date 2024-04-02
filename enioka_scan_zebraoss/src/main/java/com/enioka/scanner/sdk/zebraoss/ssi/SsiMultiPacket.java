package com.enioka.scanner.sdk.zebraoss.ssi;

import android.util.Log;

import com.enioka.scanner.data.Barcode;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * SSI multi-packet, containing the header and checksum bytes. Suitable for both incoming.
 * So far, only DECODE_DATA packets seem to sometimes use multi-packets for large barcodes, so this
 * class is only used as a buffer for reading those types of packets before being converted to a
 * barcode with no other use. For this reason, many sanity checks are skipped.
 */
public final class SsiMultiPacket {
    private static final String LOG_TAG = "SsiParser";

    private byte symbology;
    private int barcodeLength = 0;
    private byte[] data;
    private int dataRead = 0;
    private boolean expectingMoreData;

    /**
     * Initializes the MultiPacket.
     */
    public SsiMultiPacket() {
        this.data = new byte[0];
        this.expectingMoreData = true;
    }

    /**
     * Reads a packet that is part of a multi-packet batch.
     * @param buffer The buffer containing the raw packet data, including headers and checksum
     * @param offset The offset from which packet data starts. In case of BLE, the offset must skip the first two bytes of the packet.
     * @param length The length of the packet data, including headers and checksum
     */
    public void readPacket(final byte[] buffer, int offset, int length) {
        if (!expectingMoreData) {
            throw new IllegalStateException("Not expecting more data");
        }

        if (buffer[offset + 1] != SsiCommand.MULTIPACKET_SEGMENT.getOpCode()) {
            throw new IllegalStateException("Packet not part of a multipacket batch");
        }

        Log.d(LOG_TAG, "Parsing packet #" + buffer[offset + 4] + " of a multi-packet batch");

        if (data.length == 0) { // first packet
            if (buffer[offset] != (byte) 0xFF)
                throw new IllegalStateException("First packet of a multipacket batch is not full, should be a monopacket");
            if (buffer[offset + 2] != SsiStatus.MULTIPACKET_FIRST.getByte() || buffer[offset + 4] != 0x00)
                throw new IllegalStateException("Expected the first packet of a multipacket batch, got a continuation");
            if (buffer[offset + 5] != SsiCommand.DECODE_DATA.getOpCode())
                throw new IllegalStateException("Multipacket content is not a barcode scan, the SDK does not know how to process this data");

            symbology = buffer[offset + 10];
            barcodeLength = ByteBuffer.wrap(new byte[]{buffer[offset + 12], buffer[offset + 13]}).order(ByteOrder.BIG_ENDIAN).getShort();
            data = new byte[barcodeLength];

            offset += 14;
            length -= 14;
        } else {
            if (buffer[offset] != (byte) 0xFF)
                expectingMoreData = false;
            offset += 5;
            length -= 5;
        }

        System.arraycopy(buffer, offset, data, dataRead, Math.min(length - 2, barcodeLength - dataRead));
        dataRead += Math.min(length - 2, barcodeLength - dataRead);
        if (dataRead == barcodeLength) {
            expectingMoreData = false;
        }
    }

    public boolean expectingMoreData() {
        return expectingMoreData;
    }

    public Barcode toBarcode() {
        return new Barcode(new String(data, StandardCharsets.US_ASCII), SsiSymbologyTranslator.sdk2Api(symbology));
    }
}
