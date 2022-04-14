package com.enioka.scanner.helpers;

import android.support.annotation.NonNull;

import com.enioka.scanner.api.ScannerProvider;

/**
 * Helper class pairing a ScannerProvider with its corresponding meta information object.
 */
public class ScannerProviderHolder implements Comparable<ScannerProviderHolder> {

    /**
     * The provider's meta information.
     */
    ScannerProviderMeta meta;

    /**
     * The ScannerProvider wrapped by the holder.
     */
    ScannerProvider provider;

    public ScannerProviderHolder(ScannerProvider provider, ScannerProviderMeta meta) {
        this.provider = provider;
        this.meta = meta;
    }

    public ScannerProviderMeta getMeta() {
        return meta;
    }

    public ScannerProvider getProvider() {
        return provider;
    }

    public void setMeta(ScannerProviderMeta meta) {
        this.meta = meta;
    }

    public void setProvider(ScannerProvider provider) {
        this.provider = provider;
    }

    @Override
    public int compareTo(@NonNull ScannerProviderHolder o) {
        return this.meta.compareTo(o.getMeta());
    }
}
