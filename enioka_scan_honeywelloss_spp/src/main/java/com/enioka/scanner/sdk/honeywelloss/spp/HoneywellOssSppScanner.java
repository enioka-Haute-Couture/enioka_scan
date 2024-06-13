package com.enioka.scanner.sdk.honeywelloss.spp;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerLedColor;
import com.enioka.scanner.api.proxies.ScannerCommandCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerInitCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.bt.api.BluetoothScanner;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.sdk.honeywelloss.spp.commands.ActivateTrigger;
import com.enioka.scanner.sdk.honeywelloss.spp.commands.Beep;
import com.enioka.scanner.sdk.honeywelloss.spp.commands.DeactivateTrigger;
import com.enioka.scanner.sdk.honeywelloss.spp.commands.DisableAimer;
import com.enioka.scanner.sdk.honeywelloss.spp.commands.DisableIllumination;
import com.enioka.scanner.sdk.honeywelloss.spp.commands.DisplayScreenColor;
import com.enioka.scanner.sdk.honeywelloss.spp.commands.EnableAimer;
import com.enioka.scanner.sdk.honeywelloss.spp.commands.EnableBarcodeMetadata;
import com.enioka.scanner.sdk.honeywelloss.spp.commands.EnableIllumination;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HoneywellOssSppScanner extends  HoneyWellOssSppPairing implements Scanner, Scanner.WithTriggerSupport, Scanner.WithBeepSupport, Scanner.WithLedSupport {
    private ScannerDataCallbackProxy dataCallback = null;
    private final BluetoothScanner btScanner;

    HoneywellOssSppScanner(BluetoothScanner btScanner) {
        this.btScanner = btScanner;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SOFTWARE TRIGGERS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void pressScanTrigger(@Nullable ScannerCommandCallbackProxy cb) {
        btScanner.runCommand(new ActivateTrigger(), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void releaseScanTrigger(@Nullable ScannerCommandCallbackProxy cb) {
        btScanner.runCommand(new DeactivateTrigger(), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
        this.btScanner.runCommand(new DisableAimer(), null);
        this.btScanner.runCommand(new DisableIllumination(), null);
        this.btScanner.runCommand(new DisplayScreenColor(ScannerLedColor.RED), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void resume(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new EnableAimer(), null);
        this.btScanner.runCommand(new EnableIllumination(), null);
        this.btScanner.runCommand(new DisplayScreenColor(null), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Beeps
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new Beep(), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepScanFailure(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new Beep(), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepPairingCompleted(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new Beep(), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LEDs
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void ledColorOn(ScannerLedColor color, @Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new DisplayScreenColor(color), null);

        // This is needed otherwise the color is reused for every default action afterwards!
        new Handler().postDelayed(() -> btScanner.runCommand(new DisplayScreenColor(null), null), 5000);

        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void ledColorOff(ScannerLedColor color, @Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new DisplayScreenColor(null), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Init and data cb
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getProviderKey() {
        return HoneywellOssSppScannerProvider.PROVIDER_KEY;
    }

    @Override
    public void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, final Mode mode, final Set<BarcodeType> symbologySelection) {
        this.dataCallback = dataCallback;

        this.btScanner.registerStatusCallback(statusCallback);

        this.btScanner.registerSubscription(new DataSubscriptionCallback<Barcode>() {
            @Override
            public void onSuccess(final Barcode data) {
                final List<Barcode> res = new ArrayList<>(1);
                res.add(data);
                HoneywellOssSppScanner.this.dataCallback.onData(HoneywellOssSppScanner.this, res);
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
