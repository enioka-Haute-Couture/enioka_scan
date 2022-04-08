package com.enioka.scanner.sdk.postech;

import android.util.Log;

import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.bt.api.Scanner;
import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.sdk.generalscan.Parser;
import com.enioka.scanner.sdk.postech.commands.GetDeviceName;
import com.enioka.scanner.sdk.postech.data.DeviceName;

public class PostechSppScannerProvider implements BtSppScannerProvider {
    public static final String PROVIDER_NAME = "BT_PostechProvider";

    @Override
    public void canManageDevice(final Scanner device, final ManagementCallback callback) {
        //discoverCodes(device);
        //device.runCommand(new CloseRead(), null);
        device.runCommand(new GetDeviceName(), new DataSubscriptionCallback<DeviceName>() {
            @Override
            public void onSuccess(DeviceName data) {
                callback.canManage(new PostechSppScanner(device));
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
        //device.runCommand(new OpenRead(), null);
    }

    @Override
    public String getKey() {
        return PROVIDER_NAME;
    }

    @Override
    public ScannerDataParser getInputHandler() {
        return new Parser();
    }

    /**
     * Helper to discover protocol. Debug only.
     */
    @SuppressWarnings("unused")
    private void discoverCodes(Scanner device) {
        for (int i = 0; i < 3000; i++) {
            Log.i("TEST", "Command: " + i);
            //device.runCommand(new CloseRead(), null);

            final int j = i;
            device.runCommand(new Command<Void>() {
                @Override
                public byte[] getCommand() {
                    return ("{G" + j + "?}{GB201}").getBytes();
                }

                @Override
                public Class<? extends Void> getReturnType() {
                    return null;
                }

                @Override
                public int getTimeOut() {
                    return 500;
                }
            }, null);

            //device.runCommand(new OpenRead(), null);

            try {
                Thread.sleep(1000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
