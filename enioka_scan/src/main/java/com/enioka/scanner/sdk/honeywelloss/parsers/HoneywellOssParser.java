package com.enioka.scanner.sdk.honeywelloss.parsers;

import com.enioka.scanner.bt.api.ParsingResult;
import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.sdk.honeywelloss.helpers.MessagePrinter;

/**
 * Honeywell scanners have two very different data structures. This parser simply routes to the correct sub parser.
 */
public class HoneywellOssParser implements ScannerDataParser {
    private static final String LOG_TAG = "HonOssDriver";

    private ParsingMode mode = ParsingMode.NONE;
    private BarcodeParser barcodeParser = new BarcodeParser();
    private MenuParser menuParser = new MenuParser();

    private enum ParsingMode {
        NONE, MENU, DATA
    }

    @Override
    public ParsingResult parse(byte[] buffer, int offset, int dataLength) {
        MessagePrinter.prettyPrint(buffer, offset, dataLength);
        if (mode == ParsingMode.NONE && dataLength > 0 && buffer[offset] == 0x16) {
            mode = ParsingMode.DATA;
        } else if (mode == ParsingMode.NONE) {
            mode = ParsingMode.MENU;
        }

        ParsingResult result = null;
        switch (mode) {
            case DATA:
                result = barcodeParser.parse(buffer, offset, dataLength);
                break;
            case MENU:
                result = menuParser.parse(buffer, offset, dataLength);
                break;
        }

        if (result == null || !result.expectingMoreData) {
            mode = ParsingMode.NONE;
        }

        return result;
    }
}
