package com.enioka.scanner.sdk.generalscan;

import android.content.Context;
import android.os.Handler;

import com.enioka.scanner.api.Color;
import com.enioka.scanner.api.ScannerBackground;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.sdk.generalscan.commands.Bell;
import com.enioka.scanner.sdk.generalscan.commands.CloseRead;
import com.enioka.scanner.sdk.generalscan.commands.EnableBarcodeSuffix;
import com.enioka.scanner.sdk.generalscan.commands.OpenRead;

import java.util.ArrayList;
import java.util.List;

class GsSppScanner implements ScannerBackground {
    private ScannerDataCallback dataCallback = null;
    private final com.enioka.scanner.bt.api.Scanner btScanner;

    GsSppScanner(com.enioka.scanner.bt.api.Scanner btScanner) {
        this.btScanner = btScanner;
    }

    @Override
    public void setDataCallBack(ScannerDataCallback cb) {
        this.dataCallback = cb;
    }

    @Override
    public void disconnect() {
        this.btScanner.disconnect();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void beepScanSuccessful() {
        this.btScanner.runCommand(new CloseRead(), null);
        this.btScanner.runCommand(new Bell(), null);
        this.btScanner.runCommand(new OpenRead(), null);
    }

    @Override
    public void beepScanFailure() {

    }

    @Override
    public void beepPairingCompleted() {

    }

    @Override
    public void enableIllumination() {

    }

    @Override
    public void disableIllumination() {

    }

    @Override
    public void toggleIllumination() {
        this.beepScanSuccessful();
    }

    @Override
    public boolean isIlluminationOn() {
        return false;
    }

    @Override
    public void ledColorOn(Color color) {

    }

    @Override
    public void ledColorOff(Color color) {

    }

    @Override
    public boolean supportsIllumination() {
        return false;
    }

    @Override
    public String getProviderKey() {
        return "BtSppSdk";
    }

    @Override
    public void initialize(Context applicationContext, ScannerInitCallback initCallback, ScannerDataCallback dataCallback, ScannerStatusCallback statusCallback, Mode mode) {
        this.dataCallback = dataCallback;

        final Handler uiHandler = new Handler(applicationContext.getMainLooper());

        this.btScanner.registerSubscription(new DataSubscriptionCallback<Barcode>() {
            @Override
            public void onSuccess(final Barcode data) {
                final List<Barcode> res = new ArrayList<>(1);
                res.add(data);
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        GsSppScanner.this.dataCallback.onData(GsSppScanner.this, res);
                    }
                });

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

        // We are already connected if the scanner could be created...
        initCallback.onConnectionSuccessful(this);
    }
}
