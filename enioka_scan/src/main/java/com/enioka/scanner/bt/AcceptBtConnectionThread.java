package com.enioka.scanner.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class AcceptBtConnectionThread extends Thread {
    private static final String LOG_TAG = "InternalBtDevice";

    // This is the SPP service UUID. From http://sviluppomobile.blogspot.com/2012/11/bluetooth-services-uuids.html
    protected static final UUID SERVER_BT_SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothServerSocket serverSocket;
    private final BluetoothAdapter bluetoothAdapter;
    private final ConnectToBtDeviceThread.OnConnectedCallback onConnectedCallback;

    public AcceptBtConnectionThread(BluetoothAdapter bluetoothAdapter, ConnectToBtDeviceThread.OnConnectedCallback callback) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.onConnectedCallback = callback;

        BluetoothServerSocket serverSocket = null;
        try {
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("MARSU", SERVER_BT_SERVICE_UUID);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Socket's create() method failed", e);
        }
        this.serverSocket = serverSocket;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter.cancelDiscovery();
        BluetoothSocket clientSocket = null;

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
                }
                Log.i(LOG_TAG, "It is a live socket.");
                this.cancel();
                break;
            }
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        this.onConnectedCallback.connected(clientSocket);
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
