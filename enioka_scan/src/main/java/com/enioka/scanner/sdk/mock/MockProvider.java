package com.enioka.scanner.sdk.mock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerProviderBinder;
import com.enioka.scanner.api.ScannerSearchOptions;

public class MockProvider extends Service implements ScannerProvider {
    static final String PROVIDER_NAME = "MockProvider";

    private final IBinder binder = new ScannerProviderBinder(this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void getScanner(Context ctx, ProviderCallback cb, ScannerSearchOptions options) {
        cb.onScannerCreated(PROVIDER_NAME, "Mock", new MockScanner());
    }

    @Override
    public String getKey() {
        return PROVIDER_NAME;
    }
}
