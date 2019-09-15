package com.enioka.scanner.sdk.generalscan;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.enioka.scanner.bt.BtDevice;
import com.enioka.scanner.bt.BtSppScannerProvider;
import com.enioka.scanner.bt.BtSppScannerProviderServiceBinder;

public class GsSppScannerProvider extends Service implements BtSppScannerProvider {
    private final IBinder binder = new BtSppScannerProviderServiceBinder(this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean canManageDevice(BtDevice device) {
        return true;
    }

}
