package com.enioka.scanner.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

/**
 * A SPP service listener thread on a bluetooth adapter. Used by master BT devices to connect to the phone. Closes after the first connection (OK or not). No timeout.
 */
public class AcceptBtConnectionThread extends Thread {
    private static final String LOG_TAG = "InternalBtDevice";

    private final BluetoothServerSocket serverSocket;
    private final BluetoothAdapter bluetoothAdapter;
    private final ConnectToBtDeviceThread.OnConnectedCallback onConnectedCallback;

    AcceptBtConnectionThread(BluetoothAdapter bluetoothAdapter, ConnectToBtDeviceThread.OnConnectedCallback callback) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.onConnectedCallback = callback;

        BluetoothServerSocket serverSocket = null;
        try {
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("SPP", ConnectToBtDeviceThread.SERVER_BT_SERVICE_UUID);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Socket's create() method failed", e);
        }
        this.serverSocket = serverSocket;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter.cancelDiscovery();
        BluetoothSocket clientSocket;

        Log.i(LOG_TAG, "Starting bluetooth slave server and waiting for incoming connections");

        while (true) {
            try {
                // Connect to the remote device through the socket. This call blocks until it succeeds or throws an exception.
                clientSocket = this.serverSocket.accept();
                Log.i(LOG_TAG, "Connection received!");
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                Log.e(LOG_TAG, "Could not accept device connection. " + connectException.getMessage());
                break;
            }

            if (clientSocket != null) {
                if (this.onConnectedCallback != null) {
                    this.onConnectedCallback.connected(clientSocket);
                    Log.i(LOG_TAG, "Live socket opened for incoming SPP BT device.");
                }

                this.cancel();
                break;
            }
        }
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not close the client socket", e);
        }
    }
}
