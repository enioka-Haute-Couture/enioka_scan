package com.enioka.scanner.sdk.zebra;

import android.content.Context;
import android.util.Log;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebra.scannercontrol.RMDAttributes;
import com.zebra.scannercontrol.SDKHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * All the Zebra BT connection logics goes here.
 * This is separated from the {@link BtZebraProvider} class to allow a reflection check before
 * loading the Zebra BT driver, and avoid a nasty stacktrace if the driver is not present.
 */
class BtZebraProviderConnector implements IDcsSdkApiDelegate {
    private static final String LOG_TAG = "BtZebraProvider";

    private SDKHandler sdkHandler;

    private ScannerProvider.ProviderCallback providerCallback;

    private final Set<Integer> configuredScanners = new HashSet<>();

    private final ConcurrentHashMap<Integer, BtZebraScanner> createdScanners = new ConcurrentHashMap<>();


    @SuppressWarnings("unused") // Used through reflection
    public void startScannerSearch(Context ctx, final ScannerProvider.ProviderCallback cb, ScannerSearchOptions options) {
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
            // Connect to available (already paired) scanners
            List<DCSScannerInfo> mScannerInfoList = new ArrayList<>();
            Log.i(LOG_TAG, "dcssdkGetAvailableScannersList :" + sdkHandler.dcssdkGetAvailableScannersList(mScannerInfoList) + " " + mScannerInfoList.size());

            final Semaphore waitingFor = new Semaphore(0);

            for (final DCSScannerInfo scannerInfo : mScannerInfoList) {
                Log.i(LOG_TAG, "Trying to connect to BT device :");
                Log.i(LOG_TAG, " ID: " + scannerInfo.getScannerID());
                Log.i(LOG_TAG, " Name: " + scannerInfo.getScannerName());

                // The stupid dcssdkGetAvailableScannersList API actually lists all paired BT devices. Try and connect to check device is a Zebra device.
                connectTo(scannerInfo.getScannerID(), new BtZebraConnectionCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(LOG_TAG, "A scanner has successfully connected to the Zebra BT driver");
                        BtZebraScanner scanner = new BtZebraScanner(sdkHandler, scannerInfo.getScannerID());
                        createdScanners.put(scannerInfo.getScannerID(), scanner);
                        cb.onScannerCreated(BtZebraProvider.PROVIDER_KEY, BtZebraProvider.PROVIDER_KEY + scannerInfo.getScannerID(), scanner);
                        created.incrementAndGet();
                        waitingFor.release();
                    }

                    @Override
                    public void onFailure() {
                        Log.d(LOG_TAG, "A BT scanner has failed to connect - may not be a Zebra scanner at all");
                        waitingFor.release();
                    }
                });
            }

            try {
                Log.d(LOG_TAG, "Waiting for BT device connections");
                waitingFor.acquire(mScannerInfoList.size());
                Log.d(LOG_TAG, "Provider has finished trying to connect to already paired devices");
            } catch (InterruptedException e) {
                // Who cares
            }
        }

        if (created.get() > 0 || options.allowLaterConnections) {
            cb.onAllScannersCreated(BtZebraProvider.PROVIDER_KEY);
        } else {
            Log.i(LOG_TAG, "No Zebra BT devices connected to this device and master device connection is disabled - disabling Zebra BT SDK");
            cb.onProviderUnavailable(BtZebraProvider.PROVIDER_KEY); // Costly search. We do not want it to do it on each scanner search.
            sdkHandler.dcssdkClose();
        }

        // Master scanner connection
        Log.i(LOG_TAG, "Provider will allow new (BT master) scanner connections: " + options.allowLaterConnections);
        sdkHandler.dcssdkEnableAvailableScannersDetection(options.allowLaterConnections);
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
            this.providerCallback.onScannerCreated(BtZebraProvider.PROVIDER_KEY, BtZebraProvider.PROVIDER_KEY + scannerID, newScanner);
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


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Connection management
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void connectTo(int scannerId, BtZebraConnectionCallback cb) {
        Log.d(LOG_TAG, "Asking driver to connect to device " + scannerId);
        DCSSDKDefs.DCSSDK_RESULT result = sdkHandler.dcssdkEstablishCommunicationSession(scannerId);
        Log.d(LOG_TAG, "Driver has answered for device " + scannerId);

        if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS && !configuredScanners.contains(scannerId)) {
            Log.d(LOG_TAG, "Driver thinks it's a success and scanner is not configured yet");
            String inXML;
            // Set initial settings
            // High volume and medium frequency
            inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-xml><attrib_list>" +
                    "<attribute><id>" + RMDAttributes.RMD_ATTR_BEEPER_FREQUENCY + "</id><datatype>B</datatype>" +
                    "<value>" + RMDAttributes.RMD_ATTR_VALUE_BEEPER_FREQ_MEDIUM + "</value></attribute>"
                    + "</attrib_list></arg-xml></cmdArgs></inArgs>";
            BtZebraAsyncTask.executeCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET, inXML);

            inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-xml><attrib_list>" +
                    "<attribute><id>" + RMDAttributes.RMD_ATTR_BEEPER_VOLUME + "</id><datatype>B</datatype>" +
                    "<value>" + RMDAttributes.RMD_ATTR_VALUE_BEEPER_VOLUME_HIGH + "</value></attribute>" +
                    "</attrib_list></arg-xml></cmdArgs></inArgs>";
            BtZebraAsyncTask.executeCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET, inXML);

            // Set authorized symbologies
            for (int unauthorizedSymbology : BtZebraDataTranslator.unauthorizedSymbologies) {
                inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-xml><attrib_list>";
                inXML += "<attribute><id>" + unauthorizedSymbology + "</id><datatype>F</datatype><value>" + false + "</value></attribute>";
                inXML += "</attrib_list></arg-xml></cmdArgs></inArgs>";
                BtZebraAsyncTask.executeCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET, inXML);
            }

            for (int authorizedSymbology : BtZebraDataTranslator.authorizedSymbologies) {
                inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-xml><attrib_list>";
                inXML += "<attribute><id>" + authorizedSymbology + "</id><datatype>F</datatype><value>" + true + "</value></attribute>";
                inXML += "</attrib_list></arg-xml></cmdArgs></inArgs>";
                BtZebraAsyncTask.executeCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET, inXML);
            }

            configuredScanners.add(scannerId);
            cb.onSuccess();
            return;
        } else if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE) {
            Log.d(LOG_TAG, "Driver thinks it's a failure");
            cb.onFailure();
            return;
        }

        Log.d(LOG_TAG, "Driver thinks connected successfully and already configured");
        cb.onSuccess();
    }
}

