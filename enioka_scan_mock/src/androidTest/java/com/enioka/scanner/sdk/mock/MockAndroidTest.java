package com.enioka.scanner.sdk.mock;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;

import com.enioka.scanner.LaserScanner;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.callbacks.ScannerConnectionHandler;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.api.callbacks.ScannerDataCallback;
import com.enioka.scanner.api.callbacks.ScannerInitCallback;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.api.proxies.ScannerConnectionHandlerProxy;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerInitCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.service.BackgroundScannerClient;
import com.enioka.scanner.service.ScannerService;
import com.enioka.scanner.service.ScannerServiceApi;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Tests that the mock is compatible with the library's instantiation methods: LaserScanner and ScannerService.
 */
public class MockAndroidTest {

    // Mock application context
    private static final Context ctx = InstrumentationRegistry.getContext();

    // Semaphore gaining permits only when the binding process of ScannerService ends.
    private static final Semaphore serviceBindingSemaphore = new Semaphore(0);

    // Semaphore gaining permits only when the process of searching a Scanner ends.
    private static final Semaphore scannerDiscoverySemaphore = new Semaphore(0);

    @Before
    public void drainSemaphorePermits() {
        serviceBindingSemaphore.drainPermits();
        scannerDiscoverySemaphore.drainPermits();
    }

    public static void waitOnSemaphore(final Semaphore semaphore) {
        try {
            semaphore.acquire();
        } catch (final InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    @Test
    public void testMockRetrievableByLaserScanner(){
        final ScannerSearchOptions options = ScannerSearchOptions.defaultOptions();
        options.useBlueTooth = false;
        options.allowedProviderKeys = new HashSet<>();
        options.allowedProviderKeys.add("MockProvider");

        final TestScannerConnectionHandler handler = new TestScannerConnectionHandler();
        LaserScanner.getLaserScanner(ctx, new ScannerConnectionHandlerProxy(handler), options);

        // Wait for LaserScanner to finish Scanner discovery
        waitOnSemaphore(scannerDiscoverySemaphore);

        Assert.assertNotNull("Mock scanner was not found", handler.mock);
    }

    @Test
    public void testMockRetrievableByScannerService(){
        final TestServiceConnection serviceConnection = new TestServiceConnection();
        final Intent serviceIntent = new Intent(ctx, ScannerService.class);
        serviceIntent.putExtra(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, new String[]{"MockProvider"});

        ctx.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // Wait for binding with ScannerService
        waitOnSemaphore(serviceBindingSemaphore);

        Assert.assertNotNull("ScannerService was not found", serviceConnection.binder);

        final ScannerServiceApi scannerService = serviceConnection.binder.getService();
        scannerService.registerClient(new BackgroundScannerClient() {
            @Override
            public void onStatusChanged(@Nullable Scanner scanner, Status newStatus) {
                if (newStatus == Status.CONNECTED) // should be SERVICE_SDK_SEARCH_OVER but because methods on the UI thread are queued the "success" init callback may be queued after the "end of search" event.
                    scannerDiscoverySemaphore.release();
            }

            @Override
            public void onBackgroundScannerInitEnded(int count) {}
            @Override
            public void onData(List<Barcode> data) {}
        });

        // Wait for ScannerService to finish Scanner discovery
        waitOnSemaphore(scannerDiscoverySemaphore);

        final List<Scanner> scanners = scannerService.getConnectedScanners();
        Assert.assertEquals(1, scanners.size());
        Assert.assertTrue(scanners.get(0) instanceof MockScanner);
    }

    // TOOLS

    private static class TestScannerConnectionHandler implements ScannerConnectionHandler {
        public ScannerInitCallback initCallback = new ScannerInitCallback() {
            @Override
            public void onConnectionSuccessful(Scanner s) {}
            @Override
            public void onConnectionFailure(Scanner s) {
                Assert.fail();
            }
        };
        public ScannerStatusCallback statusCallback = (scanner, newStatus) -> {};
        public ScannerDataCallback dataCallback = (s, data) -> {};
        public MockScanner mock = null;

        @Override
        public void scannerCreated(String providerKey, String scannerKey, Scanner s) {
            if (s instanceof MockScanner) {
                mock = ((MockScanner) s);
                mock.initialize(ctx, new ScannerInitCallbackProxy(initCallback), new ScannerDataCallbackProxy(dataCallback), new ScannerStatusCallbackProxy(statusCallback), null);
            } else {
                Assert.fail("Wrong scanner was detected");
            }
        }

        @Override
        public void endOfScannerSearch() {
            Assert.assertNotNull("Mock was not found", mock);
            // Scanner discovery ended
            scannerDiscoverySemaphore.release();
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
            // Service binding ended
            serviceBindingSemaphore.release();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Assert.fail("Service disconnected");
        }
    }
}
