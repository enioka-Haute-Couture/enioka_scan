package com.enioka.scanner.sdk.mock;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;

import com.enioka.scanner.LaserScanner;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerConnectionHandler;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.service.ScannerService;
import com.enioka.scanner.service.ScannerServiceApi;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;

/**
 * Tests that the mock is compatible with the library's instantiation methods: LaserScanner and ScannerService.
 */
public class MockTest {

    private static final Context ctx = InstrumentationRegistry.getContext();

    @Test(timeout = 200) // Have to wait for provider search.
    public void testMockRetrievableByLaserScanner(){
        final ScannerSearchOptions options = ScannerSearchOptions.defaultOptions();
        options.useBlueTooth = false;
        options.allowedProviderKeys = new HashSet<>();
        options.allowedProviderKeys.add("MockProvider");

        final TestScannerConnectionHandler handler = new TestScannerConnectionHandler();
        LaserScanner.getLaserScanner(ctx, handler, options);

        for (;handler.mock == null;){}
    }

    @Test(timeout = 200) // Have to wait for service binding and provider search.
    public void testMockRetrievableByScannerService(){
        final TestServiceConnection serviceConnection = new TestServiceConnection();
        final Intent serviceIntent = new Intent(ctx, ScannerService.class);
        serviceIntent.putExtra(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, "MockProvider");

        ctx.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        for (;serviceConnection.binder == null;){}

        final ScannerServiceApi scannerService = serviceConnection.binder.getService();
        for (;scannerService.getConnectedScanners().size() == 0;) {}

        final List<Scanner> scanners = scannerService.getConnectedScanners();
        Assert.assertEquals(1, scanners.size());
        Assert.assertTrue(scanners.get(0) instanceof MockScanner);
    }

    // TOOLS

    private static class TestScannerConnectionHandler implements ScannerConnectionHandler {
        public Scanner.ScannerInitCallback initCallback = new Scanner.ScannerInitCallback() {
            @Override
            public void onConnectionSuccessful(Scanner s) {}
            @Override
            public void onConnectionFailure(Scanner s) {
                Assert.fail();
            }
        };
        public Scanner.ScannerStatusCallback statusCallback = new Scanner.ScannerStatusCallback() {
            @Override
            public void onStatusChanged(String newStatus) {}
            @Override
            public void onScannerReconnecting(Scanner s) {}
            @Override
            public void onScannerDisconnected(Scanner s) {}
        };
        public Scanner.ScannerDataCallback dataCallback = (s, data) -> {};
        public MockScanner mock = null;

        @Override
        public void scannerCreated(String providerKey, String scannerKey, Scanner s) {
            if (s instanceof MockScanner) {
                mock = ((MockScanner) s);
                mock.initialize(ctx, initCallback, dataCallback, statusCallback, null);
            } else {
                Assert.fail("Wrong scanner was detected");
            }
        }

        @Override
        public void endOfScannerSearch() {
            Assert.assertNotNull("Mock was not found", mock);
        }

        @Override
        public void noScannerAvailable() {}
        @Override
        public void scannerConnectionProgress(String providerKey, String scannerKey, String message) {}
    }

    private static class TestServiceConnection implements ServiceConnection {
        public ScannerService.LocalBinder binder;

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (ScannerService.LocalBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Assert.fail("Service disconnected");
        }
    }
}
