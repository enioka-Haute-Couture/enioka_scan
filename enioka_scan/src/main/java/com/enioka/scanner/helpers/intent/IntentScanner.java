package com.enioka.scanner.helpers.intent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.callbacks.ScannerCommandCallback;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.api.proxies.ScannerCommandCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerInitCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.helpers.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Many devices actually only allow to communicate with a scanner through an Android service
 * (be it a system service or a service provided by another app).<br>
 * This class factors all the boilerplate code to communicate with such services.
 */
public abstract class IntentScanner<BarcodeTypeClass> extends BroadcastReceiver implements Scanner, Scanner.WithBeepSupport {

    private Context ctx;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // PARAMETERS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The broadcast Action this scanner should subscribe to.
     */
    protected List<String> broadcastIntentFilters = new ArrayList<>();

    /**
     * A translation table between vendor-specific codification and our own barcode API.
     */
    protected final Map<BarcodeTypeClass, BarcodeType> sdk2Api = new HashMap<>();

    protected Intent disableTrigger = null;
    protected Intent enableTrigger = null;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected ScannerDataCallbackProxy dataCb = null;
    protected ScannerStatusCallbackProxy statusCb = null;
    protected Scanner.Mode mode;


    @Override
    public void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, final Mode mode) {
        this.dataCb = dataCallback;
        this.statusCb = statusCallback;
        this.mode = mode;
        this.ctx = applicationContext;

        // Let the child provider set all the configuration values if needed.
        configureProvider();

        // Register the broadcast receiver.
        registerReceivers(ctx, initCallback, statusCallback);

        // Let the child provider do anything it wants with the scanner (like setting available sympbologies).
        configureAfterInit(ctx);

        // Done - signal client.
        if (initCallback != null) {
            initCallback.onConnectionSuccessful(this);
        }

        if (this.statusCb != null) {
            this.statusCb.onStatusChanged(this, ScannerStatusCallback.Status.READY);
        }
    }

    protected void registerReceivers(Context ctx, ScannerInitCallbackProxy initCallback, ScannerStatusCallbackProxy statusCallback) {
        IntentFilter intentFilter = new IntentFilter();
        for (String f : broadcastIntentFilters) {
            Log.i(getProviderKey(), "Registering an intent receiver with intent filter " + f);
            intentFilter.addAction(f);
        }
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        ctx.registerReceiver(this, intentFilter);
    }

    /**
     * Called just before initial scanner initialization. Empty by default. An occasion to set the configuration fields.
     */
    protected void configureProvider() {
    }

    /**
     * Called just after initial scanner initialization. Empty by default. An occasion to set different parameters on the scanner, like enabled symbologies.
     */
    protected void configureAfterInit(Context ctx) {
    }

    @Override
    public void setDataCallBack(ScannerDataCallbackProxy cb) {
        this.dataCb = cb;
    }

    @Override
    public void disconnect(@Nullable ScannerCommandCallbackProxy cb) {
        Log.d(getProviderKey(), "Removing subscription to scanner service");
        ctx.unregisterReceiver(this);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void pause(@Nullable ScannerCommandCallbackProxy cb) {
        if (disableTrigger != null) {
            Log.d(getProviderKey(), "Sending intent to scanner to disable the trigger");
            ctx.sendBroadcast(disableTrigger);
            if (cb != null) {
                cb.onSuccess();
            }
        } else if (cb != null) {
            cb.onFailure();
        }
    }

    @Override
    public void resume(@Nullable ScannerCommandCallbackProxy cb) {
        if (enableTrigger != null) {
            Log.d(getProviderKey(), "Sending intent to scanner to enable the trigger");
            ctx.sendBroadcast(enableTrigger);
            if (cb != null) {
                cb.onSuccess();
            }
        } else if (cb != null) {
            cb.onFailure();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BEEPS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful(@Nullable ScannerCommandCallbackProxy cb) {
        Common.beepScanSuccessful();
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepScanFailure(@Nullable ScannerCommandCallbackProxy cb) {
        Common.beepScanFailure();
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepPairingCompleted(@Nullable ScannerCommandCallbackProxy cb) {
        Common.beepPairingCompleted();
        if (cb != null) {
            cb.onSuccess();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // EXTERNAL INTENT SERVICE CALLBACK
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected BarcodeType getType(BarcodeTypeClass sdkType) {
        BarcodeType res = sdk2Api.get(sdkType);
        if (res == null) {
            return BarcodeType.UNKNOWN;
        }
        return res;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // INTENT HELPERS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void broadcastIntent(Intent intent) {
        Log.d("BtSppSdk", "Broadcasting new Intent with action " + intent.getAction());
        ctx.sendBroadcast(intent);
    }

    protected void broadcastIntent(String action, Map<String, String> extras) {
        Intent intent = newIntent(action, extras);
        broadcastIntent(intent);
    }

    protected void broadcastIntent(String action, String parameter1Key, boolean parameter1Value) {
        Intent intent = newIntent(action, parameter1Key, parameter1Value);
        broadcastIntent(intent);
    }

    protected void broadcastIntent(String action, String parameter1Key, String parameter1Value) {
        Intent intent = newIntent(action, parameter1Key, parameter1Value);
        broadcastIntent(intent);
    }

    protected void broadcastIntent(String action, String parameter1Key, int parameter1Value) {
        Intent intent = newIntent(action, parameter1Key, parameter1Value);
        broadcastIntent(intent);
    }

    protected void broadcastIntent(String action, String parameter1Key, String[] parameter1Value) {
        Intent intent = newIntent(action, parameter1Key, parameter1Value);
        broadcastIntent(intent);
    }

    protected void broadcastIntent(String action) {
        Intent intent = newIntent(action);
        broadcastIntent(intent);
    }

    protected Intent newIntent(String action, Map<String, String> extras) {
        Intent intent = new Intent();
        intent.setAction(action);
        for (Map.Entry<String, String> entry : extras.entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }
        return intent;
    }

    protected Intent newIntent(String action, String parameter1Key, boolean parameter1Value) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(parameter1Key, parameter1Value);
        return intent;
    }

    protected Intent newIntent(String action, String parameter1Key, String parameter1Value) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(parameter1Key, parameter1Value);
        return intent;
    }

    protected Intent newIntent(String action, String parameter1Key, int parameter1Value) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(parameter1Key, parameter1Value);
        return intent;
    }

    protected Intent newIntent(String action, String parameter1Key, String[] parameter1Value) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(parameter1Key, parameter1Value);
        return intent;
    }

    protected Intent newIntent(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        return intent;
    }

    protected void startActivity(Intent intent) {
        ctx.startActivity(intent);
    }
}
