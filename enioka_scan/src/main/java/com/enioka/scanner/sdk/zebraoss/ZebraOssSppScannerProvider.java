package com.enioka.scanner.sdk.zebraoss;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.enioka.scanner.bt.BtDevice;
import com.enioka.scanner.bt.BtInputHandler;
import com.enioka.scanner.bt.BtSppScannerProvider;
import com.enioka.scanner.bt.BtSppScannerProviderServiceBinder;
import com.enioka.scanner.bt.CommandCallback;
import com.enioka.scanner.sdk.zebraoss.commands.CapabilitiesRequest;
import com.enioka.scanner.sdk.zebraoss.data.CapabilitiesReply;

public class ZebraOssSppScannerProvider extends Service implements BtSppScannerProvider {
    private final IBinder binder = new BtSppScannerProviderServiceBinder(this);

    private final BtInputHandler inputHandler = new SsiParser();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void canManageDevice(BtDevice device, final ManagementCallback callback) {

        device.runCommand(new CapabilitiesRequest(new CommandCallback<CapabilitiesReply>() {
            @Override
            public void onSuccess(CapabilitiesReply data) {
                callback.canManage();
            }

            @Override
            public void onTimeout() {
                callback.cannotManage();
            }

            @Override
            public void onFailure() {
                callback.cannotManage();
            }
        }));
    }

    @Override
    public BtInputHandler getInputHandler() {
        return inputHandler;
    }

}
