package com.enioka.scanner.sdk.generalscan;

import android.content.Context;
import android.os.Handler;

import com.enioka.scanner.R;
import com.enioka.scanner.api.Color;
import com.enioka.scanner.api.ScannerBackground;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.bt.api.Scanner;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.sdk.generalscan.commands.Bell;
import com.enioka.scanner.sdk.generalscan.commands.CloseRead;
import com.enioka.scanner.sdk.generalscan.commands.EnableBarcodeSuffix;
import com.enioka.scanner.sdk.generalscan.commands.OpenRead;
import com.enioka.scanner.sdk.generalscan.commands.SetBeepLevel;

import java.util.ArrayList;
import java.util.List;

class GsSppScanner implements ScannerBackground {
    private ScannerDataCallback dataCallback = null;
    private final com.enioka.scanner.bt.api.Scanner btScanner;

    GsSppScanner(com.enioka.scanner.bt.api.Scanner btScanner) {
        this.btScanner = btScanner;
    }

    @Override
    public String getProviderKey() {
        return "BtSppSdk";
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialize(final Context applicationContext, ScannerInitCallback initCallback, ScannerDataCallback dataCallback, final ScannerStatusCallback statusCallback, Mode mode) {
        this.dataCallback = dataCallback;

        final Handler uiHandler = new Handler(applicationContext.getMainLooper());

        this.btScanner.registerStatusCallback(new Scanner.SppScannerStatusCallback() {
            @Override
            public void onScannerConnected() {
                statusCallback.onStatusChanged(applicationContext.getString(R.string.scanner_status_connected));
            }

            @Override
            public void onScannerReconnecting() {
                statusCallback.onStatusChanged(applicationContext.getString(R.string.scanner_status_reconnecting));
                statusCallback.onScannerReconnecting(GsSppScanner.this);
            }

            @Override
            public void onScannerDisconnected() {
                statusCallback.onStatusChanged(applicationContext.getString(R.string.scanner_status_lost));
                statusCallback.onScannerDisconnected(GsSppScanner.this);
            }
        });

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
        this.btScanner.runCommand(new SetBeepLevel(2), null);

        // We are already connected if the scanner could be created...
        initCallback.onConnectionSuccessful(this);
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


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Illumination
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void enableIllumination() {
        // No illumination on device.
    }

    @Override
    public void disableIllumination() {
        // No illumination on device.
    }

    @Override
    public void toggleIllumination() {
        // No illumination on device.
    }

    @Override
    public boolean isIlluminationOn() {
        return false;
    }

    @Override
    public boolean supportsIllumination() {
        return false;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LED
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void ledColorOn(Color color) {
        // No programmable LED on device.
    }

    @Override
    public void ledColorOff(Color color) {
        // No programmable LED on device.
    }
}