package com.enioka.scanner.sdk.zebraoss_2.parsers;

import com.enioka.scanner.sdk.zebraoss_2.data.ScannerInit;

/**
 * Responsible for handling ACK data
 */
public class ScannerInitParser implements PayloadParser<ScannerInit> {
    @Override
    public ScannerInit parseData(final byte[] dataBuffer) {
        return new ScannerInit();
    }
}
