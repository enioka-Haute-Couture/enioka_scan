package com.enioka.scanner.sdk.zebra;

import android.content.Context;
import android.util.Log;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebra.scannercontrol.SDKHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provider for the BT Zebra SDK.
 */
public class BtZebraProvider implements ScannerProvider, IDcsSdkApiDelegate {
    private static final String LOG_TAG = "BtZebraProvider";
    public static final String PROVIDER_KEY = "BtZebraProvider";

    private ConcurrentHashMap<Integer, BtZebraScanner> createdScanners = new ConcurrentHashMap<>();
    private ProviderCallback providerCallback;
    private SDKHandler sdkHandler;

    @Override
    public void getScanner(Context ctx, final ProviderCallback cb, ScannerSearchOptions options) {
        Log.i(LOG_TAG, "Starting scanner search");
        try {
            this.getClass().getClassLoader().loadClass("com.zebra.scannercontrol.SDKHandler");
        } catch (ClassNotFoundException e) {
            cb.onProviderUnavailable(PROVIDER_KEY);
        }

        this.providerCallback = cb;

        sdkHandler = new SDKHandler(ctx);
        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);

        // Subscribe to SDK events
        sdkHandler.dcssdkSetDelegate(this);
        int notificationsMask = 0;

        // We would like to subscribe to all scanner available/not-available events
        notificationsMask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value;
        // We would like to subscribe to all scanner connection events
        notificationsMask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value;
        // We would like to subscribe to all barcode events
        notificationsMask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value;
        // subscribe to events set in notification mask
        sdkHandler.dcssdkSubsribeForEvents(notificationsMask); // | 20 | 21


        final AtomicInteger created = new AtomicInteger(0);
        if (options.allowInitialSearch) {
            // Connect to available scanners
            List<DCSScannerInfo> mScannerInfoList = new ArrayList<>();
            Log.i(LOG_TAG, "dcssdkGetAvailableScannersList :" + sdkHandler.dcssdkGetAvailableScannersList(mScannerInfoList) + " " + mScannerInfoList.size());

            final Semaphore waitingFor = new Semaphore(0);

            for (final DCSScannerInfo scannerInfo : mScannerInfoList) {
                Log.i(LOG_TAG, "Trying to connect to BT device :");
                Log.i(LOG_TAG, " ID :" + scannerInfo.getScannerID());
                Log.i(LOG_TAG, " Name :" + scannerInfo.getScannerName());

                // The stupid dcssdkGetAvailableScannersList API actually lists all paired BT devices. Try and connect to check device is a Zebra device.
                new BtZebraConnectScannerAsync(new BtZebraConnectScannerAsync.ConnectionCallback() {
                    @Override
                    public void onSuccess() {
                        BtZebraScanner scanner = new BtZebraScanner(sdkHandler, scannerInfo.getScannerID());
                        createdScanners.put(scannerInfo.getScannerID(), scanner);
                        cb.onScannerCreated(getKey(), getKey() + scannerInfo.getScannerID(), scanner);
                        created.incrementAndGet();
                        waitingFor.release();
                    }

                    @Override
                    public void onFailure() {
                        waitingFor.release();
                    }
                }, sdkHandler, scannerInfo.getScannerID()).execute();
            }

            try {
                waitingFor.acquire(mScannerInfoList.size());
            } catch (InterruptedException e) {
                // Who cares
            }
        }

        if (created.get() > 0 || options.allowLaterConnections) {
            cb.onAllScannersCreated(getKey());
        } else {
            Log.i(LOG_TAG, "No Zebra BT devices connected to this device and master device connection is disabled - disabling Zebra BT SDK");
            cb.onProviderUnavailable(PROVIDER_KEY); // Costly search. We do not want it to do it on each scanner search.
            sdkHandler.dcssdkClose();
        }

        // Master scanner connection
        if (options.allowLaterConnections) {
            sdkHandler.dcssdkEnableAvailableScannersDetection(true);
        } else {
            sdkHandler.dcssdkEnableAvailableScannersDetection(false);
        }
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Driver callbacks
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * "Barcode Event" notification informs about reception of a particular barcode of a particular type from a particular active scanner.
     */
    @Override
    public void dcssdkEventBarcode(byte[] barcodeData, int barcodeType, int fromScannerID) {
        BtZebraScanner scanner = createdScanners.get(fromScannerID);
        if (scanner != null) {
            scanner.onData(barcodeData, barcodeType);
        }
    }

    /**
     * "Session Established" notification informs about appearance of a particular active scanner.
     */
    @Override
    public void dcssdkEventCommunicationSessionEstablished(DCSScannerInfo activeScanner) {
        Log.i(LOG_TAG, "dcssdkEventCommunicationSessionEstablished");
        int scannerID = activeScanner.getScannerID();
        BtZebraScanner existingScanner = createdScanners.get(scannerID);

        if (existingScanner != null) {
            existingScanner.reconnected();
        } else {
            BtZebraScanner newScanner = new BtZebraScanner(sdkHandler, scannerID);
            createdScanners.put(scannerID, newScanner);
            this.providerCallback.onScannerCreated(getKey(), getKey() + scannerID, newScanner);
        }
    }

    /**
     * "Session Terminated" notification informs about disappearance of a particular active scanner
     */
    @Override
    public void dcssdkEventCommunicationSessionTerminated(int scannerID) {
        Log.i(LOG_TAG, "dcssdkEventCommunicationSessionTerminated");
        if (createdScanners.containsKey(scannerID) && createdScanners.get(scannerID) != null) {
            createdScanners.get(scannerID).disconnected();
        }
    }

    /**
     * "Firmware Update Event" notification informs about status in firmware update parse
     */
    @Override
    public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent firmwareUpdateEvent) {
        Log.i(LOG_TAG, "dcssdkEventFirmwareUpdate");
    }

    @Override
    public void dcssdkEventAuxScannerAppeared(DCSScannerInfo dcsScannerInfo, DCSScannerInfo dcsScannerInfo1) {
        Log.i(LOG_TAG, "dcssdkEventAuxScannerAppeared");
    }

    /**
     * "Image Event" notification is triggered when an active imaging scanner captures images in image mode.
     */
    @Override
    public void dcssdkEventImage(byte[] imageData, int fromScannerID) {
        Log.i(LOG_TAG, "dcssdkEventImage");
    }

    /**
     * "Device Arrival" notification informs about appearance of a particular available scanner.
     */
    @Override
    public void dcssdkEventScannerAppeared(DCSScannerInfo availableScanner) {
        Log.i(LOG_TAG, "dcssdkEventScannerAppeared");
    }

    /**
     * "Device Disappeared" notification informs about disappearance of a particular available scanner.
     */
    @Override
    public void dcssdkEventScannerDisappeared(int scannerID) {
        Log.i(LOG_TAG, "dcssdkEventScannerDisappeared");
    }

    /**
     * "Video Event" notification is triggered when an active imaging scanner captures video in video mode
     */
    @Override
    public void dcssdkEventVideo(byte[] videoFrame, int fromScannerID) {
        Log.i(LOG_TAG, "dcssdkEventVideo");
    }

    @Override
    public void dcssdkEventBinaryData(byte[] bytes, int i) {
        Log.i(LOG_TAG, "dcssdkEventBinaryData");
    }

}
