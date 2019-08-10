package com.enioka.scanner.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.enioka.scanner.sdk.generalscan.commands.GetBatteryLevel;

import java.io.Closeable;
import java.io.IOException;

/**
 * Internal class used as the main interaction entry point for bluetooth devices.
 */
public class BtDevice implements Closeable {
    private static final String LOG_TAG = "InternalBtDevice";

    private final BluetoothDevice rawDevice;
    private final BluetoothAdapter bluetoothAdapter;
    private ConnectToBtDeviceThread connectionThread;

    private final String name;

    private BluetoothSocket clientSocket;
    private BtSocketStreamReader inputStreamReader;
    private BtSocketStreamWriter outputStreamWriter;

    private final OrderedInputHandler inputHandler;


    BtDevice(BluetoothDevice device, BluetoothAdapter bluetoothAdapter) {
        this.rawDevice = device;
        this.bluetoothAdapter = bluetoothAdapter;
        this.name = this.rawDevice.getName();
        this.inputHandler = new OrderedInputHandler();
    }

    void connect() {
        Log.i(LOG_TAG, "Starting connection to device " + BtDevice.this.name);
        connectionThread = new ConnectToBtDeviceThread(rawDevice, bluetoothAdapter, new ConnectToBtDeviceThread.OnConnectedCallback() {
            @Override
            public void connected(BluetoothSocket bluetoothSocket) {
                BtDevice.this.connectionThread = null;
                Log.i(LOG_TAG, "Device " + BtDevice.this.name + " reports it is connected");
                BtDevice.this.clientSocket = bluetoothSocket;
                connectStreams();

                // TEMP
                //BtDevice.this.outputStreamWriter.write(0);
                BtDevice.this.runCommand(new GetBatteryLevel());
                BtDevice.this.outputStreamWriter.write("{G2108}");
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
            this.inputStreamReader = new BtSocketStreamReader(this.clientSocket.getInputStream(), this.inputHandler);
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

    private void runCommand(BtCommand command) {
        if (command instanceof BtCommandWithAnswer) {
            this.inputHandler.expectAnswer((BtCommandWithAnswer) command);
        }
        String cmd = command.getCommand();
        Log.d(LOG_TAG, "Running command " + cmd);
        this.outputStreamWriter.write(cmd);
    }
}