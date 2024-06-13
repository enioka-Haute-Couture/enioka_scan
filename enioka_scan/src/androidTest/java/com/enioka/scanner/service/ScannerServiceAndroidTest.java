package com.enioka.scanner.service;

import static com.enioka.scanner.helpers.Common.buildBarcode;
import static com.enioka.scanner.helpers.Common.findMacAddress;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.test.InstrumentationRegistry;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.sdk.mock.MockProvider;
import com.enioka.scanner.sdk.mock.MockScanner;
import com.google.zxing.WriterException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
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
    public void testBarcodeGeneration() {
        int[] bitmapRef = new int[]{-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 3, 32, 0, 0, 0, -96, 8, 6, 0, 0, 0, -12, 40, 8, 45, 0, 0, 0, 1, 115, 82, 71, 66, 0, -82, -50, 28, -23, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8, 124, 8, 100, -120, 0, 0, 3, -63, 73, 68, 65, 84, 120, -100, -19, -41, 73, 14, -125, 48, 16, 0, -63, -112, -1, -1, -103, 124, -128, -61, 72, 88, 13, -127, -86, 51, -104, -59, -117, -44, -37, -66, -17, -5, -25, -27, -74, 109, 27, 93, 119, -12, -85, -114, -18, -99, -2, -46, 51, -9, 78, 77, -65, -19, -56, -12, 123, -117, -15, 86, -33, 59, 53, -99, -93, -43, 115, -71, 122, 93, 77, -83, 126, 70, 49, 111, -59, -102, 92, -67, -89, -117, -11, 114, -28, -86, -67, 48, -75, 122, -68, -43, -49, 40, -50, -10, 39, -97, -79, 119, 26, -17, 78, -17, 50, 29, -17, 78, -5, -9, 31, -49, -85, -87, 51, 103, 118, -15, 109, 79, -15, -67, -6, 5, 0, 0, -128, -9, 16, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, -103, 31, 117, 56, 116, 60, 81, 38, 123, -16, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        final String testedBarcodeData = "PH16A200000";

        try {
            Bitmap result = buildBarcode(testedBarcodeData, 800, 160);
            byte[] byteArray = convertBitmapToByteArray(result);

            // Check if the generated barcode is the same as the reference
            Assert.assertEquals(bitmapRef.length, byteArray.length);
            for (int i = 0; i < byteArray.length; i++) {
                Assert.assertEquals(bitmapRef[i], byteArray[i]);
            }

        } catch (WriterException e) {
            Assert.fail("Barcode generation failed");
        }
    }

    @Test
    public void testBarcodeGenerationSecond() {
        int[] bitmapRef = new int[]{-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 3, 32, 0, 0, 0, -96, 8, 6, 0, 0, 0, -12, 40, 8, 45, 0, 0, 0, 1, 115, 82, 71, 66, 0, -82, -50, 28, -23, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8, 124, 8, 100, -120, 0, 0, 3, -53, 73, 68, 65, 84, 120, -100, -19, -41, 75, 110, -125, 48, 0, 64, -63, -46, -5, -33, -103, -20, 89, -60, -78, 108, 30, 73, 59, -77, -27, 99, 3, -58, -46, 59, -50, -13, 60, 127, -72, -51, 113, 28, 111, -113, 95, 95, -1, -11, -4, -47, -25, -103, 61, 127, 100, 117, -66, 35, -77, -13, 27, 61, -33, -20, -15, -39, -7, 124, -5, -5, 93, -99, -1, 95, 91, 15, -69, -17, 127, -75, 123, -68, -39, -13, 119, -49, 111, 52, -2, -22, -3, 87, -97, 103, 52, -2, -35, -1, -17, -89, -19, 55, -77, -17, -21, 106, 117, 127, -3, -76, -1, 101, -10, -6, -89, -65, -33, -22, -3, 118, -17, -65, -69, -9, -1, -69, -41, -13, -45, -21, -109, 57, -65, 79, 79, 0, 0, 0, -8, 63, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, 70, -128, 0, 0, 0, 25, 1, 2, 0, 0, 100, 4, 8, 0, 0, -112, 17, 32, 0, 0, 64, -26, 5, -122, 91, -122, 60, -70, -66, -63, -98, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        final String testedBarcodeData = "TeSTBaRcoDe";

        try {
            Bitmap result = buildBarcode(testedBarcodeData, 800, 160);
            byte[] byteArray = convertBitmapToByteArray(result);
            Log.d("test", "array length" + byteArray.length);

            // Check if the generated barcode is the same as the reference
            Assert.assertEquals(bitmapRef.length, byteArray.length);
            for (int i = 0; i < byteArray.length; i++) {
                Assert.assertEquals(bitmapRef[i], byteArray[i]);
            }

        } catch (WriterException e) {
            Assert.fail("Barcode generation failed");
        }
    }

    @Test
    public void testMacAddress() {
        String macAddress = findMacAddress();

        Assert.assertEquals("Mac address should be the default Android one", "2:0:0:0:0:0", macAddress);
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

    private static byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        return baos.toByteArray();
    }
}
