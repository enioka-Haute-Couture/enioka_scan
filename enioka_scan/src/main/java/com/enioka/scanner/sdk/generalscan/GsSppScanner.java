package com.enioka.scanner.sdk.generalscan;

import android.content.Context;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerInitCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.bt.api.BluetoothScanner;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.sdk.generalscan.commands.Bell;
import com.enioka.scanner.sdk.generalscan.commands.CloseRead;
import com.enioka.scanner.sdk.generalscan.commands.EnableBarcodeSuffix;
import com.enioka.scanner.sdk.generalscan.commands.OpenRead;
import com.enioka.scanner.sdk.generalscan.commands.SetBeepLevel;

import java.util.ArrayList;
import java.util.List;

class GsSppScanner implements Scanner, Scanner.WithBeepSupport {
    private ScannerDataCallbackProxy dataCallback = null;
    private final BluetoothScanner btScanner;

    GsSppScanner(BluetoothScanner btScanner) {
        this.btScanner = btScanner;
    }

    @Override
    public String getProviderKey() {
        return GsSppScannerProvider.PROVIDER_KEY;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, final Mode mode) {
        this.dataCallback = dataCallback;
        this.btScanner.registerStatusCallback(statusCallback);

        this.btScanner.registerSubscription(new DataSubscriptionCallback<Barcode>() {
            @Override
            public void onSuccess(final Barcode data) {
                final List<Barcode> res = new ArrayList<>(1);
                res.add(data);
                GsSppScanner.this.dataCallback.onData(GsSppScanner.this, res);

            }

            @Override
            public void onFailure() {
                // TODO
            }

            @Override
            public void onTimeout() {
                // Ignore - no timeouts on persistent subscriptions.
            }
        }, Barcode.class);

        // Without a suffix, we cannot parse results. Great.
        this.btScanner.runCommand(new EnableBarcodeSuffix(), null);
        this.btScanner.runCommand(new SetBeepLevel(2), null);

        // We are already connected if the scanner could be created...
        initCallback.onConnectionSuccessful(this);
    }

    @Override
    public void setDataCallBack(ScannerDataCallbackProxy cb) {
        this.dataCallback = cb;
    }

    @Override
    public void disconnect() {
        this.btScanner.disconnect();
    }

    @Override
    public void pause() {
        // Not supported (confirmed by support)
    }

    @Override
    public void resume() {
        // Not supported (confirmed by support)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Beep
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful() {
        this.btScanner.runCommand(new CloseRead(), null);
        this.btScanner.runCommand(new Bell(), null);
        this.btScanner.runCommand(new OpenRead(), null);
    }

    @Override
    public void beepScanFailure() {
        this.btScanner.runCommand(new CloseRead(), null);
        this.btScanner.runCommand(new Bell(), null);
        this.btScanner.runCommand(new OpenRead(), null);
    }

    @Override
    public void beepPairingCompleted() {
        this.btScanner.runCommand(new CloseRead(), null);
        this.btScanner.runCommand(new Bell(), null);
        this.btScanner.runCommand(new OpenRead(), null);
    }
}
