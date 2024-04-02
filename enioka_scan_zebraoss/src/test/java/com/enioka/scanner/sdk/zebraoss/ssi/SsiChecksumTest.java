package com.enioka.scanner.sdk.zebraoss.ssi;

import org.junit.Assert;
import org.junit.Test;

/**
 * Can only test on known values of non-variable packets, like CMD_ACK packets which never change.
 * Checksums of Decoder packets will be off by 4 compared to the expected value as SsiMonoPacket counts them as Host packets with this constructor.
 */
public class SsiChecksumTest {

    @Test
    public void testHostCmdAckChecksum() {
        final SsiMonoPacketWrapper ssiPacket = new SsiMonoPacketWrapper(SsiCommand.CMD_ACK.getOpCode(), (byte) 0x00, new byte[0]);
        final byte[] ssiPacketBuffer = ssiPacket.toCommandBuffer(false);

        Assert.assertEquals(6, ssiPacketBuffer.length);
        Assert.assertEquals(4, ssiPacketBuffer[0]);
        Assert.assertEquals((byte) (0xFF & 0xFF), ssiPacketBuffer[ssiPacketBuffer.length - 2]);
        Assert.assertEquals((byte) (0x28 & 0xFF), ssiPacketBuffer[ssiPacketBuffer.length - 1]);
    }

    @Test
    public void testHostStartSessionChecksum() {
        final SsiMonoPacketWrapper ssiPacket = new SsiMonoPacketWrapper(SsiCommand.START_SESSION.getOpCode(), (byte) 0x00, new byte[0]);
        final byte[] ssiPacketBuffer = ssiPacket.toCommandBuffer(false);

        Assert.assertEquals(6, ssiPacketBuffer.length);
        Assert.assertEquals(4, ssiPacketBuffer[0]);
        Assert.assertEquals((byte) (0xFF & 0xFF), ssiPacketBuffer[ssiPacketBuffer.length - 2]);
        Assert.assertEquals((byte) (0x14 & 0xFF), ssiPacketBuffer[ssiPacketBuffer.length - 1]);
    }

    @Test
    public void testHostScanEnableChecksum() {
        final SsiMonoPacketWrapper ssiPacket = new SsiMonoPacketWrapper(SsiCommand.SCAN_ENABLE.getOpCode(), (byte) 0x08, new byte[0]);
        final byte[] ssiPacketBuffer = ssiPacket.toCommandBuffer(false);

        Assert.assertEquals(6, ssiPacketBuffer.length);
        Assert.assertEquals(4, ssiPacketBuffer[0]);
        Assert.assertEquals((byte) (0xFF & 0xFF), ssiPacketBuffer[ssiPacketBuffer.length - 2]);
        Assert.assertEquals((byte) (0x07 & 0xFF), ssiPacketBuffer[ssiPacketBuffer.length - 1]);
    }

    @Test
    public void testDecoderCmdAckChecksum() {
        final SsiMonoPacketWrapper ssiPacket = new SsiMonoPacketWrapper(SsiCommand.CMD_ACK.getOpCode(), (byte) 0x80, new byte[0]);
        final byte[] ssiPacketBuffer = ssiPacket.toCommandBuffer(false);

        Assert.assertEquals(6, ssiPacketBuffer.length);
        Assert.assertEquals(4, ssiPacketBuffer[0]);
        Assert.assertEquals((byte) (0xFE & 0xFF), ssiPacketBuffer[ssiPacketBuffer.length - 2]);
        Assert.assertEquals((byte) ((0xAC - 0x4) & 0xFF), ssiPacketBuffer[ssiPacketBuffer.length - 1]);
    }

    @Test
    public void testDecoderEvent20Checksum() {
        final SsiMonoPacketWrapper ssiPacket = new SsiMonoPacketWrapper(SsiCommand.EVENT.getOpCode(), (byte) 0x00, new byte[] {0x20});
        final byte[] ssiPacketBuffer = ssiPacket.toCommandBuffer(false);

        Assert.assertEquals(7, ssiPacketBuffer.length);
        Assert.assertEquals(5, ssiPacketBuffer[0]);
        Assert.assertEquals((byte) (0xFE & 0xFF), ssiPacketBuffer[ssiPacketBuffer.length - 2]);
        Assert.assertEquals((byte) ((0xE5 - 0x04) & 0xFF), ssiPacketBuffer[ssiPacketBuffer.length - 1]);
    }

    @Test
    public void testDecoderEvent21Checksum() {
        final SsiMonoPacketWrapper ssiPacket = new SsiMonoPacketWrapper(SsiCommand.EVENT.getOpCode(), (byte) 0x00, new byte[] {0x21});
        final byte[] ssiPacketBuffer = ssiPacket.toCommandBuffer(false);

        Assert.assertEquals(7, ssiPacketBuffer.length);
        Assert.assertEquals(5, ssiPacketBuffer[0]);
        Assert.assertEquals((byte) (0xFE & 0xFF), ssiPacketBuffer[ssiPacketBuffer.length - 2]);
        Assert.assertEquals((byte) ((0xE4 - 0x04) & 0xFF), ssiPacketBuffer[ssiPacketBuffer.length - 1]);
    }
}
