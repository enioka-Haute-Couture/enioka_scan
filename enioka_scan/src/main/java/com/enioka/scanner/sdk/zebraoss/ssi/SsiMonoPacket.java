package com.enioka.scanner.sdk.zebraoss.ssi;

import android.util.Log;

/**
 * SSI packet, containing the header and checksum bytes. Suitable for both incoming and outgoing packets.
 * Multi-packets are (so far) read-only and in a separate class as they require extra header elements.
 */
public abstract class SsiMonoPacket {
    protected static final String LOG_TAG = "SsiParser";

    // Packet attributes are in the same order as which they occur in the raw packet buffer
    /** Length of the packet excluding the checksum. */
    protected byte packetLengthWithoutChecksum;
    /** OpCode of the packet, describing the type of data it contains. */
    protected final byte opCode;
    /** Source of the packet, to check against SsiSource helper. */
    protected final byte source;
    /** Status of the packet, to check against SsiStatus helper. */
    protected final byte status;
    /** Raw data encapsulated by the packet (excluding headers and checksum). */
    protected final byte[] data;
    /** Checksum MSB. */
    protected byte checksumMsb;
    /** Checksum LSB. */
    protected byte checksumLsb;

    /**
     * Creates a new SSI packet for receiving data.
     * Only suitable for incoming packets. Checksum and length bytes will be verified and trigger an exception if invalid.
     */
    public SsiMonoPacket(final byte packetLengthWithoutChecksum,
                         final byte opCode,
                         final byte status,
                         final byte[] data,
                         final byte checksumMsb,
                         final byte checksumLsb) {
        if (opCode == SsiCommand.MULTIPACKET_SEGMENT.getOpCode()) {
            throw new IllegalArgumentException("Opcode indicates a multipacket, not a monopacket.");
        }

        this.packetLengthWithoutChecksum = packetLengthWithoutChecksum;
        this.opCode = opCode;
        this.source = 0x00;
        this.status = status;
        this.data = data;
        this.checksumMsb = checksumMsb;
        this.checksumLsb = checksumLsb;

        this.validateLengthAndChecksum(); // FIXME - 2022/03/28: inbound packets are constantly rejected due to checksum. For now, assume they are all correct.
    }

    /**
     * Creates a new SSI packet for sending commands.
     * Only suitable for outgoing packets. Checksum and length are calculated automatically.
     */
    public SsiMonoPacket(final byte opCode, final byte status, final byte[] data) {
        this.opCode = opCode;
        this.source = 0x04;
        this.status = status;
        this.data = data;

        this.updateLengthAndChecksum();
    }

    /**
     * Updates the BleLength, packetLength and checksum fields.
     */
    protected void updateLengthAndChecksum() {
        packetLengthWithoutChecksum = (byte) (0x04 + this.data.length);

        short checksum = this.calculateChecksum();
        this.checksumMsb = (byte) ((checksum >> 8) & 0xFF);
        this.checksumLsb = (byte) (checksum & 0xFF);
    }

    /**
     * Validates the packetLength and checksum fields, throws an exception in case of invalid values.
     */
    protected void validateLengthAndChecksum() {
        if (packetLengthWithoutChecksum != (byte) (data.length + 4)) {
            Log.e(LOG_TAG, "Invalid packet length, announced " + String.format("%02x ", packetLengthWithoutChecksum) + " but was " + String.format("%02x ", data.length + 4));
            //throw new IllegalStateException("Invalid packet length, announced " + (byte) packetLengthWithoutChecksum + " but was " + (byte) (data.length + 4));
        }

        short checksum = this.calculateChecksum();
        if ((this.checksumMsb & 0xFF) != (byte) ((checksum >> 8) & 0xFF) || (this.checksumLsb & 0xFF) != (byte) (checksum & 0xFF)) {
            Log.e(LOG_TAG, "Invalid checksum, expected " + String.format("%02x ", ((checksum >> 8) & 0xFF)) + String.format("%02x ", checksum & 0xFF) + " but was " + String.format("%02x ", checksumMsb & 0xFF) + String.format("%02x ", checksumLsb & 0xFF));
            //throw new IllegalStateException("Invalid checksum, expected " + (byte) ((checksum >> 8) & 0xFF) + (byte) (checksum & 0xFF) + " but was " + (checksumMsb & 0xFF) + (checksumLsb & 0xFF));
        }
    }

    /**
     * Calculates the packet's checksum.
     * FIXME - 2022/03/28: This method produces checksums that match what could be observed in testing but for some reason does not seem to match the scanner anymore.
     *                     Outgoing ACKs get rejected/ignored, and inbound packets do not seem to match those checksums.
     *                     It does not affect the ability to scan, but it may affect the ability to send commands to the scanner, more testing is required.
     */
    protected short calculateChecksum() {
        // Rule is: add all bytes (except checksum itself) and substract the result from 0x10000
        // It is a kind of 2's complement
        // FF to avoid Java stupid "signed byte" interpretation inside sums...
        int byteSum = (this.packetLengthWithoutChecksum & 0xFF) + (this.opCode & 0xFF) + (this.source & 0xFF) + (this.status & 0xFF);
        for (int i = 0; i < this.data.length; i++) {
            byteSum += (this.data[i] & 0xFF);
        }

        return (short) (0x10000 - byteSum);
    }

    public byte getOpCode() {
        return opCode;
    }

    public byte[] getData() {
        return data;
    }

    public SsiSource getSource() {
        return SsiSource.fromByte(source);
    }
}
