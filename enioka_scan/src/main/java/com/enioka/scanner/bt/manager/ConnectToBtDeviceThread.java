package com.enioka.scanner.bt.manager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * A thread which attempts (once) to connect to a given BT SPP slave device.
 */
class ConnectToBtDeviceThread extends Thread {
    private static final String LOG_TAG = "BtSppSdk";

    // This is the SPP service UUID. From http://sviluppomobile.blogspot.com/2012/11/bluetooth-services-uuids.html
    static final UUID SERVER_BT_SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket clientSocket;
    private final BluetoothDevice bluetoothDevice;
    private final OnConnectedCallback onConnectedCallback;

    interface OnConnectedCallback {
        void connected(BluetoothSocket bluetoothSocket);

        void failed();
    }

    ConnectToBtDeviceThread(BluetoothDevice bluetoothDevice, OnConnectedCallback callback) {
        this.onConnectedCallback = callback;
        this.bluetoothDevice = bluetoothDevice;
    }

    public void run() {
        try {
            this.clientSocket = bluetoothDevice.createRfcommSocketToServiceRecord(SERVER_BT_SERVICE_UUID);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Socket's create() method failed", e);
            this.onConnectedCallback.failed();
            return;
        }

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
                Log.w(LOG_TAG, "Could not close the client socket after exception", closeException);
            }
            this.clientSocket = null;
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
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not close the client socket", e);
        }
    }
}
