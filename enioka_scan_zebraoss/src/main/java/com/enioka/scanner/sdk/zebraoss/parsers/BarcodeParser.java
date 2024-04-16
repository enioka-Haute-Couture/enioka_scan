package com.enioka.scanner.sdk.zebraoss.parsers;

import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiSymbologyTranslator;

import java.nio.charset.StandardCharsets;

/**
 * Responsible for handling
 */
public class BarcodeParser implements PayloadParser<Barcode> {
    @Override
    public Barcode parseData(final byte[] dataBuffer) {
        if (dataBuffer.length < 5) {
            return null;
        }
        byte symbology = dataBuffer[0];
        return new Barcode(new String(dataBuffer, 4, dataBuffer.length - 4, StandardCharsets.US_ASCII), SsiSymbologyTranslator.sdk2Api(symbology));
    }
}
