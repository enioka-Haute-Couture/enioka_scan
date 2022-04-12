package com.enioka.scanner.sdk.mock;

import android.content.Context;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;

public class MockProvider implements ScannerProvider {
    public static final String PROVIDER_KEY = "MockProvider";

    @Override
    public void getScanner(Context ctx, ProviderCallback cb, ScannerSearchOptions options) {
        cb.onScannerCreated(PROVIDER_KEY, "Mock", new MockScanner());
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }
}
