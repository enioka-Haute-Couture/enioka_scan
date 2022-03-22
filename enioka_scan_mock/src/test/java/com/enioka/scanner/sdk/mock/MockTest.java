package com.enioka.scanner.sdk.mock;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerStatusCallback;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests that the mock correctly reacts to inputs by calling the appropriate callbacks
 */
public class MockTest {

    @Test
    public void testOnDataCallback(){
        final List<Barcode> expectedData = new ArrayList<>();

        final Scanner.ScannerInitCallback initCallback = new Scanner.ScannerInitCallback() {
            @Override
            public void onConnectionSuccessful(Scanner s) {}
            @Override
            public void onConnectionFailure(Scanner s) {
                Assert.fail("Failed to connect to the scanner");
            }
        };
        final ScannerStatusCallback statusCallback = (scanner, newStatus) -> {};
        final Scanner.ScannerDataCallback dataCallback = (s, data) -> Assert.assertEquals(expectedData, data);

        final Barcode barcode = new Barcode("1234ABCD", BarcodeType.CODE39);
        final MockScanner mock = new MockScanner();

        mock.initialize(null, initCallback, dataCallback, statusCallback, null);

        expectedData.add(barcode);
        mock.scan(barcode);
    }

    @Test
    public void testStatusChanges(){
        final List<Barcode> expectedData = new ArrayList<>();
        final List<ScannerStatusCallback.Status> expectedStatus = new ArrayList<ScannerStatusCallback.Status>() {{ add(ScannerStatusCallback.Status.UNKNOWN); }}; // Ugly way to allow expectedStatus to be used in lambdas below.

        final Scanner.ScannerInitCallback initCallback = new Scanner.ScannerInitCallback() {
            @Override
            public void onConnectionSuccessful(Scanner s) {}
            @Override
            public void onConnectionFailure(Scanner s) {
                Assert.fail("Failed to connect to the scanner");
            }
        };
        final ScannerStatusCallback statusCallback = (scanner, newStatus) -> Assert.assertEquals(expectedStatus.get(0), newStatus);
        final Scanner.ScannerDataCallback dataCallback = (s, data) -> Assert.assertEquals(expectedData, data);

        final Barcode barcode = new Barcode("1234ABCD", BarcodeType.CODE39);
        final MockScanner mock = new MockScanner();

        expectedStatus.set(0, ScannerStatusCallback.Status.READY);
        mock.initialize(null, initCallback, dataCallback, statusCallback, null);

        expectedStatus.set(0, ScannerStatusCallback.Status.PAUSED);
        mock.pause();

        try {
            mock.scan(barcode);
            Assert.fail();
        } catch (RuntimeException e) {
            Assert.assertEquals("Received barcode while the scanner was paused", e.getMessage());
        }

        expectedStatus.set(0, ScannerStatusCallback.Status.READY);
        mock.resume();

        try {
            expectedData.add(barcode);
            mock.scan(barcode);
            expectedData.remove(0);
        } catch (RuntimeException e) {
            e.printStackTrace();
            Assert.fail();
        }

        expectedStatus.set(0, ScannerStatusCallback.Status.DISCONNECTED);
        mock.disconnect();

        try {
            mock.scan(barcode);
            Assert.fail();
        } catch (RuntimeException e) {
            Assert.assertEquals("Received barcode while the scanner was disconnected", e.getMessage());
        }
    }
}
