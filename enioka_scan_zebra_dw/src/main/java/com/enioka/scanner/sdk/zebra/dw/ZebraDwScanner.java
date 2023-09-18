package com.enioka.scanner.sdk.zebra.dw;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.proxies.ScannerCommandCallbackProxy;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.helpers.intent.IntentScanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Scanner interface for Zebra DataWedge scanners
 */
public class ZebraDwScanner extends IntentScanner<String> implements Scanner.WithTriggerSupport, Scanner.WithIlluminationSupport {
    private static final String LOG_TAG = "ZebraDwScanner";

    private final Map<String, String> commandsWaitingForResult = new HashMap<>();
    private boolean illuminationOn = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "Received data from DW service");

        // Barcode?
        String barcodeData = intent.getStringExtra(ZebraDwIntents.DW_BARCODE_DATA_EXTRA);
        if (barcodeData != null && !barcodeData.isEmpty()) {
            List<Barcode> barcodes = new ArrayList<>();
            barcodes.add(new Barcode(barcodeData,
                    ZebraDwSymbology.getSymbology(intent.getStringExtra(ZebraDwIntents.DW_BARCODE_TYPE_EXTRA)).type));
            if (dataCb != null) {
                dataCb.onData(this, barcodes);
            }
        }

        // Command result?
        String command = intent.getStringExtra("COMMAND");
        if (command != null && !command.isEmpty()) {
            String commandIdentifier = intent.getStringExtra("COMMAND_IDENTIFIER");
            String result = intent.getStringExtra("RESULT");

            if (commandsWaitingForResult.containsKey(commandIdentifier)) {
                Log.i(LOG_TAG, "Received result for datawedge configuration command " + command + " -  " + result);
                commandsWaitingForResult.remove(commandIdentifier);
            } else {
                Log.w(LOG_TAG, "Received result for datawedge configuration command not initiated here");
            }
        }

        // Config query result?
        if (intent.getExtras() != null && intent.getExtras().containsKey("com.symbol.datawedge.api.RESULT_GET_CONFIG")) {
            ZebraDwConfig config = ZebraDwHelpers.getConfig(intent);
            if (config == null) {
                Log.w(LOG_TAG, "Could not query config - empty result");
            } else {
                Log.i(LOG_TAG, "Scanner configuration received");
                Log.i(LOG_TAG, config.toString());
                illuminationOn = !config.getParameter("BARCODE", "illumination_mode").equals("off");
            }
        }

        // Status? (explicit query)
        if (intent.getExtras() != null && intent.hasExtra("com.symbol.datawedge.api.RESULT_SCANNER_STATUS")) {
            String scannerStatus = intent.getStringExtra("com.symbol.datawedge.api.RESULT_SCANNER_STATUS");
            if (this.statusCb != null && scannerStatus != null) {
                this.statusCb.onStatusChanged(this, ZebraDwHelpers.getStatus(scannerStatus));
            }
            Log.d(LOG_TAG, "Scanner status:" + scannerStatus);
        }

        // Status notification?
        if (intent.getExtras() != null && intent.hasExtra(ZebraDwIntents.DW_NOTIFICATION_EXTRA)) {
            Bundle b = intent.getBundleExtra(ZebraDwIntents.DW_NOTIFICATION_EXTRA);
            if (b == null) {
                Log.w(LOG_TAG, "Incorrect DW notification: no DW_NOTIFICATION_EXTRA");
                return;
            }

            String notificationType = b.getString(ZebraDwIntents.DW_NOTIFICATION_TYPE_EXTRA);
            if (notificationType != null) {
                switch (notificationType) {
                    case ZebraDwIntents.DW_NOTIFICATION_CHANGE_STATUS:
                        String status = b.getString("STATUS");
                        Log.d(LOG_TAG, "SCANNER_STATUS: status: " + status + ", profileName: " + b.getString("PROFILE_NAME"));
                        if (this.statusCb != null && status != null) {
                            this.statusCb.onStatusChanged(this, ZebraDwHelpers.getStatus(status));
                        }
                        break;

                    case ZebraDwIntents.DW_NOTIFICATION_CHANGE_PROFILE:
                        Log.d(LOG_TAG, "PROFILE_SWITCH: profileName: " + b.getString("PROFILE_NAME") + ", profileEnabled: " + b.getBoolean("PROFILE_ENABLED"));
                        break;

                    case ZebraDwIntents.DW_NOTIFICATION_CHANGE_CONFIGURATION:
                        Log.d(LOG_TAG, "CONFIGURATION_UPDATE: status: " + b.getString("STATUS") + ", profileName: " + b.getString("PROFILE_NAME"));
                        break;

                    case ZebraDwIntents.DW_NOTIFICATION_CHANGE_WORKFLOW:
                        Log.d(LOG_TAG, "WORKFLOW_STATUS: status: " + b.getString("STATUS") + ", profileName: " + b.getString("PROFILE_NAME"));
                        break;
                }
            }
        }
    }

    @Override
    public String getProviderKey() {
        return ZebraDwProvider.PROVIDER_KEY;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // DW Intent creation
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void configureItem(String key, String value) {
        Log.d(LOG_TAG, "Configuring via intent " + key + " - " + value);
        Intent i = new Intent();
        i.setAction(ZebraDwIntents.DW_API_MAIN_ACTION);

        Bundle b = new Bundle();
        b.putString(key, value);

        i.putExtra(ZebraDwIntents.DW_API_PARAM_EXTRA, b);

        String cmdId = UUID.randomUUID().toString();
        i.putExtra(ZebraDwIntents.DW_API_CMD_ID_EXTRA, cmdId);
        i.putExtra("SEND_RESULT", "true");

        // Store the command - we are waiting for its result.
        commandsWaitingForResult.put(cmdId, key);

        // Go!
        this.broadcastIntent(i);
    }

    private void getConfig() {
        // Structure is :
        // Main extra is named com.symbol.datawedge.api.GET_CONFIG
        // For internal processes, its argument is a bundle:
        //      PROFILE_NAME -> string
        //      PLUGIN_CONFIG -> bundle
        //          PROCESS_PLUGIN_NAME -> list of bundles (DW plugins to query)
        //              PLUGIN_NAME/OUTPUT_PLUGIN_NAME -> String.
        //
        // For normal plugins:
        //      PROFILE_NAME -> String
        //      PLUGIN_CONFIG -> bundle
        //          PLUGIN_NAME -> string array list (the plugins to query)
        Bundle bundle = new Bundle();
        bundle.putString("PROFILE_NAME", "Profile0 (default)");
        Bundle pluginConfigBundle = new Bundle();

        ArrayList<String> pluginNameList = new ArrayList<>();
        pluginNameList.add("BARCODE");
        pluginNameList.add("INTENT");

        pluginConfigBundle.putStringArrayList("PLUGIN_NAME", pluginNameList);
        bundle.putBundle("PLUGIN_CONFIG", pluginConfigBundle);

        Intent i = new Intent();
        i.setAction(ZebraDwIntents.DW_API_MAIN_ACTION);
        i.putExtra("com.symbol.datawedge.api.GET_CONFIG", bundle);

        Log.d(LOG_TAG, bundle.toString());
        Log.i(LOG_TAG, "Sending configuration query");
        this.broadcastIntent(i);
    }

    private void getScannerStatus() {
        Map<String, String> extras = new HashMap<>(2);
        extras.put("SEND_RESULT", "true");
        extras.put(ZebraDwIntents.DW_API_GET_STATUS_EXTRA, "");
        broadcastIntent(ZebraDwIntents.DW_API_MAIN_ACTION, extras);
    }

    private void notificationSubscription(Context ctx) {
        // Register for status changes
        Bundle b = new Bundle();
        b.putString("com.symbol.datawedge.api.APPLICATION_NAME", ctx.getPackageName());
        b.putString("com.symbol.datawedge.api.NOTIFICATION_TYPE", "SCANNER_STATUS");
        Intent i = new Intent();
        i.setAction(ZebraDwIntents.DW_API_MAIN_ACTION);
        i.putExtra("com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION", b);
        broadcastIntent(i);

        // Register for profile changes
        b = new Bundle();
        b.putString("com.symbol.datawedge.api.APPLICATION_NAME", ctx.getPackageName());
        b.putString("com.symbol.datawedge.api.NOTIFICATION_TYPE", "PROFILE_SWITCH");
        i = new Intent();
        i.setAction(ZebraDwIntents.DW_API_MAIN_ACTION);
        i.putExtra("com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION", b);
        broadcastIntent(i);

        // Register for config changes
        b = new Bundle();
        b.putString("com.symbol.datawedge.api.APPLICATION_NAME", ctx.getPackageName());
        b.putString("com.symbol.datawedge.api.NOTIFICATION_TYPE", "CONFIGURATION_UPDATE");
        i = new Intent();
        i.setAction(ZebraDwIntents.DW_API_MAIN_ACTION);
        i.putExtra("com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION", b);
        broadcastIntent(i);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void configureProvider(final Context applicationContext) {
        String mainCallback = applicationContext.getResources().getString(R.string.com_enioka_scanners_zebra_dw_intent_callback_name);
        if (mainCallback.isEmpty()) {
            mainCallback = ZebraDwIntents.DW_MAIN_CALLBACK_ACTION;
        }

        broadcastIntentFilters.add(mainCallback);
        broadcastIntentFilters.add(ZebraDwIntents.DW_CONFIGURATION_CALLBACK_ACTION);
        broadcastIntentFilters.add(ZebraDwIntents.DW_NOTIFICATION_ACTION);

        disableScanner = newIntent(ZebraDwIntents.DW_API_MAIN_ACTION, "com.symbol.datawedge.api.ENABLE_DATAWEDGE", true);
        enableScanner = newIntent(ZebraDwIntents.DW_API_MAIN_ACTION, "com.symbol.datawedge.api.ENABLE_DATAWEDGE", false);
    }

    @Override
    protected void configureAfterInit(Context ctx) {
        super.configureAfterInit(ctx);

        // We want to know what is happening on the device, at least for logs
        notificationSubscription(ctx);

        // Retrieve current config
        getConfig();

        // Retrieve current scanner status
        getScannerStatus();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SOFTWARE TRIGGERS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void pressScanTrigger(@Nullable ScannerCommandCallbackProxy cb) {
        broadcastIntent(newIntent("com.symbol.datawedge.api.ACTION", "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "START_SCANNING"));
    }

    @Override
    public void releaseScanTrigger(@Nullable ScannerCommandCallbackProxy cb) {
        broadcastIntent(newIntent("com.symbol.datawedge.api.ACTION", "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "STOP_SCANNING"));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Illumination
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void enableIllumination(@Nullable ScannerCommandCallbackProxy cb) {
        configureItem("illumination_mode", "torch");
        illuminationOn = true;
    }

    @Override
    public void disableIllumination(@Nullable ScannerCommandCallbackProxy cb) {
        configureItem("illumination_mode", "off");
        illuminationOn = false;
    }

    @Override
    public void toggleIllumination(@Nullable ScannerCommandCallbackProxy cb) {
        configureItem("illumination_mode", illuminationOn ? "off" : "torch");
        illuminationOn = !illuminationOn;
    }

    @Override
    public boolean isIlluminationOn() {
        return illuminationOn;
    }
}
