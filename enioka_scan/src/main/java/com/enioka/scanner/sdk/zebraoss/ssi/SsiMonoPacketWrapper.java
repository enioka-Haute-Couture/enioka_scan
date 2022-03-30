package com.enioka.scanner.sdk.zebraoss.ssi;

/**
 * Encapsulates the SsiMonoPacket class to separate the protocol-dependant bytearray generation.
 */
public class SsiMonoPacketWrapper extends SsiMonoPacket {

    public SsiMonoPacketWrapper(byte packetLengthWithoutChecksum, byte opCode, byte status, byte[] data, byte checksumMsb, byte checksumLsb) {
        super(packetLengthWithoutChecksum, opCode, status, data, checksumMsb, checksumLsb);
    }

    public SsiMonoPacketWrapper(byte opCode, byte status, byte[] data) {
        super(opCode, status, data);
    }

    /**
     * Generates the raw packet bytearray.
     */
    public byte[] toCommandBuffer(final boolean ble) {
        validateLengthAndChecksum(); // FIXME - 2022/03/28: Checksum problems may cause this to fail for inbound packets. Constructors both call update/validation and values cannot be changed outside so let's assume they are correct for now.

        final int totalLength = (ble ? 2 : 0) + 4 + data.length + 2; // bleLength + header + data + checksum
        final int offset = (ble ? 2 : 0);

        final byte[] buffer = new byte[totalLength];

        if (ble) {
            buffer[offset - 2] = (byte) (totalLength & 0xFF);
            buffer[offset - 1] = (byte) ((totalLength >> 8) & 0xFF);
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
}
