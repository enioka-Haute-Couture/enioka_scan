package com.enioka.scanner.sdk.generalscan.data;

public class DeviceId {
    private String id;

    public DeviceId(String data) {
        this.id = data.replace("[", "").replace("]", "");
    }

    @Override
    public String toString() {
        return this.id;
    }
}
