package com.enioka.scanner.sdk.mock;

import android.content.Context;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;

public class MockProvider implements ScannerProvider {
    static final String PROVIDER_NAME = "MockProvider";

    @Override
    public void getScanner(Context ctx, ProviderCallback cb, ScannerSearchOptions options) {
        cb.onScannerCreated(PROVIDER_NAME, "Mock", new MockScanner());
        cb.onAllScannersCreated(PROVIDER_NAME);
    }

    @Override
    public String getKey() {
        return PROVIDER_NAME;
    }
}
