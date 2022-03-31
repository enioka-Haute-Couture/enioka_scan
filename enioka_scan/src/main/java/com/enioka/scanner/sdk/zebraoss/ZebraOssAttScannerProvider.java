package com.enioka.scanner.sdk.zebraoss;

import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.bt.api.Scanner;
import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.sdk.zebraoss.commands.CapabilitiesRequest;
import com.enioka.scanner.sdk.zebraoss.data.CapabilitiesReply;

public class ZebraOssAttScannerProvider implements BtSppScannerProvider {
    private final ScannerDataParser inputHandler = new SsiOverAttParser();

    @Override
    public void canManageDevice(final Scanner device, final ManagementCallback callback) {
        if (!device.isBleDevice())
            callback.cannotManage();

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
    public ScannerDataParser getInputHandler() {
        return inputHandler;
    }

}
