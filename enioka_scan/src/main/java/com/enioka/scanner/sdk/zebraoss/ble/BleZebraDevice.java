package com.enioka.scanner.sdk.zebraoss.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.bt.manager.bleserial.BleStateMachineDevice;
import com.enioka.scanner.bt.manager.classicbtspp.ClassicBtConnectToDeviceThread;
import com.enioka.scanner.bt.manager.common.OnConnectedCallback;
import com.enioka.scanner.bt.manager.common.ScannerInternal;

import java.io.Closeable;
import java.io.IOException;

/**
 * For BLE, Zebra has chosen to encapsulate the existing SSI protocol inside a very simple protocol with two attributes dedicated to data,
 * two for token-based flow control, and (more mysterious) two for configuration (seemingly based on magic strings).<br><br>
 * The SSI data is simply prefixed with a (two-bytes) total packet length, packet which can be cut into pieces - prefix is only for first piece.
 */
public class BleZebraDevice implements BleStateMachineDevice, Closeable, ScannerInternal {
    private final Context ctx;
    private final BluetoothDevice btDevice;
    private final String deviceName;

    private BtSppScannerProvider scannerProvider;

    /**
     * Creates a new BLE device. Does not connect to anything of have any other kind of side-effect. Connection is handled in connect method.
     *
     * @param ctx      a valid context (application, service, activity...)
     * @param btDevice the BT device to encapsulate.
     */
    public BleZebraDevice(Context ctx, BluetoothDevice btDevice) {
        this.ctx = ctx;
        this.btDevice = btDevice;
        this.deviceName = btDevice.getName();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // State machine methods
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onEvent(BleEvent event) {

    }

    @Override
    public void close() throws IOException {

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Scanner Internal methods
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void connect(OnConnectedCallback callback) {

    }

    @Override
    public void setProvider(BtSppScannerProvider provider) {
        this.scannerProvider = provider;
    }

    @Override
    public <T> void runCommand(Command<T> command, DataSubscriptionCallback<T> subscription) {

    }

    @Override
    public String getName() {
        return this.deviceName;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public <T> void registerSubscription(DataSubscriptionCallback<T> subscription, Class<? extends T> targetType) {

    }

    @Override
    public void registerStatusCallback(SppScannerStatusCallback statusCallback) {

    }
}
