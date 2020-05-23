package com.enioka.scanner.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class BleTerminalIOStreamWriter implements Closeable {
    private static final String LOG_TAG = "BtSppSdk";

    private final ExecutorService pool;
    private final Semaphore commandLock = new Semaphore(1);
    private final BleTerminalIODevice device;
    private final BluetoothGatt gatt;
    private final BluetoothGattCharacteristic tioDataRxCharacteristic;

    BleTerminalIOStreamWriter(BleTerminalIODevice device, BluetoothGatt gatt) {
        this.pool = Executors.newFixedThreadPool(1);
        this.device = device;

        BluetoothGattService service = gatt.getService(GattAttribute.TERMINAL_IO_SERVICE.id);
        tioDataRxCharacteristic = service.getCharacteristic(GattAttribute.TERMINAL_IO_UART_DATA_RX.id);
        if (tioDataRxCharacteristic == null) {
            throw new IllegalStateException("cannot create a TIO stream writer on a non-TIO device");
        }
        tioDataRxCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        this.gatt = gatt;
    }

    @Override
    public void close() throws IOException {

    }

    void write(byte[] buffer, boolean waitForCommandLock) {
        pool.submit(new BleTerminalIOStreamWriterTask(buffer, waitForCommandLock, this.gatt));
    }

    void endOfCommand() {
        this.commandLock.release();
    }

    private void startOfCommand() {
        try {
            this.commandLock.acquire();
        } catch (InterruptedException e) {
            return;
        }
    }

    private class BleTerminalIOStreamWriterTask implements Runnable {
        private final byte[] buffer;
        private final boolean waitForCommandLock;
        private final BluetoothGatt gatt;

        BleTerminalIOStreamWriterTask(byte[] buffer, boolean waitForCommandLock, BluetoothGatt gatt) {
            this.buffer = buffer;
            this.waitForCommandLock = waitForCommandLock;
            this.gatt = gatt;
        }


        @Override
        public void run() {
            Log.d(LOG_TAG, "Trying to write data on characteristic " + tioDataRxCharacteristic.getUuid() + " - " + buffer);
            if (waitForCommandLock) {
                BleTerminalIOStreamWriter.this.startOfCommand();
            }

            // Cut in blocks of 20 bytes with minimal allocations.
            byte[] loopData = null;
            for (int i = 0; i < this.buffer.length; i += 20) {
                // TIO requires a credit for each loop.
                BleTerminalIOStreamWriter.this.device.waitForClientCredits(1);

                if (i == 0 && this.buffer.length <= 20) {
                    loopData = this.buffer;
                } else {
                    if (loopData == null) {
                        loopData = new byte[20];
                    }
                    System.arraycopy(this.buffer, i, loopData, 0, Math.min(this.buffer.length, (i + 1) * 20));
                }

                Log.d(LOG_TAG, "Locks OK, writing data on characteristic " + tioDataRxCharacteristic.getUuid() + " - " + buffer);
                BleTerminalIOStreamWriter.this.tioDataRxCharacteristic.setValue(loopData);
                gatt.writeCharacteristic(BleTerminalIOStreamWriter.this.tioDataRxCharacteristic);
            }
        }
    }
}
