package com.enioka.scanner.sdk.zebraoss.parsers;

import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.sdk.zebraoss.ZebraOssDataTranslator;

import java.nio.charset.Charset;

/**
 * Responsible for handling
 */
public class BarcodeParser implements PayloadParser<Barcode> {
    @Override
    public Barcode parseData(byte[] buffer) {
        if (buffer.length < 2) {
            return null;
        }
        byte symbology = buffer[0];
        return new Barcode(new String(buffer, Charset.forName("ASCII")), ZebraOssDataTranslator.sdk2Api(symbology));
    }
}
