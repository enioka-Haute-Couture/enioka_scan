package com.enioka.scanner.sdk.mock;

import android.content.Context;

import com.enioka.scanner.LaserScanner;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerConnectionHandler;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.service.ScannerService;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MockTest {

    private static Context ctx = null;

    //@Test // Requires a way to mock an application context
    public void testMockRetrievableByLaserScanner(){
        final ScannerSearchOptions options = ScannerSearchOptions.defaultOptions();
        final TestInitCallback initCallback = new TestInitCallback();
        final TestStatusCallback statusCallback = new TestStatusCallback();
        final TestDataCallback dataCallback = new TestDataCallback();
        final TestScannerConnectionHandler handler = new TestScannerConnectionHandler(initCallback, statusCallback, dataCallback);
        LaserScanner.getLaserScanner(ctx, handler, options);
    }

    //@Test // Requires a way to mock the application context
    public void testMockRetrievableByScannerService(){
        final ScannerService scannerService = new ScannerService();
        scannerService.restartScannerDiscovery();

        final List<Scanner> scanners = scannerService.getConnectedScanners();
        Assert.assertEquals(1, scanners.size());
        Assert.assertTrue(scanners.get(1) instanceof MockScanner);
    }

    @Test
    public void testStatusCallbacks(){
        final TestScannerConnectionHandler handler = setupMock();

        handler.statusCallback.expectStatus = "Paused";
        handler.mock.pause();
        handler.statusCallback.expectStatus = "Resumed";
        handler.mock.resume();
        handler.statusCallback.expectDisconnected = true;
        handler.mock.disconnect();
    }

    @Test
    public void testOnDataCallback(){
        final TestScannerConnectionHandler handler = setupMock();

        final Barcode barcode = new Barcode("1234ABCD", BarcodeType.CODE39);
        handler.dataCallback.expectedData.add(barcode);
        handler.mock.scan(barcode);
    }

    private class TestStatusCallback implements Scanner.ScannerStatusCallback {
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

    private class TestDataCallback implements Scanner.ScannerDataCallback {
        public List<Barcode> expectedData = new ArrayList<>();

        @Override
        public void onData(Scanner s, List<Barcode> data) {
            Assert.assertEquals(expectedData, data);
        }
    }

    private class TestInitCallback implements Scanner.ScannerInitCallback {
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

    private class TestScannerConnectionHandler implements ScannerConnectionHandler {
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
            if (mock == null)
                Assert.fail("Mock Scanner was not found");
        }
    }

    private TestScannerConnectionHandler setupMock() {
        final ScannerSearchOptions options = ScannerSearchOptions.defaultOptions();
        final TestInitCallback initCallback = new TestInitCallback();
        final TestStatusCallback statusCallback = new TestStatusCallback();
        final TestDataCallback dataCallback = new TestDataCallback();
        final TestScannerConnectionHandler handler = new TestScannerConnectionHandler(initCallback, statusCallback, dataCallback);

        handler.scannerCreated("MockProvider", "Mock", new MockScanner());
        return handler;
    }
}
