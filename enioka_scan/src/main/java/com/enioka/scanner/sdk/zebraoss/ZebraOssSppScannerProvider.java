package com.enioka.scanner.sdk.zebraoss;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.enioka.scanner.bt.BtDevice;
import com.enioka.scanner.bt.BtInputHandler;
import com.enioka.scanner.bt.BtSppScannerProvider;
import com.enioka.scanner.bt.BtSppScannerProviderServiceBinder;

public class ZebraOssSppScannerProvider extends Service implements BtSppScannerProvider {
    private final IBinder binder = new BtSppScannerProviderServiceBinder(this);

    private final BtInputHandler inputHandler = new SsiParser();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean canManageDevice(BtDevice device) {
        return true;
    }

    @Override
    public BtInputHandler getInputHandler() {
        return inputHandler;
    }

}
