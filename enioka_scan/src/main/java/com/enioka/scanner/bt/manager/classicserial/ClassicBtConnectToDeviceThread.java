package com.enioka.scanner.bt.manager.classicserial;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.enioka.scanner.bt.manager.common.BtConstHelpers;

import java.io.IOException;

/**
 * A thread which attempts (once) to connect to a given Classic BT SPP slave device.
 * This is only a BT connection, without any applicative (level 7) stuff. This is the socket opening - streams are opened later by another component!
 */
public class ClassicBtConnectToDeviceThread extends Thread {
    private static final String LOG_TAG = "BtSppSdk";

    private BluetoothSocket clientSocket;
    private final BluetoothDevice bluetoothDevice;
    private final OnStreamConnectedCallback onConnectedCallback;

    ClassicBtConnectToDeviceThread(BluetoothDevice bluetoothDevice, OnStreamConnectedCallback callback) {
        this.onConnectedCallback = callback;
        this.bluetoothDevice = bluetoothDevice;
    }

    public void run() {
        try {
            this.clientSocket = bluetoothDevice.createRfcommSocketToServiceRecord(BtConstHelpers.SERVER_BT_SERVICE_UUID);
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
            Log.e(LOG_TAG, "Could not connect to device. " + bluetoothDevice.getName() + " - " + connectException.getMessage());
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

    // Called when timeout is reached, will check if the connection succeeded. If yes, nothing happens. If not, the socket is closed.
    void timeout() {
        if (clientSocket != null && !clientSocket.isConnected()) {
            Log.i(LOG_TAG, "Bluetooth reconnection timed out");
            cancel();
            onConnectedCallback.failed();
        }
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
