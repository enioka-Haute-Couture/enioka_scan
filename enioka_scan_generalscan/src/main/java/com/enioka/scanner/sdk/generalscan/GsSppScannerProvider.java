package com.enioka.scanner.sdk.generalscan;

import android.util.Log;

import com.enioka.scanner.bt.api.BluetoothScanner;
import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.sdk.generalscan.commands.CloseRead;
import com.enioka.scanner.sdk.generalscan.commands.GetDeviceId;
import com.enioka.scanner.sdk.generalscan.commands.OpenRead;
import com.enioka.scanner.sdk.generalscan.data.DeviceId;

public class GsSppScannerProvider extends GsSppPairing implements BtSppScannerProvider {
    public static final String PROVIDER_KEY = "BT_GeneralScanProvider";

    @Override
    public void canManageDevice(final BluetoothScanner device, final ManagementCallback callback) {
        device.runCommand(new CloseRead(), null);
        device.runCommand(new GetDeviceId(), new DataSubscriptionCallback<DeviceId>() {
            @Override
            public void onSuccess(DeviceId data) {
                device.runCommand(new OpenRead(), null);
                callback.canManage(new GsSppScanner(device));
            }

            @Override
            public void onFailure() {
                callback.cannotManage();
            }

            @Override
            public void onTimeout() {
                callback.cannotManage();
            }
        });
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }

    @Override
    public ScannerDataParser getInputHandler() {
        return new Parser();
    }

    /**
     * Helper to discover protocol. Debug only.
     */
    @SuppressWarnings("unused")
    private void discoverCodes(BluetoothScanner device) {
        for (int i = 0; i < 300; i++) {
            Log.i("TEST", "Command: " + i);
            device.runCommand(new CloseRead(), null);

            final int j = i;
            device.runCommand(new Command<Void>() {
                @Override
                public byte[] getCommand() {
                    return ("{G" + j + "?}{G1026}").getBytes();
                }

                @Override
                public Class<? extends Void> getReturnType() {
                    return null;
                }

                @Override
                public int getTimeOut() {
                    return 100;
                }
            }, null);

            device.runCommand(new OpenRead(), null);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
