package com.enioka.scanner.sdk.honeywelloss;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.BtSppScannerProviderServiceBinder;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.bt.api.Scanner;
import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.sdk.honeywelloss.commands.GetFirmware;
import com.enioka.scanner.sdk.honeywelloss.data.FirmwareVersion;
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
        device.runCommand(new GetFirmware(), new DataSubscriptionCallback<FirmwareVersion>() {
            @Override
            public void onSuccess(FirmwareVersion data) {
                callback.canManage(new HoneywellOssScanner(device));
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
        return inputHandler;
    }

}
