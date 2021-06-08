package com.enioka.scanner.sdk.zebraoss;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * A parser for SSI messages.
 */
public class SsiMultiPacketMessage {
    private static final String LOG_TAG = "SsiMultiPacketMessage";

    private List<SsiPacket> packets = new ArrayList<>(1);
    private boolean fullyLoaded = false;
    private SsiPacket currentPacket;

    public static class PacketParsingResult {
        public boolean incompleteParsing = true;
        public int read = 0;

        public PacketParsingResult(boolean incompleteParsing, int read) {
            this.incompleteParsing = incompleteParsing;
            this.read = read;
        }
    }

    /**
     * @param buffer data from scanner stream
     * @param offset read buffer from this position only. 0-based.
     * @param length read buffer until from + length
     * @return true if another buffer is expected (buffer incomplete or multi part message).
     */
    public PacketParsingResult addData(byte[] buffer, int offset, int length) {
        if (length == 0) {
            return new PacketParsingResult(true, 0);
        }
        if (fullyLoaded) {
            throw new IllegalStateException("message was already fully read - cannot add data");
        }

        if (this.currentPacket == null) {
            // New packet!
            this.currentPacket = new SsiPacket();
        }

        PacketParsingResult bufferPacketAdditionResult = currentPacket.addData(buffer, offset, length);
        if (!bufferPacketAdditionResult.incompleteParsing) {
            Log.d(LOG_TAG, "Read a full SSI packet (as many bytes as announced) " + currentPacket.data.length);

            if (!this.currentPacket.isChecksumValid()) {
                Log.e(LOG_TAG, "Received a message with an invalid checksum - ignored");
                fullyLoaded = false;
                return new PacketParsingResult(false, bufferPacketAdditionResult.read); // We do not expect another packet. Note fullyLoaded stays false.
            }
            this.packets.add(this.currentPacket); // Only valid packets are added to the list.

            if (this.currentPacket.isRetransmit()) {
                Log.w(LOG_TAG, "Received a retransmitted message (just for information)");
            }

            if (this.currentPacket.isMultiPacket()) {
                Log.w(LOG_TAG, "Received an intermediary message - awaiting next one.");
                this.currentPacket = null;
                fullyLoaded = false;
                return new PacketParsingResult(true, bufferPacketAdditionResult.read);
            }

            if (this.currentPacket.isLastPacket()) {
                Log.d(LOG_TAG, "Fully received a valid SSI multi part message. Packet count: " + this.packets.size());
                fullyLoaded = true;
                return new PacketParsingResult(false, bufferPacketAdditionResult.read);
            }
        }

        // Incomplete parsing - the current packet needs more data.
        return new PacketParsingResult(true, bufferPacketAdditionResult.read);
    }

    private void checkCanUse() {
        if (!fullyLoaded) {
            throw new IllegalStateException("message is not fully read from device input buffer");
        }
    }

    public byte[] getData() {
        this.checkCanUse();

        int dataLength = 0;
        for (SsiPacket packet : this.packets) {
            dataLength += packet.getData().length;
        }

        byte[] res = new byte[dataLength];
        int i = 0;

        for (SsiPacket packet : this.packets) {
            for (int j = 0; j < packet.getData().length; j++) {
                res[i++] = packet.getData()[j];
            }
        }

        return res;
    }

    public List<SsiPacket> getPackets() {
        this.checkCanUse();

        return this.packets;
    }

    byte getOpCode() {
        this.checkCanUse();

        return this.packets.get(0).getOpCode();
    }

    SsiSource getSource() {
        this.checkCanUse();

        return this.packets.get(0).getSource();
    }

    /**
     * Return true if we are not expecting any more data.
     */
    boolean isDataUsable() {
        return fullyLoaded;
    }
}
