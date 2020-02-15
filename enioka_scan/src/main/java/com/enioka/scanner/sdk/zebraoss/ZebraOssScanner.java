package com.enioka.scanner.sdk.zebraoss;

import android.content.Context;
import android.os.Handler;

import com.enioka.scanner.R;
import com.enioka.scanner.api.Color;
import com.enioka.scanner.api.ScannerBackground;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.bt.api.Scanner;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.sdk.zebraoss.commands.Beep;
import com.enioka.scanner.sdk.zebraoss.commands.InitCommand;
import com.enioka.scanner.sdk.zebraoss.commands.LedOff;
import com.enioka.scanner.sdk.zebraoss.commands.LedOn;
import com.enioka.scanner.sdk.zebraoss.commands.ScanDisable;
import com.enioka.scanner.sdk.zebraoss.commands.ScanEnable;

import java.util.ArrayList;
import java.util.List;

class ZebraOssScanner implements ScannerBackground {
    private ScannerDataCallback dataCallback = null;
    private final com.enioka.scanner.bt.api.Scanner btScanner;

    ZebraOssScanner(com.enioka.scanner.bt.api.Scanner btScanner) {
        this.btScanner = btScanner;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
        this.btScanner.runCommand(new ScanDisable(), null);
    }

    @Override
    public void resume() {
        this.btScanner.runCommand(new ScanEnable(), null);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Beeps
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful() {
        this.btScanner.runCommand(new Beep((byte) 0x01), null);
    }

    @Override
    public void beepScanFailure() {
        this.btScanner.runCommand(new Beep((byte) 0x12), null);
    }

    @Override
    public void beepPairingCompleted() {
        this.btScanner.runCommand(new Beep((byte) 0x14), null);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Illumination
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void enableIllumination() {

    }

    @Override
    public void disableIllumination() {

    }

    @Override
    public void toggleIllumination() {

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
    // LEDs
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void ledColorOn(Color color) {
        this.btScanner.runCommand(new LedOn(color), null);
    }

    @Override
    public void ledColorOff(Color color) {
        this.btScanner.runCommand(new LedOff(), null);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Init and data cb
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getProviderKey() {
        return "BtSppSdk";
    }

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
                statusCallback.onScannerReconnecting(ZebraOssScanner.this);
            }

            @Override
            public void onScannerDisconnected() {
                statusCallback.onStatusChanged(applicationContext.getString(R.string.scanner_status_lost));
                statusCallback.onScannerDisconnected(ZebraOssScanner.this);
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
                        ZebraOssScanner.this.dataCallback.onData(ZebraOssScanner.this, res);
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

        //this.btScanner.runCommand(new InitCommand(), null);
        //this.btScanner.runCommand(new ScanEnable(), null);
        //this.btScanner.runCommand(new StartSession(), null);
        //this.btScanner.runCommand(new RequestParam(), null);

        // We are already connected if the scanner could be created...
        initCallback.onConnectionSuccessful(this);
    }
}
