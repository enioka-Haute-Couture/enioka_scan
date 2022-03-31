package com.enioka.scanner.sdk.zebraoss;

import com.enioka.scanner.bt.api.ParsingResult;
import com.enioka.scanner.sdk.zebraoss.data.Ack;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiMonoPacketWrapper;

import org.junit.Assert;
import org.junit.Test;

public class SsiOverAttParserTest {

    @Test
    public void testOneSsiForOneAtt() { // data length equal to size of SSI packet
        final SsiOverAttParser parser = new SsiOverAttParser();

        final SsiMonoPacketWrapper ssiPacket = new SsiMonoPacketWrapper(SsiCommand.CMD_ACK.getOpCode(), (byte) 0x80, new byte[0]);
        final byte[] ssiPacketBuffer = ssiPacket.toCommandBuffer(true);
        // Adapting to incoming rather than outgoing
        ssiPacketBuffer[4] -= 0x04;
        ssiPacketBuffer[7] += 0x04;

        ParsingResult res = parser.parse(ssiPacketBuffer, 0, ssiPacketBuffer.length);
        Assert.assertFalse("Should not expect more data", res.expectingMoreData);
        Assert.assertTrue("Result data should be an ACK", res.data instanceof Ack);
        Assert.assertEquals(res.read, ssiPacketBuffer.length - 2); // exclude the BLE bytes
    }

    @Test
    public void testOneSsiForManyAtt() { // data length shorter than SSI packet
        final SsiOverAttParser parser = new SsiOverAttParser();

        final SsiMonoPacketWrapper ssiPacket = new SsiMonoPacketWrapper(SsiCommand.CMD_ACK.getOpCode(), (byte) 0x80, new byte[0]);
        final byte[] ssiPacketBuffer = ssiPacket.toCommandBuffer(true);
        // Adapting to incoming rather than outgoing
        ssiPacketBuffer[4] -= 0x04;
        ssiPacketBuffer[7] += 0x04;

        ParsingResult res = parser.parse(ssiPacketBuffer, 0, ssiPacketBuffer.length - 3);
        Assert.assertTrue("Should expect more data", res.expectingMoreData);

        res = parser.parse(ssiPacketBuffer, ssiPacketBuffer.length - 3, ssiPacketBuffer.length);
        Assert.assertFalse("Should not expect more data", res.expectingMoreData);
        Assert.assertTrue("Result data should be an ACK", res.data instanceof Ack);
        Assert.assertEquals(res.read, ssiPacketBuffer.length - 2); // exclude the BLE bytes
    }
}
