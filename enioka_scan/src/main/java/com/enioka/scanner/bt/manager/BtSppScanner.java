package com.enioka.scanner.bt.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.bt.api.ParsingResult;
import com.enioka.scanner.bt.api.Scanner;
import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.bt.api.Command;
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
 * Internal class used as the main interaction entry point for bluetooth devices.
 */
class BtSppScanner implements Closeable, Scanner {
    private static final String LOG_TAG = "BtSppSdk";

    private final BluetoothDevice rawDevice;
    private ConnectToBtDeviceThread connectionThread;
    private Timer timeoutHunter;

    private final String name;

    private BluetoothSocket clientSocket;
    private SocketStreamReader inputStreamReader;
    private SocketStreamWriter outputStreamWriter;

    private ScannerDataParser inputHandler;

    /**
     * All the callbacks which are registered to run on received data (post-parsing). Key is data class name.
     */
    private final Map<String, DataSubscription> dataSubscriptions = new HashMap<>();

    /**
     * Create an unconnected device from a cached device definition. Need to call {@link #connect(BluetoothAdapter, ConnectToBtDeviceThread.OnConnectedCallback)} before any interaction with the device.
     *
     * @param device a device definition
     */
    BtSppScanner(BluetoothDevice device) {
        this.rawDevice = device;
        this.name = this.rawDevice.getName();
        this.inputHandler = new SsiParser();
        this.setUpTimeoutTimer();
    }

    /**
     * Create a connected device from an already open bluetooth socket.
     *
     * @param socket a socket to a bluetooth device (male or slave - do not care).
     */
    BtSppScanner(BluetoothSocket socket) {
        this.rawDevice = socket.getRemoteDevice();
        this.name = this.rawDevice.getName();
        this.inputHandler = new SsiParser();

        this.clientSocket = socket;
        this.connectStreams();
        this.setUpTimeoutTimer();
        Log.i(LOG_TAG, "Device " + BtSppScanner.this.name + " reports it is connected");

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

    void setProvider(BtSppScannerProvider provider) {
        this.inputHandler = provider.getInputHandler();
    }

    void connect(BluetoothAdapter bluetoothAdapter, final ConnectToBtDeviceThread.OnConnectedCallback callback) {
        Log.i(LOG_TAG, "Starting connection to device " + BtSppScanner.this.name);
        connectionThread = new ConnectToBtDeviceThread(rawDevice, bluetoothAdapter, new ConnectToBtDeviceThread.OnConnectedCallback() {
            @Override
            public void connected(BluetoothSocket bluetoothSocket) {
                BtSppScanner.this.connectionThread = null;
                Log.i(LOG_TAG, "Device " + BtSppScanner.this.name + " reports it is connected");
                BtSppScanner.this.clientSocket = bluetoothSocket;
                connectStreams();

                callback.connected(bluetoothSocket);
            }

            @Override
            public void failed() {
                callback.failed();
            }
        });
        connectionThread.start();
    }

    @Override
    public void disconnect() {
        try {
            this.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not close the connect socket", e);
        }
    }

    private void connectStreams() {
        if (this.clientSocket == null) {
            return;
        }

        try {
            this.inputStreamReader = new SocketStreamReader(this.clientSocket.getInputStream(), this);
            this.inputStreamReader.start();

            this.outputStreamWriter = new SocketStreamWriter(this.clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        if (this.connectionThread != null) {
            this.connectionThread.cancel();
            this.connectionThread = null;
        }

        if (this.inputStreamReader != null) {
            this.inputStreamReader.close();
        }
        if (this.outputStreamWriter != null) {
            this.outputStreamWriter.close();
        }
        if (this.clientSocket != null) {
            this.clientSocket.close();
        }
        if (this.timeoutHunter != null) {
            this.timeoutHunter.cancel();
            this.timeoutHunter = null;
        }
    }

    @Override
    public String getName() {
        return this.name;
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

    void handleInputBuffer(byte[] buffer, int offset, int length) {
        ParsingResult res = this.inputHandler.parse(buffer, offset, length);
        if (!res.expectingMoreData && res.data != null) {
            Log.d(LOG_TAG, "Data was interpreted as: " + res.data.toString());

            // Subscriptions to fulfill on that data type?
            synchronized (dataSubscriptions) {
                if (this.dataSubscriptions.containsKey(res.data.getClass().getCanonicalName())) {
                    DataSubscription subscription = this.dataSubscriptions.get(res.data.getClass().getCanonicalName());
                    DataSubscriptionCallback callback = subscription.getCallback();
                    callback.onSuccess(res.data);

                    if (!subscription.isPermanent()) {
                        this.dataSubscriptions.remove(res.data.getClass().getCanonicalName());
                    }
                }
            }

            if (res.acknowledger != null) {
                this.outputStreamWriter.endOfCommand();
                this.outputStreamWriter.write(res.acknowledger.getCommand(), true);
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
    }
}
