package com.enioka.scanner.sdk.generalscan;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.bt.api.Scanner;
import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.BtSppScannerProviderServiceBinder;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.sdk.generalscan.commands.GetDeviceId;
import com.enioka.scanner.sdk.generalscan.data.DeviceId;

public class GsSppScannerProvider extends Service implements BtSppScannerProvider {
    private final IBinder binder = new BtSppScannerProviderServiceBinder(this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void canManageDevice(final Scanner device, final ManagementCallback callback) {
        device.runCommand(new GetDeviceId(), new DataSubscriptionCallback<DeviceId>() {
            @Override
            public void onSuccess(DeviceId data) {
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
    public ScannerDataParser getInputHandler() {
        return new Parser();
    }
}
