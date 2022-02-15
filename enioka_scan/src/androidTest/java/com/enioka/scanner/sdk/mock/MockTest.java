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
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.service.ScannerService;
import com.enioka.scanner.service.ScannerServiceApi;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class  MockTest {

    private static final Context ctx = InstrumentationRegistry.getContext();

    @Test
    public void testMockRetrievableByLaserScanner(){
        final ScannerSearchOptions options = ScannerSearchOptions.defaultOptions();
        final TestInitCallback initCallback = new TestInitCallback();
        final TestStatusCallback statusCallback = new TestStatusCallback();
        final TestDataCallback dataCallback = new TestDataCallback();
        final TestScannerConnectionHandler handler = new TestScannerConnectionHandler(initCallback, statusCallback, dataCallback);
        LaserScanner.getLaserScanner(ctx, handler, options);
    }

    @Test(timeout = 3000)
    public void testMockRetrievableByScannerService(){
        final Intent serviceIntent = new Intent(ctx, ScannerService.class);
        final TestServiceConnection serviceConnection = new TestServiceConnection();
        ctx.startService(serviceIntent);
        ctx.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        for (;serviceConnection.binder == null;){}

        final ScannerServiceApi scannerService = serviceConnection.binder.getService();

        for (;scannerService.getConnectedScanners().size() == 0;) {}

        final List<Scanner> scanners = scannerService.getConnectedScanners();
        Assert.assertEquals(1, scanners.size());
        Assert.assertTrue(scanners.get(0) instanceof MockScanner);
    }

    private static class TestStatusCallback implements Scanner.ScannerStatusCallback {
        public boolean expectReconnecting = false;
        public boolean expectDisconnected = false;
        public String expectStatus = "";

        @Override
        public void onStatusChanged(String newStatus) {
            Assert.assertEquals(expectStatus, newStatus);
        }

        @Override
        public void onScannerReconnecting(Scanner s) {
            Assert.assertTrue("Unexpected call to onScannerReconnecting", expectReconnecting);
        }

        @Override
        public void onScannerDisconnected(Scanner s) {
            Assert.assertTrue("Unexpected call to onScannerDisconnected", expectDisconnected);
        }
    }

    // TOOLS
    // Handlers are declared separately to avoid having to re-declare them in every test.
    // Tests need to access their attributes to setup test values, which is not possible when
    // using the regular Scanner.ScannerXCallback interfaces and lambda declaration.

    private static class TestDataCallback implements Scanner.ScannerDataCallback {
        public List<Barcode> expectedData = new ArrayList<>();

        @Override
        public void onData(Scanner s, List<Barcode> data) {
            Assert.assertEquals(expectedData, data);
        }
    }

    private static class TestInitCallback implements Scanner.ScannerInitCallback {
        public boolean expectSuccess = true;

        @Override
        public void onConnectionSuccessful(Scanner s) {
            Assert.assertTrue("Unexpected call to onConnectionSuccessful", expectSuccess);
        }

        @Override
        public void onConnectionFailure(Scanner s) {
            Assert.assertFalse("Unexpected call to onConnectionFailure", expectSuccess);
        }
    }

    private static class TestScannerConnectionHandler implements ScannerConnectionHandler {
        public TestInitCallback initCallback;
        public TestStatusCallback statusCallback;
        public TestDataCallback dataCallback;
        public MockScanner mock = null;

        public TestScannerConnectionHandler(final TestInitCallback initCallback, final TestStatusCallback statusCallback, final TestDataCallback dataCallback) {
            this.initCallback = initCallback;
            this.statusCallback = statusCallback;
            this.dataCallback = dataCallback;
        }

        @Override
        public void scannerConnectionProgress(String providerKey, String scannerKey, String message) {
        }

        @Override
        public void scannerCreated(String providerKey, String scannerKey, Scanner s) {
            if (s instanceof MockScanner) {
                mock = ((MockScanner) s);
                mock.initialize(ctx, initCallback, dataCallback, statusCallback, null);
            }
        }

        @Override
        public void noScannerAvailable() {
        }

        @Override
        public void endOfScannerSearch() {
        }
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
