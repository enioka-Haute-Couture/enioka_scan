package com.enioka.scanner.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.enioka.scanner.api.Color;
import com.enioka.scanner.sdk.zebraoss.SsiParser;
import com.enioka.scanner.sdk.zebraoss.commands.CapabilitiesRequest;
import com.enioka.scanner.sdk.zebraoss.commands.LedOff;
import com.enioka.scanner.sdk.zebraoss.commands.LedOn;
import com.enioka.scanner.sdk.zebraoss.data.CapabilitiesReply;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Internal class used as the main interaction entry point for bluetooth devices.
 */
public class BtDevice implements Closeable {
    private static final String LOG_TAG = "InternalBtDevice";

    private final BluetoothDevice rawDevice;
    private ConnectToBtDeviceThread connectionThread;

    private final String name;

    private BluetoothSocket clientSocket;
    private BtSocketStreamReader inputStreamReader;
    private BtSocketStreamWriter outputStreamWriter;

    private final BtInputHandler inputHandler;

    private final Map<String, CommandCallbackHolder<?>> commandCallbacks = new HashMap<>();

    /**
     * Create an unconnected device from a cached device definition. Need to call {@link #connect(BluetoothAdapter)} before any interaction with the device.
     *
     * @param device a device definition
     */
    BtDevice(BluetoothDevice device) {
        this.rawDevice = device;
        this.name = this.rawDevice.getName();
        this.inputHandler = new SsiParser();
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
        Log.i(LOG_TAG, "Device " + BtDevice.this.name + " reports it is connected");
        //this.outputStreamWriter.write(new byte[]{0x5, (byte) 0x70, 0x4, 0, (byte) 0xFE, (byte) 0xFE, (byte) 0x89});

    }

    void connect(BluetoothAdapter bluetoothAdapter) {
        Log.i(LOG_TAG, "Starting connection to device " + BtDevice.this.name);
        connectionThread = new ConnectToBtDeviceThread(rawDevice, bluetoothAdapter, new ConnectToBtDeviceThread.OnConnectedCallback() {
            @Override
            public void connected(BluetoothSocket bluetoothSocket) {
                BtDevice.this.connectionThread = null;
                Log.i(LOG_TAG, "Device " + BtDevice.this.name + " reports it is connected");
                BtDevice.this.clientSocket = bluetoothSocket;
                connectStreams();

                // TEMP
                //BtDevice.this.runCommand(new GetBatteryLevel());
                //BtDevice.this.outputStreamWriter.write("{G2043/1/\r\n}{G1026}");
                //BtDevice.this.outputStreamWriter.write("{G1026?}");
                //BtDevice.this.outputStreamWriter.write("{G3010/0}");
                //BtDevice.this.outputStreamWriter.write("{G2351?}");
                //BtDevice.this.runCommand(new SetBeepLevel());
                //discoverCodes();
                //BtDevice.this.outputStreamWriter.write(Character.toString((char) 22) + "U" + Character.toString((char) 13));

                // BtDevice.this.outputStreamWriter.write(new byte[] {0x5, (byte)0xC7, 0x4, 0, (byte)0xFE, (byte)0xFE, (byte)0x32}); // Should work for ds3600
                //BtDevice.this.outputStreamWriter.write(new byte[]{0x5, (byte) 0x70, 0x4, 0, (byte) 0xFE, (byte) 0xFE, (byte) 0x89});
                //BtDevice.this.outputStreamWriter.write("HOUBA");
            }

            @Override
            public void failed() {

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

    public <T> void runCommand(ICommand<T> command) {
        byte[] cmd = command.getCommand();

        CommandCallbackHolder cbHolder = command.getCallback();
        if (cbHolder != null && cbHolder.getCallback() != null) {
            this.commandCallbacks.put(cbHolder.getCommandReturnType().getCanonicalName(), cbHolder);
        }

        Log.d(LOG_TAG, "Queuing for dispatch command " + command.getClass().getSimpleName());
        this.outputStreamWriter.write(cmd);
    }

    void handleInputBuffer(byte[] buffer, int offset, int length) {
        BtParsingResult res = this.inputHandler.process(buffer, offset, length);
        if (!res.expectingMoreData && res.data != null) {
            Log.d(LOG_TAG, "Data was interpreted as: " + res.data.toString());

            // Callbacks?
            if (this.commandCallbacks.containsKey(res.data.getClass().getCanonicalName())) {
                CommandCallbackHolder cbHolder = this.commandCallbacks.get(res.data.getClass().getCanonicalName());
                cbHolder.getCallback().onSuccess(res.data);
            }

            if (res.acknowledger != null) {
                this.outputStreamWriter.write(res.acknowledger.getOkCommand());
                // this.runCommand(new LedOn(Color.RED)); // DEBUG
                this.runCommand(new CapabilitiesRequest(new CommandCallback<CapabilitiesReply>() {
                    @Override
                    public void onSuccess(CapabilitiesReply data) {
                        Log.i(LOG_TAG, "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
                    }
                })); // DEBUG
            }
        } else if (!res.expectingMoreData) {
            Log.d(LOG_TAG, "Message was interpreted as: message without additional data");
            if (res.acknowledger != null) {
                this.outputStreamWriter.write(res.acknowledger.getOkCommand());
            }
        } else {
            Log.d(LOG_TAG, "Data was not interpreted yet as we are expecting more data");
        }
    }
}
