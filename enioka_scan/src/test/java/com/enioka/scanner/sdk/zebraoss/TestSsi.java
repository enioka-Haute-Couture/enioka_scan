package com.enioka.scanner.sdk.zebraoss;

import com.enioka.scanner.bt.api.ParsingResult;

import org.junit.Test;

import java.nio.charset.Charset;


public class TestSsi {
    private SsiParser parser = new SsiParser();

    @Test
    public void samplePacketTest() {

        ParsingResult res = parseHexString("0c8000800008200000f000f0fcec");
        System.out.println(res.data.toString());
    }

    private ParsingResult parseString(String asciiString) {
        byte[] data = asciiString.getBytes(Charset.forName("ASCII"));

        return parser.parse(data, 0, data.length);
    }

    private ParsingResult parseHexString(String hexString) {
        byte[] data = hexStringToByteArray(hexString);

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
