package com.enioka.scanner.sdk.zebraoss.parsers;

public class NoDataParser implements PayloadParser<Void> {
    public static NoDataParser instance = new NoDataParser();

    private NoDataParser() {
    }

    @Override
    public Void parseData(byte[] buffer) {
        return null;
    }
}
