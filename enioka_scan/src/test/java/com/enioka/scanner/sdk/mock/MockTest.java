package com.enioka.scanner.sdk.mock;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerConnectionHandler;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MockTest {

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

    // TOOLS
    // Callbacks are declared separately to avoid having to re-declare them in every test.
    // Tests need to access their attributes to setup test values, which is not possible when
    // using the regular Scanner.ScannerXCallback interfaces and lambda declaration.

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
                mock.initialize(null, initCallback, dataCallback, statusCallback, null);
            }
        }

        @Override
        public void noScannerAvailable() {
        }

        @Override
        public void endOfScannerSearch() {
        }
    }

    private TestScannerConnectionHandler setupMock() {
        final TestInitCallback initCallback = new TestInitCallback();
        final TestStatusCallback statusCallback = new TestStatusCallback();
        final TestDataCallback dataCallback = new TestDataCallback();
        final TestScannerConnectionHandler handler = new TestScannerConnectionHandler(initCallback, statusCallback, dataCallback);

        handler.scannerCreated("MockProvider", "Mock", new MockScanner());
        return handler;
    }
}
