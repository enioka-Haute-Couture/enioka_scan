package com.enioka.scanner.helpers;

import android.support.annotation.NonNull;

import com.enioka.scanner.api.ScannerProvider;

public class ProviderServiceHolder implements Comparable<ProviderServiceHolder> {
    ProviderServiceMeta meta;

    ScannerProvider provider;

    public ProviderServiceHolder(ScannerProvider provider, ProviderServiceMeta meta) {
        this.provider = provider;
        this.meta = meta;
    }

    public ProviderServiceMeta getMeta() {
        return meta;
    }

    public ScannerProvider getProvider() {
        return provider;
    }

    public void setMeta(ProviderServiceMeta meta) {
        this.meta = meta;
    }

    public void setProvider(ScannerProvider provider) {
        this.provider = provider;
    }

    @Override
    public int compareTo(@NonNull ProviderServiceHolder o) {
        return this.meta.compareTo(o.getMeta());
    }
}
