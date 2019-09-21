package com.enioka.scanner.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

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
public class BtDevice implements Closeable {
    private static final String LOG_TAG = "InternalBtDevice";

    private final BluetoothDevice rawDevice;
    private ConnectToBtDeviceThread connectionThread;
    private Timer timeoutHunter;

    private final String name;

    private BluetoothSocket clientSocket;
    private BtSocketStreamReader inputStreamReader;
    private BtSocketStreamWriter outputStreamWriter;

    private BtInputHandler inputHandler;

    /**
     * All the callbacks which are registered to run on received data (post-parsing). Key is data class name.
     */
    private final Map<String, DataSubscription> dataSubscriptions = new HashMap<>();

    /**
     * Create an unconnected device from a cached device definition. Need to call {@link #connect(BluetoothAdapter, ConnectToBtDeviceThread.OnConnectedCallback)} before any interaction with the device.
     *
     * @param device a device definition
     */
    BtDevice(BluetoothDevice device) {
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
    BtDevice(BluetoothSocket socket) {
        this.rawDevice = socket.getRemoteDevice();
        this.name = this.rawDevice.getName();
        this.inputHandler = new SsiParser();

        this.clientSocket = socket;
        this.connectStreams();
        this.setUpTimeoutTimer();
        Log.i(LOG_TAG, "Device " + BtDevice.this.name + " reports it is connected");

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
        Log.i(LOG_TAG, "Starting connection to device " + BtDevice.this.name);
        connectionThread = new ConnectToBtDeviceThread(rawDevice, bluetoothAdapter, new ConnectToBtDeviceThread.OnConnectedCallback() {
            @Override
            public void connected(BluetoothSocket bluetoothSocket) {
                BtDevice.this.connectionThread = null;
                Log.i(LOG_TAG, "Device " + BtDevice.this.name + " reports it is connected");
                BtDevice.this.clientSocket = bluetoothSocket;
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

    public void disconnect() {
        try {
            this.clientSocket.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not close the connect socket", e);
        }
    }

    private void connectStreams() {
        if (this.clientSocket == null) {
            return;
        }

        try {
            this.inputStreamReader = new BtSocketStreamReader(this.clientSocket.getInputStream(), this);
            this.inputStreamReader.start();

            this.outputStreamWriter = new BtSocketStreamWriter(this.clientSocket.getOutputStream());
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
        if (this.clientSocket != null) {
            this.clientSocket.close();
        }
        if (this.timeoutHunter != null) {
            this.timeoutHunter.cancel();
            this.timeoutHunter = null;
        }
    }

    /**
     * Helper to discover SPP protocols. Debug only.
     */
    private void discoverCodes() {
        for (int i = 2043; i < 2126; i++) {
            String cmd = "{G" + i + "?}{G1026}";
            Log.i(LOG_TAG, cmd);
            this.outputStreamWriter.write(cmd);
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    String getName() {
        return this.name;
    }


    /**
     * Run a command on the scanner. Asynchronous - this call returns before the command is actually sent to the scanner.<br>
     * If the command expects an answer, it will be received as any data from the scanner and sent to the registered {@link BtInputHandler}
     * (there is no direct "link" between command and response).
     *
     * @param command      what should be run
     * @param subscription an optional subscription waiting for an asnwer to the command
     * @param <T>          expected return type of the command (implicit, found from command argument)
     */
    public <T> void runCommand(ICommand<T> command, CommandCallback<T> subscription) {
        byte[] cmd = command.getCommand();

        if (subscription != null) {
            synchronized (dataSubscriptions) {
                String expectedDataClass = command.getReturnType().getCanonicalName();
                this.dataSubscriptions.put(expectedDataClass, new DataSubscription(subscription, command.getTimeOut(), false));
            }
        }

        Log.d(LOG_TAG, "Queuing for dispatch command " + command.getClass().getSimpleName());
        this.outputStreamWriter.write(cmd);
    }

    void handleInputBuffer(byte[] buffer, int offset, int length) {
        BtParsingResult res = this.inputHandler.process(buffer, offset, length);
        if (!res.expectingMoreData && res.data != null) {
            Log.d(LOG_TAG, "Data was interpreted as: " + res.data.toString());

            // Subscriptions to fulfill on that data type?
            synchronized (dataSubscriptions) {
                if (this.dataSubscriptions.containsKey(res.data.getClass().getCanonicalName())) {
                    DataSubscription subscription = this.dataSubscriptions.get(res.data.getClass().getCanonicalName());
                    CommandCallback callback = subscription.getCallback();
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
