package com.enioka.scanner.sdk.honeywelloss;

import android.content.Context;
import android.os.Handler;

import com.enioka.scanner.R;
import com.enioka.scanner.api.Color;
import com.enioka.scanner.api.ScannerBackground;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.bt.api.Scanner;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.helpers.ScannerDataCallbackProxy;
import com.enioka.scanner.helpers.ScannerInitCallbackProxy;
import com.enioka.scanner.helpers.ScannerStatusCallbackProxy;
import com.enioka.scanner.sdk.honeywelloss.commands.ActivateTrigger;
import com.enioka.scanner.sdk.honeywelloss.commands.Beep;
import com.enioka.scanner.sdk.honeywelloss.commands.DeactivateTrigger;
import com.enioka.scanner.sdk.honeywelloss.commands.DisableAimer;
import com.enioka.scanner.sdk.honeywelloss.commands.DisableIllumination;
import com.enioka.scanner.sdk.honeywelloss.commands.DisplayScreenColor;
import com.enioka.scanner.sdk.honeywelloss.commands.EnableAimer;
import com.enioka.scanner.sdk.honeywelloss.commands.EnableBarcodeMetadata;
import com.enioka.scanner.sdk.honeywelloss.commands.EnableIllumination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HoneywellOssScanner implements ScannerBackground {
    private ScannerDataCallbackProxy dataCallback = null;
    private final Scanner btScanner;

    HoneywellOssScanner(Scanner btScanner) {
        this.btScanner = btScanner;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
        this.btScanner.runCommand(new DeactivateTrigger(), null);
        this.btScanner.runCommand(new DisableAimer(), null);
        this.btScanner.runCommand(new DisableIllumination(), null);
        this.btScanner.runCommand(new DisplayScreenColor(Color.RED), null);
    }

    @Override
    public void resume() {
        this.btScanner.runCommand(new ActivateTrigger(), null);
        this.btScanner.runCommand(new EnableAimer(), null);
        this.btScanner.runCommand(new EnableIllumination(), null);
        this.btScanner.runCommand(new DisplayScreenColor(null), null);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Beeps
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful() {
        this.btScanner.runCommand(new Beep(), null);
    }

    @Override
    public void beepScanFailure() {
        this.btScanner.runCommand(new Beep(), null);
    }

    @Override
    public void beepPairingCompleted() {
        this.btScanner.runCommand(new Beep(), null);
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
        this.btScanner.runCommand(new DisplayScreenColor(color), null);

        // This is needed otherwise the color is reused for every default action afterwards!
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btScanner.runCommand(new DisplayScreenColor(null), null);
            }
        }, 5000);
    }

    @Override
    public void ledColorOff(Color color) {
        this.btScanner.runCommand(new DisplayScreenColor(null), null);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // INVENTORY
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public String getStatus(String key) {
        return null;
    }

    public String getStatus(String key, boolean allowCache) {
        return null;
    }

    public Map<String, String> getStatus() {
        return new HashMap<>();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Init and data cb
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getProviderKey() {
        return "BtSppSdk";
    }

    @Override
    public void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, Mode mode) {
        this.dataCallback = dataCallback;

        this.btScanner.registerStatusCallback(new Scanner.SppScannerStatusCallback() {
            @Override
            public void onScannerConnected() {
                statusCallback.onStatusChanged(applicationContext.getString(R.string.scanner_status_connected));
            }

            @Override
            public void onScannerReconnecting() {
                statusCallback.onStatusChanged(applicationContext.getString(R.string.scanner_status_reconnecting));
                statusCallback.onScannerReconnecting(HoneywellOssScanner.this);
            }

            @Override
            public void onScannerDisconnected() {
                statusCallback.onStatusChanged(applicationContext.getString(R.string.scanner_status_lost));
                statusCallback.onScannerDisconnected(HoneywellOssScanner.this);
            }
        });

        this.btScanner.registerSubscription(new DataSubscriptionCallback<Barcode>() {
            @Override
            public void onSuccess(final Barcode data) {
                final List<Barcode> res = new ArrayList<>(1);
                res.add(data);
                HoneywellOssScanner.this.dataCallback.onData(HoneywellOssScanner.this, res);
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

        //this.btScanner.runCommand(new Cleanup(), null);

        resume();
        this.btScanner.runCommand(new EnableBarcodeMetadata(), null);

        // We are already connected if the scanner could be created...
        initCallback.onConnectionSuccessful(this);
    }
}
