package com.enioka.scanner.sdk.honeywelloss;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.BtSppScannerProviderServiceBinder;
import com.enioka.scanner.bt.api.Scanner;
import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.sdk.honeywelloss.parsers.HoneywellOssParser;

public class HoneywellOssSppScannerProvider extends Service implements BtSppScannerProvider {
    private final IBinder binder = new BtSppScannerProviderServiceBinder(this);

    private final ScannerDataParser inputHandler = new HoneywellOssParser();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void canManageDevice(final Scanner device, final ManagementCallback callback) {
        callback.canManage(new HoneywellOssScanner(device));
        /*device.runCommand(new CapabilitiesRequest(), new DataSubscriptionCallback<CapabilitiesReply>() {
            @Override
            public void onSuccess(CapabilitiesReply data) {
                callback.canManage(new HoneywellOssScanner(device));
            }

            @Override
            public void onTimeout() {
                callback.cannotManage();
            }

            @Override
            public void onFailure() {
                callback.cannotManage();
            }
        });*/
    }

    @Override
    public ScannerDataParser getInputHandler() {
        return inputHandler;
    }

}
