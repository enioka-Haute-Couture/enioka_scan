package com.enioka.scanner.helpers;

import android.content.pm.ServiceInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Helper class containing a ScannerProvider's meta information.
 */
public class ScannerProviderMeta implements Comparable {

    /**
     * Whether the provided scanner uses Bluetooth or not.
     */
    private boolean bluetooth;

    /**
     * The provider's name.
     */
    private String name;

    /**
     * The provider's priority during searches.
     */
    private int priority;

    private String providerKey;

    public ScannerProviderMeta(ServiceInfo si) {
        this.bluetooth = si.metaData != null && si.metaData.getBoolean("bluetooth");
        this.priority = si.metaData != null ? si.metaData.getInt("priority", 0) : 0;

        this.name = si.name;
    }

    public ScannerProviderMeta(boolean bluetooth, int priority, String name) {
        this.bluetooth = bluetooth;
        this.priority = priority;

        this.name = name;
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
        if (!(obj instanceof ScannerProviderMeta)) {
            return false;
        }
        return this.name.equals(((ScannerProviderMeta) obj).name);
    }

    @Override
    public int compareTo(@NonNull Object obj) {
        if (!(obj instanceof ScannerProviderMeta)) {
            return 0;
        }
        ScannerProviderMeta o = (ScannerProviderMeta) obj;
        return this.priority == o.priority ? this.name.compareTo(o.name) : this.priority - o.priority;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

    public String getProviderKey() {
        return providerKey;
    }
}
