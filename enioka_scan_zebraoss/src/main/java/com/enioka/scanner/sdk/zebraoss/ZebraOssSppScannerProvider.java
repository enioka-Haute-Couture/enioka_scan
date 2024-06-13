package com.enioka.scanner.sdk.zebraoss;

import com.enioka.scanner.bt.api.BluetoothScanner;
import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.bt.api.ScannerDataParser;
import com.enioka.scanner.sdk.zebraoss.commands.CapabilitiesRequest;
import com.enioka.scanner.sdk.zebraoss.data.CapabilitiesReply;

public class ZebraOssSppScannerProvider extends ZebraOssPairing implements BtSppScannerProvider {
    public static final String PROVIDER_KEY = "BT_ZebraOssSPPProvider";
    private final ScannerDataParser inputHandler = new SsiOverSppParser();

    public ZebraOssSppScannerProvider() {
        super(PROVIDER_KEY);
    }

    @Override
    public void canManageDevice(final BluetoothScanner device, final ManagementCallback callback) {
        if (device.isBleDevice()) {
            callback.cannotManage();
            return;
        }

        device.runCommand(new CapabilitiesRequest(), new DataSubscriptionCallback<CapabilitiesReply>() {
            @Override
            public void onSuccess(CapabilitiesReply data) {
                callback.canManage(new ZebraOssScanner(PROVIDER_KEY, device));
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
    public String getKey() {
        return PROVIDER_KEY;
    }

    @Override
    public ScannerDataParser getInputHandler() {
        return inputHandler;
    }

}
