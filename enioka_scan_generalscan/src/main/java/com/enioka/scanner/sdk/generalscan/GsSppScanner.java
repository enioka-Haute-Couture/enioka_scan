package com.enioka.scanner.sdk.generalscan;

import android.content.Context;
import androidx.annotation.Nullable;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.proxies.ScannerCommandCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerInitCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.bt.api.BluetoothScanner;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.sdk.generalscan.commands.Bell;
import com.enioka.scanner.sdk.generalscan.commands.CloseRead;
import com.enioka.scanner.sdk.generalscan.commands.EnableBarcodeSuffix;
import com.enioka.scanner.sdk.generalscan.commands.OpenRead;
import com.enioka.scanner.sdk.generalscan.commands.SetBeepLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class GsSppScanner extends GsSppPairing implements Scanner, Scanner.WithBeepSupport {
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
    public void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, final Mode mode, final Set<BarcodeType> symbologySelection) {
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
    public void disconnect(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.disconnect();
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void pause(@Nullable ScannerCommandCallbackProxy cb) {
        // Not supported (confirmed by support)
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void resume(@Nullable ScannerCommandCallbackProxy cb) {
        // Not supported (confirmed by support)
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Beep
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new CloseRead(), null);
        this.btScanner.runCommand(new Bell(), null);
        this.btScanner.runCommand(new OpenRead(), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepScanFailure(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new CloseRead(), null);
        this.btScanner.runCommand(new Bell(), null);
        this.btScanner.runCommand(new OpenRead(), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepPairingCompleted(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new CloseRead(), null);
        this.btScanner.runCommand(new Bell(), null);
        this.btScanner.runCommand(new OpenRead(), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }
}
