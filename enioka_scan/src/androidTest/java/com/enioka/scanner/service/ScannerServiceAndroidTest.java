package com.enioka.scanner.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.test.InstrumentationRegistry;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.sdk.mock.MockProvider;
import com.enioka.scanner.sdk.mock.MockScanner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Tests public methods of ScannerService
 */
public class ScannerServiceAndroidTest {

    // Mock application context
    private static final Context ctx = InstrumentationRegistry.getContext();

    // Semaphore gaining permits only when the binding process of ScannerService ends.
    private static final Semaphore serviceBindingSemaphore = new Semaphore(0);

    // Semaphore gaining permits only when the process of searching a Scanner ends.
    private static final Semaphore scannerDiscoverySemaphore = new Semaphore(0);

    // Semaphore gaining permits only when the process of discovering ScannerProviders ends.
    private static final Semaphore providerDiscoverySemaphore = new Semaphore(0);

    @Before
    public void drainSemaphorePermits() {
        serviceBindingSemaphore.drainPermits();
        scannerDiscoverySemaphore.drainPermits();
        providerDiscoverySemaphore.drainPermits();
    }

    private static void waitOnSemaphore(final Semaphore semaphore) {
        try {
            semaphore.acquire();
        } catch (final InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    @Test
    public void testScannerSearchDelayedAfterBind() {
        final TestServiceConnection serviceConnection = new TestServiceConnection();

        final ScannerSearchOptions options = ScannerSearchOptions.defaultOptions();
        options.useBlueTooth = false;
        options.allowedProviderKeys = new HashSet<>();
        options.allowedProviderKeys.add(MockProvider.PROVIDER_KEY);
        options.startSearchOnServiceBind = false;

        final Intent serviceIntent = new Intent(ctx, ScannerService.class);
        options.toIntentExtras(serviceIntent);
        ctx.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // Wait for binding with ScannerService
        waitOnSemaphore(serviceBindingSemaphore);

        Assert.assertNotNull("ScannerService was not found", serviceConnection.binder);

        final ScannerServiceApi scannerService = serviceConnection.binder.getService();
        scannerService.registerClient(new ScannerClient() {
            @Override
            public void onStatusChanged(@Nullable Scanner scanner, Status newStatus) {}
            @Override
            public void onScannerInitEnded(int count) {
                scannerDiscoverySemaphore.release();
            }
            @Override
            public void onProviderDiscoveryEnded() {
                providerDiscoverySemaphore.release();
            }
            @Override
            public void onData(List<Barcode> data) {}
        });

        // Wait for ScannerService to finish Provider discovery
        waitOnSemaphore(providerDiscoverySemaphore);

        final List<String> providers = scannerService.getAvailableProviders();
        Assert.assertFalse("List of provider keys should not be empty", providers.isEmpty());
        Assert.assertTrue("List of provider keys should contain the Mock provider", providers.contains(MockProvider.PROVIDER_KEY));

        scannerService.restartScannerDiscovery();

        // Wait for ScannerService to finish Scanner discovery
        waitOnSemaphore(scannerDiscoverySemaphore);

        final List<Scanner> scanners = scannerService.getConnectedScanners();
        Assert.assertEquals(1, scanners.size());
        Assert.assertTrue(scanners.get(0) instanceof MockScanner);
    }

    // TOOLS

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
