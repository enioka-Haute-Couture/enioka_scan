package com.enioka.scanner.sdk.zebraoss.data;

public class RsmAttribute {
    public String data;
    public int id;

    @Override
    public String toString() {
        return String.format("%-6s - %s", id, data);
    }
}
