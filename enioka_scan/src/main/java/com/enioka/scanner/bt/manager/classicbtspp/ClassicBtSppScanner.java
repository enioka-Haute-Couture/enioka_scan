package com.enioka.scanner.bt.manager.classicbtspp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.bt.api.ParsingResult;
import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.bt.manager.SerialBtScannerProvider;
import com.enioka.scanner.bt.manager.common.DataSubscription;
import com.enioka.scanner.bt.manager.common.OnConnectedCallback;
import com.enioka.scanner.bt.manager.common.ScannerInternal;
import com.enioka.scanner.bt.manager.common.SerialBtScannerPassiveConnectionManager;
import com.enioka.scanner.bt.manager.data.BtConnectionType;
import com.enioka.scanner.sdk.zebraoss.SsiParser;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Internal class used as the main interaction entry point for bluetooth devices. It handles all connection/reconnection boilerplate and delegates level 7 stuff to a {@link ScannerDataParser}.
 */
public class ClassicBtSppScanner implements Closeable, ScannerInternal {
    private static final String LOG_TAG = "BtSppSdk";

    private final SerialBtScannerPassiveConnectionManager parentProvider;
    private BtSppScannerProvider scannerDriver;

    private final BluetoothDevice rawDevice;
    private ClassicBtConnectToDeviceThread connectionThread;
    private Timer timeoutHunter;

    private final String name;
    private boolean masterBtDevice = false;

    private final int maxReconnectionAttempts = 60;
    private final int reconnectionIntervalMs = 1000;
    private int connectionFailures = 0;

    private BluetoothSocket clientSocket;
    private ClassicBtSocketStreamReader inputStreamReader;
    private ClassicBtSocketStreamWriter outputStreamWriter;

    private ScannerDataParser inputHandler;

    private SppScannerStatusCallback statusCallback;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    /**
     * All the callbacks which are registered to run on received data (post-parsing). Key is data class name.
     */
    private final Map<String, DataSubscription> dataSubscriptions = new HashMap<>();

    /**
     * Create an <strong>unconnected</strong> device from a cached device definition.<br>
     * Need to call {@link #connect(OnConnectedCallback)} before any interaction with the device.<br>
     * Used for slave BT devices.
     *
     * @param device a device definition
     */
    public ClassicBtSppScanner(SerialBtScannerPassiveConnectionManager parentProvider, BluetoothDevice device) {
        this.rawDevice = device;
        this.parentProvider = parentProvider;
        this.name = this.rawDevice.getName();
        this.inputHandler = new SsiParser();
        this.setUpTimeoutTimer();
    }

    /**
     * Create a <strong>connected</strong> device from an already open bluetooth socket.<br>
     * Used for master BT devices.
     *
     * @param socket a socket to a bluetooth device.
     */
    ClassicBtSppScanner(SerialBtScannerProvider parentProvider, BluetoothSocket socket) {
        this.rawDevice = socket.getRemoteDevice();
        this.parentProvider = parentProvider;
        this.name = this.rawDevice.getName();
        this.inputHandler = new SsiParser();
        this.masterBtDevice = true;

        this.clientSocket = socket;
        this.connectStreams();
        this.setUpTimeoutTimer();
        Log.i(LOG_TAG, "Device " + ClassicBtSppScanner.this.name + " reports it is connected");
    }

    /**
     * Start a timer which checks if data subscribers are timed-out and deals with the consequences.
     */
    private void setUpTimeoutTimer() {
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
                            outputStreamWriter.endOfCommand();
                        }
                    }

                    for (String key : toRemove) {
                        dataSubscriptions.remove(key);
                    }
                }
            }
        }, 0, 100);
    }

    public void setProvider(BtSppScannerProvider provider) {
        this.scannerDriver = provider;
        this.inputHandler = scannerDriver.getInputHandler();
    }

    public void connect(final OnConnectedCallback callback) {
        Log.i(LOG_TAG, "Starting connection to device " + ClassicBtSppScanner.this.name);
        connectionThread = new ClassicBtConnectToDeviceThread(rawDevice, new OnStreamConnectedCallback() {
            @Override
            public void connected(BluetoothSocket bluetoothSocket) {
                ClassicBtSppScanner.this.connectionThread = null;
                Log.i(LOG_TAG, "Device " + ClassicBtSppScanner.this.name + " reports it is connected");
                ClassicBtSppScanner.this.clientSocket = bluetoothSocket;
                connectStreams();

                if (callback != null) {
                    callback.connected(ClassicBtSppScanner.this);
                }
            }

            @Override
            public void failed() {
                if (callback != null) {
                    callback.failed();
                }
            }
        });
        connectionThread.start();
    }

    @Override
    public void disconnect() {
        this.close();
    }

    private void connectStreams() {
        if (this.clientSocket == null) {
            return;
        }

        try {
            this.inputStreamReader = new ClassicBtSocketStreamReader(this.clientSocket.getInputStream(), this);
            this.inputStreamReader.start();

            this.outputStreamWriter = new ClassicBtSocketStreamWriter(this.clientSocket.getOutputStream(), this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (this.connectionThread != null) {
            this.connectionThread.cancel();
            this.connectionThread = null;
        }

        this.closeStreams();

        if (this.timeoutHunter != null) {
            this.timeoutHunter.cancel();
            this.timeoutHunter = null;
        }
    }

    private void closeStreams() {
        if (this.inputStreamReader != null) {
            try {
                this.inputStreamReader.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not close reader", e);
            }
        }
        if (this.outputStreamWriter != null) {
            try {
                this.outputStreamWriter.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not close writer", e);
            }
        }
        if (this.clientSocket != null) {
            try {
                this.clientSocket.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not close socket", e);
            }
        }
    }

    void onConnectionFailure() {
        Log.w(LOG_TAG, "A connection to an SPP BT device was lost");

        this.closeStreams();

        if (this.masterBtDevice) {
            Log.w(LOG_TAG, "Reconnection will not be attempted as it was a master device - the device itself should reconnect.");
            this.parentProvider.resetListener(BluetoothAdapter.getDefaultAdapter());
            if (this.statusCallback != null) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ClassicBtSppScanner.this.statusCallback.onScannerDisconnected();
                    }
                });
            }
            return;
        }

        Log.w(LOG_TAG, "This is a slave device, attempting reconnection");
        if (this.statusCallback != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    ClassicBtSppScanner.this.statusCallback.onScannerReconnecting();
                }
            });
        }

        // We choose to disable master connection if socket is still open - that way devices which are both master and slave will not reconnect the "wrong" way.
        this.parentProvider.stopMasterListener();

        // Go for reconnection loop.
        this.reconnect();
    }

    private void reconnect() {
        Log.w(LOG_TAG, "Reconnection attempt " + (this.connectionFailures + 1) + " out of " + this.maxReconnectionAttempts);

        // Always sleeps first (and not only in case of failure) as sockets take time to close for small ring scanners.
        try {
            Thread.sleep(reconnectionIntervalMs); // Allowed: we are in a dedicated thread which has nothing to do anyway.
        } catch (InterruptedException e) {
            // Ignore.
        }

        connectionThread = new ClassicBtConnectToDeviceThread(rawDevice, new OnStreamConnectedCallback() {
            @Override
            public void connected(BluetoothSocket bluetoothSocket) {
                ClassicBtSppScanner.this.connectionThread = null;
                Log.i(LOG_TAG, "Device " + ClassicBtSppScanner.this.name + " reports it has reconnected");
                ClassicBtSppScanner.this.connectionFailures = 0;
                ClassicBtSppScanner.this.clientSocket = bluetoothSocket;
                connectStreams();

                if (ClassicBtSppScanner.this.statusCallback != null) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ClassicBtSppScanner.this.statusCallback.onScannerConnected();
                        }
                    });
                }
            }

            @Override
            public void failed() {
                ClassicBtSppScanner.this.connectionFailures++;

                if (ClassicBtSppScanner.this.connectionFailures < ClassicBtSppScanner.this.maxReconnectionAttempts) {
                    ClassicBtSppScanner.this.reconnect();
                } else {
                    Log.w(LOG_TAG, "Giving up on dead scanner " + ClassicBtSppScanner.this.name);
                    if (ClassicBtSppScanner.this.statusCallback != null) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ClassicBtSppScanner.this.statusCallback.onScannerDisconnected();
                            }
                        });
                    }
                }
            }
        });
        connectionThread.start();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public BtConnectionType getConnectionType() {
        return BtConnectionType.CLASSIC_SLAVE;
    }

    @Override
    public <T> void runCommand(Command<T> command, DataSubscriptionCallback<T> subscription) {
        byte[] cmd = command.getCommand();

        if (subscription != null) {
            synchronized (dataSubscriptions) {
                String expectedDataClass = command.getReturnType().getCanonicalName();
                this.dataSubscriptions.put(expectedDataClass, new DataSubscription(subscription, command.getTimeOut(), false));
            }
        } else {
            // Nothing is expected in return, so no need to wait before running the next command.
            this.outputStreamWriter.endOfCommand();
        }

        Log.d(LOG_TAG, "Queuing for dispatch command " + command.getClass().getSimpleName());
        this.outputStreamWriter.write(cmd);
    }

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
        this.statusCallback = statusCallback;
    }

    void handleInputBuffer(byte[] buffer, int offset, int length) {
        int read = offset;
        while (read < length) {
            read = handleInputBufferLoop(buffer, read, length) + 1;
            if (read < length) {
                Log.d(LOG_TAG, "The buffer contains multiple tokens - a loop will happen starting at position " + read + " until " + length);
            }
        }
    }

    private int handleInputBufferLoop(byte[] buffer, int offset, int length) {
        ParsingResult res = this.inputHandler.parse(buffer, offset, length);
        if (!res.expectingMoreData && res.data != null) {
            Log.d(LOG_TAG, "Data was interpreted as: " + res.data.toString());

            // ACK first - the event handlers may write to stream and create out of order ACKs.
            if (res.acknowledger != null) {
                this.outputStreamWriter.endOfCommand();
                this.outputStreamWriter.write(res.acknowledger.getCommand(), true);
            }

            // Subscriptions to fulfill on that data type?
            synchronized (dataSubscriptions) {
                if (this.dataSubscriptions.containsKey(res.data.getClass().getCanonicalName())) {
                    DataSubscription subscription = this.dataSubscriptions.get(res.data.getClass().getCanonicalName());

                    // Remove callback before calling it - that way callbacks can re-subscribe at once.
                    if (!subscription.isPermanent()) {
                        this.dataSubscriptions.remove(res.data.getClass().getCanonicalName());
                    }

                    DataSubscriptionCallback callback = subscription.getCallback();
                    callback.onSuccess(res.data);
                }
            }

            this.outputStreamWriter.endOfCommand();
        } else if (!res.expectingMoreData && !res.rejected) {
            Log.d(LOG_TAG, "Message was interpreted as: message without additional data");
            if (res.acknowledger != null) {
                this.outputStreamWriter.write(res.acknowledger.getCommand(), true);
            }
            this.outputStreamWriter.endOfCommand();
        } else if (!res.expectingMoreData && res.rejected) {
            Log.d(LOG_TAG, "Message was rejected " + res.result);
            if (res.acknowledger != null) {
                this.outputStreamWriter.write(res.acknowledger.getCommand(), true);
            }
            this.outputStreamWriter.endOfCommand();
        } else {
            Log.d(LOG_TAG, "Data was not interpreted yet as we are expecting more data");
            if (res.acknowledger != null) {
                this.outputStreamWriter.write(res.acknowledger.getCommand(), true);
            }
        }

        return res.read;
    }
}
