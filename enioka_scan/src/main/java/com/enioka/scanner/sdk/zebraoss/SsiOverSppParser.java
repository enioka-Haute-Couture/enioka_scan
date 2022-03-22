package com.enioka.scanner.sdk.zebraoss;

import com.enioka.scanner.bt.api.ParsingResult;
import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiParser;

/**
 * Wraps the handling of SSI packets contained in SPP packets (1 SPP packet = 1 SSI packet).
 */
public class SsiOverSppParser implements ScannerDataParser {
    private static final SsiParser ssiParser = new SsiParser(false);

    @Override
    public ParsingResult parse(byte[] buffer, int offset, int dataLength) {
        return ssiParser.parse(buffer, offset, dataLength);
    }
}
