package com.enioka.scanner.sdk.zebraoss;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.bt.api.Scanner;
import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.BtSppScannerProviderServiceBinder;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.sdk.zebraoss.commands.CapabilitiesRequest;
import com.enioka.scanner.sdk.zebraoss.data.CapabilitiesReply;

public class ZebraOssSppScannerProvider extends Service implements BtSppScannerProvider {
    private final IBinder binder = new BtSppScannerProviderServiceBinder(this);

    private final ScannerDataParser inputHandler = new SsiParser();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void canManageClassicDevice(final Scanner device, final ManagementCallback callback) {

        device.runCommand(new CapabilitiesRequest(), new DataSubscriptionCallback<CapabilitiesReply>() {
            @Override
            public void onSuccess(CapabilitiesReply data) {
                callback.canManage(new ZebraOssScanner(device));
            }

            @Override
            public void onTimeout() {
                callback.cannotManage();
            }

            @Override
            public void onFailure() {
                callback.cannotManage();
            }
        });
    }

    @Override
    public void canManageBleDevice(Scanner device, ManagementCallback callback) {
        callback.cannotManage();
    }

    @Override
    public ScannerDataParser getInputHandler() {
        return inputHandler;
    }

}
