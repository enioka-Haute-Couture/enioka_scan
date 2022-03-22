package com.enioka.scanner.sdk.zebraoss;

import com.enioka.scanner.bt.api.ParsingResult;
import com.enioka.scanner.sdk.zebraoss.data.ParamSend;
import com.enioka.scanner.sdk.zebraoss.data.RsmAttributeReply;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiParser;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;


public class TestSsi {
    private SsiParser parser = new SsiParser(false);

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

    @Test
    public void rsmTest() {
        // Simple array attribute from doc
        ParsingResult res = parseHexString("21 80 00 00 00 1D 02 00 27 4D 41 01 42 00 0E 00 00 00 00 01 03 02 03 03 03 04 03 05 03 06 03 FF FF FC 15");
        System.out.println(res.data.toString());

        Assert.assertTrue(res.data instanceof RsmAttributeReply);
        RsmAttributeReply res2 = (RsmAttributeReply) res.data;
        Assert.assertEquals(1, res2.attributes.size());
        Assert.assertEquals(10061, res2.attributes.get(0).id);

        // Multiple string attributes
        res = parseHexString("3e 80 00 80 00 3a 02 00 02 15 53 01 00 13 00 00 52 53 35 31 42 30 2d 54 42 53 4e 57 52 20 20 20 20 20 00 02 16 53 01 00 11 00 00 53 32 30 31 34 30 35 32 33 30 32 30 31 34 34 20 00 ff ff f4 34");
        System.out.println(res.data.toString());

        Assert.assertTrue(res.data instanceof RsmAttributeReply);
        res2 = (RsmAttributeReply) res.data;
        Assert.assertEquals(2, res2.attributes.size());
        Assert.assertEquals(533, res2.attributes.get(0).id);
        Assert.assertEquals("RS51B0-TBSNWR", res2.attributes.get(0).data);
        Assert.assertEquals("S20140523020144", res2.attributes.get(1).data);

        //
        res = parseHexString("0f 80 00 80    000b 02 00 0192 42 07 00ffff fc0a");
        System.out.println(res.data.toString());
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
