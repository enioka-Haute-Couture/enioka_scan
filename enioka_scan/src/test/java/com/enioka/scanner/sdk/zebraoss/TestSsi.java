package com.enioka.scanner.sdk.zebraoss;

import com.enioka.scanner.bt.api.ParsingResult;
import com.enioka.scanner.sdk.zebraoss.data.ParamSend;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;


public class TestSsi {
    private SsiParser parser = new SsiParser();

    @Test
    public void samplePacketTest() {

        ParsingResult res = parseHexString("0c8000800008200000f000f0fcec");
        System.out.println(res.data.toString());
    }

    @Test
    public void paramResultTest() {
        // Single word.
        ParsingResult res = parseHexString("0A C6 00 00 FF F4 F0 3E 04 FF FB0C");
        System.out.println(res.data.toString());

        Assert.assertTrue(res.data instanceof ParamSend);
        ParamSend res2 = (ParamSend) res.data;

        Assert.assertEquals(1, res2.parameters.size());
        Assert.assertEquals(318, res2.parameters.get(0).number);
        Assert.assertEquals("1279", res2.parameters.get(0).getStringValue()); // 0xFF

        // Two bytes - 1 (0x01), 156 (0x9C)
        res = parseHexString("09 C6 00 00 FF 01 00 9C 07 FD 8E");
        System.out.println(res.data.toString());

        Assert.assertTrue(res.data instanceof ParamSend);
        res2 = (ParamSend) res.data;

        Assert.assertEquals(2, res2.parameters.size());
        Assert.assertEquals(1, res2.parameters.get(0).number);
        Assert.assertEquals("0x00", res2.parameters.get(0).getStringValue());
        Assert.assertEquals(156, res2.parameters.get(1).number);
        Assert.assertEquals("0x07", res2.parameters.get(1).getStringValue());
    }

    private ParsingResult parseString(String asciiString) {
        byte[] data = asciiString.getBytes(Charset.forName("ASCII"));

        return parser.parse(data, 0, data.length);
    }

    private ParsingResult parseHexString(String hexString) {
        byte[] data = hexStringToByteArray(hexString.replace(" ", ""));

        return parser.parse(data, 0, data.length);
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
