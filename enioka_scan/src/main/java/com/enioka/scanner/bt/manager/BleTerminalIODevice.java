package com.enioka.scanner.bt.manager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.bt.api.Helpers;
import com.enioka.scanner.bt.api.ParsingResult;
import com.enioka.scanner.bt.api.ScannerDataParser;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

class BleTerminalIODevice implements BleStateMachineDevice, ScannerInternal, Closeable {
    private static final String LOG_TAG = "BtSppSdk";

    private Context ctx;
    private BleStateMachineGattCallback gattCallback;

    // Device
    private BluetoothDevice btDevice;
    private BluetoothGatt gatt;
    private String deviceName;

    private BleTerminalIOStreamWriter writer;

    // Credits
    private Semaphore clientCredits = new Semaphore(0);
    private Semaphore serverCredits = new Semaphore(0);
    private static final byte MIN_SERVER_CREDITS = 20;
    private static final byte SERVER_CREDITS_ALLOCATION = 40;

    @Override
    public void close() {
        this.disconnect();

        if (this.timeoutHunter != null) {
            this.timeoutHunter.cancel();
            this.timeoutHunter = null;
        }
    }

    // State
    private enum TioState {
        INITIAL, SUBSCRIBED_TO_CREDIT, SUBSCRIBED_TO_DATA, READY
    }

    private TioState currentState = TioState.INITIAL;


    // Relations with providers
    /**
     * The scanner driver.
     */
    private BtSppScannerProvider scannerProvider;
    /**
     * All the callbacks which are registered to run on received data (post-parsing). Key is data class name.
     */
    private final Map<String, DataSubscription> dataSubscriptions = new HashMap<>();

    /**
     * The (provider-supplied) parser used to cut the data given by the scanner into objects.
     */
    private ScannerDataParser inputHandler;

    // Misc.
    private Timer timeoutHunter;

    /**
     * Create a new TIO device from the given BT device. This does not attempt to connect to anything - this is done in the connect method.
     *
     * @param ctx      a valid context (application, service, activity...)
     * @param btDevice the BT device to encapsulate.
     */
    BleTerminalIODevice(Context ctx, BluetoothDevice btDevice) {
        this.ctx = ctx;
        this.btDevice = btDevice;
        this.deviceName = btDevice.getName();
    }

    public void connect(final ClassicBtConnectToDeviceThread.OnConnectedCallback callback) {
        if (btDevice.getType() != BluetoothDevice.DEVICE_TYPE_LE && btDevice.getType() != BluetoothDevice.DEVICE_TYPE_DUAL) {
            Log.i(LOG_TAG, "Trying to connect to GATT with a non-BLE device " + this.deviceName);
            callback.failed();
            return;
        }

        Log.i(LOG_TAG, "Starting connection to device " + this.deviceName);

        gattCallback = new BleStateMachineGattCallback(this, new BleStateMachineGattCallback.OnConnectedCallback() {
            @Override
            public void onConnected(BluetoothGatt gatt) {
                Log.i(LOG_TAG, "Device " + BleTerminalIODevice.this.deviceName + " reports it is connected to its GATT server");

                if (gatt.getService(GattAttribute.TERMINAL_IO_SERVICE.id) == null) {
                    if (callback != null) {
                        callback.failed();
                    }

                    Log.i(LOG_TAG, "Trying to connect to TIO on a BLE device which does not have the TIO service " + BleTerminalIODevice.this.deviceName);
                    return;
                }

                // init the wrapper.
                BleTerminalIODevice.this.writer = new BleTerminalIOStreamWriter(BleTerminalIODevice.this, gatt);
                BleTerminalIODevice.this.gatt = gatt;

                BleTerminalIODevice.this.setUpTimeoutTimer();

                // warn caller - we are connected OK to the GATT server.
                if (callback != null) {
                    callback.connected(BleTerminalIODevice.this);
                }
            }

            @Override
            public void onConnectionFailure() {
                if (callback != null) {
                    callback.failed();
                }
            }
        });
        btDevice.connectGatt(ctx, true, gattCallback);
    }

    @Override
    public void setProvider(BtSppScannerProvider provider) {
        this.scannerProvider = provider;
        this.inputHandler = this.scannerProvider.getInputHandler();
    }

    static boolean isCompatibleWith(BluetoothGatt gatt) {
        return gatt.getService(GattAttribute.TERMINAL_IO_SERVICE.id) != null;
    }


    @Override
    public void onEvent(BleEvent event) {
        // State 0 is: has just been retrieved from BT stack.

        // State 0
        BluetoothGattService service = gatt.getService(GattAttribute.TERMINAL_IO_SERVICE.id);
        if (service == null) {
            Log.w(LOG_TAG, "A device was created before service discovery - likely a bug. Restarting discovery.");
            gatt.discoverServices();
            return;
        }

        // Advance state according to event.
        if (event.nature.equals(BleEventNature.RESET)) {
            currentState = TioState.INITIAL;
            clientCredits.drainPermits();
            serverCredits.drainPermits();
        }
        switch (currentState) {
            case INITIAL:
                if (event.nature == BleEventNature.DESCRIPTOR_WRITE_SUCCESS && event.parentAttribute == GattAttribute.TERMINAL_IO_UART_CREDITS_TX) {
                    currentState = TioState.SUBSCRIBED_TO_CREDIT;
                }
                break;
            case SUBSCRIBED_TO_CREDIT:
                if (event.nature == BleEventNature.DESCRIPTOR_WRITE_SUCCESS && event.parentAttribute == GattAttribute.TERMINAL_IO_UART_DATA_TX) {
                    currentState = TioState.SUBSCRIBED_TO_DATA;
                }
                break;
            case SUBSCRIBED_TO_DATA:
                if (event.nature == BleEventNature.CHARACTERISTIC_WRITE_SUCCESS && event.targetAttribute == GattAttribute.TERMINAL_IO_UART_CREDITS_RX) {
                    currentState = TioState.READY;
                }
                break;
            case READY:
                // Nothing to do.
                break;
        }


        // Trigger specific actions on each state in order to progress inside the TIO lifecycle.
        switch (currentState) {
            case INITIAL:
                BleHelpers.subscribeToCharacteristic(gatt, service, GattAttribute.TERMINAL_IO_UART_CREDITS_TX, BleSubscriptionType.INDICATION);
                return;
            case SUBSCRIBED_TO_CREDIT:
                BleHelpers.subscribeToCharacteristic(gatt, service, GattAttribute.TERMINAL_IO_UART_DATA_TX, BleSubscriptionType.NOTIFICATION);
                return;
            case SUBSCRIBED_TO_DATA:
                sendCreditsToServerIfNeeded(gatt);
                return;
            case READY:
                sendCreditsToServerIfNeeded(gatt);
                if (event.nature == BleEventNature.CHARACTERISTIC_CHANGED_SUCCESS && event.targetAttribute == GattAttribute.TERMINAL_IO_UART_DATA_TX) {
                    handleData(event);
                }
                if (event.nature == BleEventNature.CHARACTERISTIC_CHANGED_SUCCESS && event.targetAttribute == GattAttribute.TERMINAL_IO_UART_CREDITS_TX) {
                    handleCredit(event);
                }
                break;
        }
    }

    private void handleData(BleEvent event) {
        Log.i(LOG_TAG, "Received data " + new String(event.data, StandardCharsets.UTF_8) + " - " + Helpers.byteArrayToHex(event.data, event.data.length));

        // Sanity checks
        if (!serverCredits.tryAcquire()) {
            Log.w(LOG_TAG, "Weirdly the scanner has answered without having enough credits. May sbe a lib or a scanner bug");
            return;
        }

        if (event.data == null || event.data.length == 0) {
            Log.w(LOG_TAG, "Weirdly the scanner has sent a data event... without any data");
            return;
        }

        // Parse data
        ParsingResult res = this.inputHandler.parse(event.data, 0, event.data.length);
        if (!res.expectingMoreData && res.data != null) {
            Log.d(LOG_TAG, "Data was interpreted as: " + res.data.toString());

            // Subscriptions to fulfill on that data type?
            synchronized (dataSubscriptions) {
                if (this.dataSubscriptions.containsKey(res.data.getClass().getCanonicalName())) {
                    DataSubscription subscription = this.dataSubscriptions.get(res.data.getClass().getCanonicalName());
                    if (subscription == null) {
                        throw new IllegalStateException("stored subscription cannot be null");
                    }
                    DataSubscriptionCallback callback = subscription.getCallback();
                    callback.onSuccess(res.data);

                    if (!subscription.isPermanent()) {
                        this.dataSubscriptions.remove(res.data.getClass().getCanonicalName());
                    }
                }
            }

            if (res.acknowledger != null) {
                this.writer.endOfCommand();
                this.writer.write(res.acknowledger.getCommand(), false);
            }

            this.writer.endOfCommand();
        } else if (!res.expectingMoreData && !res.rejected) {
            Log.d(LOG_TAG, "Message was interpreted as: message without additional data");
            if (res.acknowledger != null) {
                this.writer.write(res.acknowledger.getCommand(), false);
            }
            this.writer.endOfCommand();
        } else if (!res.expectingMoreData) {
            Log.d(LOG_TAG, "Message was rejected " + res.result);
            if (res.acknowledger != null) {
                this.writer.write(res.acknowledger.getCommand(), false);
            }
            this.writer.endOfCommand();
        } else {
            Log.d(LOG_TAG, "Data was not interpreted yet as we are expecting more data");
            if (res.acknowledger != null) {
                this.writer.write(res.acknowledger.getCommand(), false);
            }
        }
    }

    private void handleCredit(BleEvent event) {
        Log.i(LOG_TAG, "Received client credits " + event.data[0]);
        clientCredits.release(event.data[0]);
    }

    private void sendCreditsToServerIfNeeded(BluetoothGatt gatt) {
        if (serverCredits.availablePermits() >= MIN_SERVER_CREDITS) {
            return;
        }

        BluetoothGattService service = gatt.getService(GattAttribute.TERMINAL_IO_SERVICE.id);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(GattAttribute.TERMINAL_IO_UART_CREDITS_RX.id);
        if (characteristic != null) {
            Log.i(LOG_TAG, "Sending " + SERVER_CREDITS_ALLOCATION + " credits to remote device " + this.deviceName);
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            characteristic.setValue(new byte[]{SERVER_CREDITS_ALLOCATION});
            gatt.writeCharacteristic(characteristic);
            serverCredits.release(SERVER_CREDITS_ALLOCATION);
        }
    }

    boolean waitForClientCredits(int creditCount) {
        try {
            this.clientCredits.acquire(creditCount);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * Start a timer which checks if data subscribers are timed-out and deals with the consequences.
     */
    private void setUpTimeoutTimer() {
        if (this.timeoutHunter != null) {
            return;
        }

        this.timeoutHunter = new Timer();
        this.timeoutHunter.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (dataSubscriptions) {
                    List<String> toRemove = new ArrayList<>(0);
                    for (Map.Entry<String, DataSubscription> entry : dataSubscriptions.entrySet()) {
                        DataSubscription subscription = entry.getValue();
                        String expectedDataClass = entry.getKey();

                        if (subscription.isTimedOut()) {
                            Log.d(LOG_TAG, "A data subscription has timed out");
                            toRemove.add(expectedDataClass);
                            subscription.getCallback().onTimeout();
                            writer.endOfCommand();
                        }
                    }

                    for (String key : toRemove) {
                        dataSubscriptions.remove(key);
                    }
                }
            }
        }, 0, 100);
    }

    public <T> void runCommand(Command<T> command, DataSubscriptionCallback<T> subscription) {
        byte[] cmd = command.getCommand();

        if (subscription != null) {
            synchronized (dataSubscriptions) {
                String expectedDataClass = command.getReturnType().getCanonicalName();
                this.dataSubscriptions.put(expectedDataClass, new DataSubscription(subscription, command.getTimeOut(), false));
            }
        } else {
            // Nothing is expected in return, so no need to wait before running the next command.
            this.writer.endOfCommand();
        }

        Log.d(LOG_TAG, "Queuing for dispatch command " + command.getClass().getSimpleName());
        this.writer.write(cmd, true);
    }

    @Override
    public String getName() {
        return this.deviceName;
    }

    @Override
    public void disconnect() {
        if (gatt != null) {
            this.gatt.disconnect();
            this.gatt = null;
            this.gattCallback = null;
        }
    }

    @Override
    public <T> void registerSubscription(DataSubscriptionCallback<T> subscription, Class<? extends T> targetType) {
        if (subscription != null) {
            synchronized (dataSubscriptions) {
                String expectedDataClass = targetType.getCanonicalName();
                this.dataSubscriptions.put(expectedDataClass, new DataSubscription(subscription, 0, true));
            }
        }
    }

    @Override
    public void registerStatusCallback(SppScannerStatusCallback statusCallback) {

    }
}
