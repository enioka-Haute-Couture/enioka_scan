package com.enioka.scanner.sdk.honeywelloss.parsers;

import android.util.Log;

import com.enioka.scanner.bt.api.ParsingResult;
import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.sdk.honeywelloss.data.FirmwareVersion;

/**
 * Parser for one of the two main data structure: menu command answers.
 */
public class MenuParser implements ScannerDataParser {
    private static final String LOG_TAG = "HonOssDriver";

    private String current = "";

    @Override
    public ParsingResult parse(byte[] buffer, int offset, int dataLength) {
        ParsingResult res = new ParsingResult();

        for (int i = offset; i < dataLength; i++) {
            res.read++;
            switch (buffer[i]) {
                case 5: // ENQ: invalid tag or subtag
                    Log.e(LOG_TAG, "command does not exist");
                    res.rejected = true;
                    res.expectingMoreData = false;
                    break;
                case 6: // ACK
                    // Just ignore. It's OK. No need to do anything.
                    break;
                case 21: // NACK: invalid data.
                    Log.e(LOG_TAG, "invalid data was sent to the scanner");
                    res.rejected = true;
                    res.expectingMoreData = false;
                    break;
                case 46: // PERIOD . - end of command, ROM
                case 33: // EXCLAMATION ! - end of command, RAM
                    res.expectingMoreData = false;
                case 59: // SEMI COLUMN ; - end of answer, list
                case 44: // COMMA , - end of answer, list
                    // TODO: a directory of commands, as for the zebra provider.
                    if (current.contains("REVINF")) {
                        res.data = new FirmwareVersion();
                    }
                    current = "";
                    break;
                default:
                    current += Character.toString((char)buffer[i]);
                    break;
            }
        }

        return res;
    }
}
