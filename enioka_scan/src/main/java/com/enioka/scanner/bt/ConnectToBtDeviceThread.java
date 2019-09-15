package com.enioka.scanner.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * A thread which attempts (once) to connect to a given BT SPP slave device.
 */
class ConnectToBtDeviceThread extends Thread {
    private static final String LOG_TAG = "InternalBtDevice";

    // This is the SPP service UUID. From http://sviluppomobile.blogspot.com/2012/11/bluetooth-services-uuids.html
    static final UUID SERVER_BT_SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothSocket clientSocket;
    private final BluetoothAdapter bluetoothAdapter;
    private final OnConnectedCallback onConnectedCallback;

    interface OnConnectedCallback {
        void connected(BluetoothSocket bluetoothSocket);

        void failed();
    }

    ConnectToBtDeviceThread(BluetoothDevice bluetoothDevice, BluetoothAdapter bluetoothAdapter, OnConnectedCallback callback) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.onConnectedCallback = callback;

        BluetoothSocket socket = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            socket = bluetoothDevice.createRfcommSocketToServiceRecord(SERVER_BT_SERVICE_UUID);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Socket's create() method failed", e);
        }
        this.clientSocket = socket;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            clientSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            Log.e(LOG_TAG, "Could not connect to device. " + connectException.getMessage());
            try {
                clientSocket.close();
            } catch (IOException closeException) {
                Log.e(LOG_TAG, "Could not close the client socket", closeException);
            }
            this.onConnectedCallback.failed();
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        this.onConnectedCallback.connected(clientSocket);
    }

    // Closes the client socket and causes the thread to finish.
    void cancel() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not close the client socket", e);
        }
    }
}
