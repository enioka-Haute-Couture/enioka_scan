package com.enioka.scanner.sdk.zebraoss_2.ssi;

/**
 * SSI packet, containing the header and checksum bytes. Suitable for both incoming and outgoing packets.
 * Multi-packets are (so far) read-only and in a separate class as they require extra header elements.
 */
public final class SsiMonoPacket {
    private static final String LOG_TAG = "SsiMonoPacket";
    private final boolean ble;

    // Packet attributes are in the same order as which they occur in the raw packet buffer
    /** MSB of total length in little-endian used by BLE only, for outgoing packets only (these bytes should be skipped for reading). */
    private byte blePacketLengthMsb;
    /** LSB of total length in little-endian used by BLE only, for outgoing packets only (these bytes should be skipped for reading). */
    private byte blePacketLengthLsb;
    /** Length of the packet excluding the checksum. */
    private byte packetLengthWithoutChecksum;
    /** OpCode of the packet, describing the type of data it contains. */
    private final byte opCode;
    /** Source of the packet, to check against SsiSource helper. */
    private final byte source;
    /** Status of the packet, to check against SsiStatus helper. */
    private final byte status;
    /** Raw data encapsulated by the packet (excluding headers and checksum). */
    private final byte[] data;
    /** Checksum MSB. */
    private byte checksumMsb;
    /** Checksum LSB. */
    private byte checksumLsb;

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
        if (opCode == SsiCommand.MULTIPACKET_SEGMENT.getOpCode())
            throw new IllegalArgumentException("Opcode indicates a multipacket, not a monopacket.");

        this.ble = false;

        this.packetLengthWithoutChecksum = packetLengthWithoutChecksum;
        this.opCode = opCode;
        this.source = 0x00;
        this.status = status;
        this.data = data;
        this.checksumMsb = checksumMsb;
        this.checksumLsb = checksumLsb;

        this.validateLengthAndChecksum();
    }

    /**
     * Creates a new SSI packet for sending commands.
     * Only suitable for outgoing packets. Checksum and length are calculated automatically.
     * @param ble Whether the packet must contain BLE length bytes or not.
     */
    public SsiMonoPacket(final byte opCode, final byte status, final byte[] data, final boolean ble) {
        this.ble = ble;

        this.opCode = opCode;
        this.source = 0x04;
        this.status = status;
        this.data = data;

        this.updateLengthAndChecksum();
    }

    /**
     * Updates the BleLength, packetLength and checksum fields.
     */
    private void updateLengthAndChecksum() {
        packetLengthWithoutChecksum = (byte) (0x04 + this.data.length);

        if (ble) {
            short totalLength = (short) (packetLengthWithoutChecksum + 4);
            blePacketLengthMsb = (byte) ((totalLength >> 8) & 0xFF);
            blePacketLengthLsb = (byte) (totalLength & 0xFF);
        }

        short checksum = this.calculateChecksum();
        this.checksumMsb = (byte) ((checksum >> 8) & 0xff);
        this.checksumLsb = (byte) (checksum & 0xff);
    }

    /**
     * Validates the packetLength and checksum fields, throws an exception in case of invalid values.
     */
    private void validateLengthAndChecksum() {
        if (packetLengthWithoutChecksum != (byte) (0x04 + data.length))
            throw new IllegalStateException("Invalid packet length");


        short checksum = this.calculateChecksum();
        if (this.checksumMsb != (byte) ((checksum >> 8) & 0xff) || this.checksumLsb != (byte) (checksum & 0xff))
            throw new IllegalStateException("Invalid checksum");
    }

    /**
     * Calculates the packet's checksum.
     */
    private short calculateChecksum() {
        // Rule is: add all bytes (except checksum itself) and substract the result from 0x10000
        // It is a kind of 2's complement
        // FF to avoid Java stupid "signed byte" interpretation inside sums...
        int byteSum = (this.packetLengthWithoutChecksum & 0xFF) + (this.opCode & 0xFF) + (this.source & 0xFF) + (this.status & 0xFF);
        for (int i = 0; i < this.data.length; i++) {
            byteSum += (this.data[i] & 0xFF);
        }

        return (short) (0x10000 - byteSum);
    }

    /**
     * Generates the raw packet buffer.
     */
    public byte[] toCommandBuffer() {
        validateLengthAndChecksum();

        final int totalLength = (ble ? 2 : 0) + 4 + data.length + 2; // bleLength + header + data + checksum
        final byte[] buffer = new byte[totalLength];

        final int offset = (ble ? 2 : 0);
        if (ble) {
            buffer[offset - 2] = blePacketLengthMsb;
            buffer[offset - 1] = blePacketLengthLsb;
        }
        buffer[offset] = packetLengthWithoutChecksum;
        buffer[offset + 1] = opCode;
        buffer[offset + 2] = source;
        buffer[offset + 3] = status;
        System.arraycopy(data, 0, buffer, offset + 4, data.length);
        buffer[totalLength - 2] = checksumMsb;
        buffer[totalLength - 1] = checksumLsb;

        return buffer;
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
