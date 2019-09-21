package com.enioka.scanner.sdk.generalscan;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.enioka.scanner.bt.BtDevice;
import com.enioka.scanner.bt.BtInputHandler;
import com.enioka.scanner.bt.BtSppScannerProvider;
import com.enioka.scanner.bt.BtSppScannerProviderServiceBinder;
import com.enioka.scanner.bt.CommandCallback;
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
    public void canManageDevice(BtDevice device, final ManagementCallback callback) {
        device.runCommand(new GetDeviceId(), new CommandCallback<DeviceId>() {
            @Override
            public void onSuccess(DeviceId data) {
                callback.canManage();
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
    public BtInputHandler getInputHandler() {
        return new Parser();
    }

}
