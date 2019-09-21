package com.enioka.scanner.sdk.generalscan;

import com.enioka.scanner.bt.BtInputHandler;
import com.enioka.scanner.bt.BtParsingResult;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.sdk.generalscan.data.DeviceId;

import java.nio.charset.Charset;

/**
 * A very simple parser - GS only uses ASCII strings, without ACK or anything.
 */
public class Parser implements BtInputHandler {
    private String currentData = null;


    @Override
    public BtParsingResult process(byte[] buffer, int offset, int dataLength) {
        String newData = new String(buffer, offset, dataLength, Charset.forName("ASCII"));
        if (currentData == null) {
            currentData = newData;
        } else {
            currentData += newData;
        }

        if (!currentData.endsWith("\r\n")) {
            return new BtParsingResult();
        }

        // Special parser for this data?
        Object res = null;
        if (currentData.startsWith("[")) {
            res = new DeviceId(currentData);
        }
        // TODO: {GSxxxx} parsing.

        if (res == null) {
            res = new Barcode(currentData, BarcodeType.UNKNOWN);
        }

        currentData = null;
        return new BtParsingResult(res);
    }
}
