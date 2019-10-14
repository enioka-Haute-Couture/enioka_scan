package com.enioka.scanner.sdk.zebraoss;

import android.util.Log;

/**
 * Represents a single packet inside a potentially multi-packet SSI message. (should never be used outside a {@link SsiMultiPacketMessage})
 */
public class SsiPacket {
    private static final String LOG_TAG = "SsiPacket";

    /**
     * Helper for stream reading.
     */
    private int readBytes = 0;

    protected byte opCode;
    protected byte status = 0;
    private int packetLengthWithoutCheckSum;
    protected byte source = 0x04;
    protected byte[] data = new byte[0];
    private byte checkSumMsb;
    private byte checkSumLsb;

    /**
     * Create a Packet for reading data. All data comes from later {@link #addData(byte[], int, int)} calls.
     */
    SsiPacket() {
    }

    /**
     * Create a packet for sending data. All data is given here.
     *
     * @param opCode SSI operation code
     * @param data   buffer containing the payload of the message
     */
    public SsiPacket(byte opCode, byte[] data) {
        this.opCode = opCode;
        this.data = data;

        this.updateComputedFields();
    }

    /**
     * Create a packet for sending data. All data is given here.
     *
     * @param opCode SSI operation code
     */
    public SsiPacket(byte opCode) {
        this.opCode = opCode;
        this.data = new byte[0];

        this.updateComputedFields();
    }

    /**
     * @param buffer       data from scanner stream
     * @param offset       read buffer from this position only. 0-based.
     * @param bufferLength read buffer until from + bufferLength
     * @return true if another buffer is expected (buffer incomplete or multi part message).
     */
    boolean addData(byte[] buffer, int offset, int bufferLength) {
        if (bufferLength == 0) {
            return true;
        }

        // First byte is packet length.
        int bufferIndex = (byte) offset;
        if (readBytes == 0 && bufferLength > 0) {
            packetLengthWithoutCheckSum = (0xFF & buffer[bufferIndex]);
            readBytes++;
            bufferIndex++;

            if (packetLengthWithoutCheckSum < 4) {
                Log.e(LOG_TAG, "Received a message with an impossible length - ignored");
                return false; // Packet is weird. Stop reading it at once.
            }

            data = new byte[packetLengthWithoutCheckSum - 4];
        }

        // Second byte is operation code.
        if (readBytes == 1 && bufferIndex < bufferLength) {
            opCode = buffer[bufferIndex];
            readBytes++;
            bufferIndex++;
        }

        // Third byte is packet source.
        if (readBytes == 2 && bufferIndex < bufferLength) {
            source = buffer[bufferIndex];
            readBytes++;
            bufferIndex++;
        }

        // Fourth byte is status. (used with masks in accessors)
        if (readBytes == 3 && bufferIndex < bufferLength) {
            status = buffer[bufferIndex];
            readBytes++;
            bufferIndex++;
        }

        // After this, we may have data.
        while (readBytes < packetLengthWithoutCheckSum && bufferIndex < bufferLength) {
            data[readBytes - 4] = buffer[bufferIndex];
            readBytes++;
            bufferIndex++;
        }

        // Finally checksum is on two bytes
        if (readBytes == packetLengthWithoutCheckSum && bufferIndex < bufferLength) {
            checkSumMsb = buffer[bufferIndex];
            readBytes++;
            bufferIndex++;
        }
        if (readBytes == packetLengthWithoutCheckSum + 1 && bufferIndex < bufferLength) {
            checkSumLsb = buffer[bufferIndex];
            readBytes++;
            bufferIndex++;
        }

        // Done. Is this the packet end?
        if (readBytes != packetLengthWithoutCheckSum + 2) {
            Log.d(LOG_TAG, "Still missing bytes to complete SSI packet: " + (packetLengthWithoutCheckSum + 2 - readBytes));
        }
        return readBytes != packetLengthWithoutCheckSum + 2;
    }

    SsiSource getSource() {
        return (this.source == 0 ? SsiSource.DECODER : SsiSource.HOST);
    }

    byte[] getData() {
        return this.data;
    }

    public byte getOpCode() {
        return this.opCode;
    }

    public void setOpCode(byte opCode) {
        this.opCode = opCode;
    }

    boolean isLastPacket() {
        return (this.status & 0b01000000) == 0;
    }

    boolean isRetransmit() {
        return (this.status & 0b00000001) != 0;
    }

    boolean isMultiPacket() {
        return (this.status & 0b00000010) != 0;
    }

    boolean isTransientChange() {
        return (this.status & 0b00010000) == 0;
    }

    boolean isChecksumValid() {
        short checksum = this.getChecksum();

        return this.checkSumMsb == (byte) (checksum >> 8) && this.checkSumLsb == (byte) checksum;
    }

    /**
     * Create a single-packet message.
     *
     * @return the full message.
     */
    public byte[] getMessageData() {
        byte[] res = new byte[this.packetLengthWithoutCheckSum + 2];
        res[0] = (byte) (this.packetLengthWithoutCheckSum);
        res[1] = this.opCode;
        res[2] = this.source;
        res[3] = this.status;
        for (int i = 0; i < this.data.length; i++) {
            res[4 + i] = this.data[i];
        }

        short checksum = this.getChecksum();
        this.checkSumMsb = (byte) ((checksum >> 8) & 0xff);
        this.checkSumLsb = (byte) (checksum & 0xff);

        res[this.packetLengthWithoutCheckSum] = this.checkSumMsb;
        res[this.packetLengthWithoutCheckSum + 1] = this.checkSumLsb;

        return res;
    }

    public void setMessageData(byte[] data) {
        this.data = data;
    }

    /**
     * Compute length and checksum. (if we ever need to do multi packets, could be computed here too).
     */
    public void updateComputedFields() {
        this.packetLengthWithoutCheckSum = (byte) (4 + this.data.length);

        short checksum = this.getChecksum();
        this.checkSumMsb = (byte) (checksum & 0xff);
        this.checkSumLsb = (byte) ((checksum >> 8) & 0xff);
    }

    private short getChecksum() {
        // Rule is: add all bytes (except checksum itself) and substract the result from 0x10000
        // It is a kind of 2's complement
        // FF to avoid Java stupid "signed byte" interpretation inside sums...
        int byteSum = (this.packetLengthWithoutCheckSum & 0xFF) + (this.opCode & 0xFF) + (this.source & 0xFF) + (this.status & 0xFF);
        for (int i = 0; i < this.data.length; i++) {
            byteSum += (this.data[i] & 0xFF);
        }

        return (short) (0x10000 - byteSum);
    }

    public int getTimeOut() {
        return 100;
    }

    public byte[] getCommand() {
        return this.getMessageData();
    }
}
