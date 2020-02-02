package com.enioka.scanner.helpers;

import android.content.pm.ServiceInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ProviderServiceMeta implements Comparable {

    private boolean bluetooth;

    private String name;

    private int priority;

    private String providerKey;

    public ProviderServiceMeta(ServiceInfo si) {
        this.bluetooth = si.metaData != null && si.metaData.getBoolean("bluetooth");
        this.priority = si.metaData != null ? si.metaData.getInt("priority", 0) : 0;

        this.name = si.name;
    }

    public boolean isBluetooth() {
        return bluetooth;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof ProviderServiceMeta)) {
            return false;
        }
        return this.name.equals(((ProviderServiceMeta) obj).name);
    }

    @Override
    public int compareTo(@NonNull Object obj) {
        if (!(obj instanceof ProviderServiceMeta)) {
            return 0;
        }
        ProviderServiceMeta o = (ProviderServiceMeta) obj;
        return this.priority == o.priority ? this.name.compareTo(o.name) : this.priority - o.priority;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

    public String getProviderKey() {
        return providerKey;
    }
}
