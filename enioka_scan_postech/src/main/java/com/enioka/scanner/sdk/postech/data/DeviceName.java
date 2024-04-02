package com.enioka.scanner.sdk.postech.data;

public class DeviceName {
    private String id;

    public DeviceName(String data) {
        this.id = data.replace("[", "").replace("]", "");
    }

    @Override
    public String toString() {
        return this.id;
    }
}
