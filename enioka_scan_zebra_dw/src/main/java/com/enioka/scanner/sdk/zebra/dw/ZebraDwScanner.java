package com.enioka.scanner.sdk.zebra.dw;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.proxies.ScannerCommandCallbackProxy;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.helpers.intent.IntentScanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scanner interface for Zebra DataWedge scanners
 */
public class ZebraDwScanner extends IntentScanner<String> implements Scanner.WithTriggerSupport, Scanner.WithIlluminationSupport {
    private static final String LOG_TAG = "ZebraDwScanner";

    private final Map<String, ZebraDwCommand> commandsWaitingForResult = new HashMap<>();
    private boolean illuminationOn = false;
    private String profileName;
    private String dwIntentPluginActionName;

    private ZebraDwConfig currentConfig;
    private String currentlyActiveProfile = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Avoid re-config loops in case another enioka_scan instance is running in the foreground
        if (paused)
            return;

        Log.d(LOG_TAG, "Received data from DW service");

        // Barcode?
        String barcodeData = intent.getStringExtra(ZebraDwIntents.DW_BARCODE_DATA_EXTRA);
        if (barcodeData != null && !barcodeData.isEmpty()) {
            List<Barcode> barcodes = new ArrayList<>();
            barcodes.add(new Barcode(barcodeData,
                    ZebraDwSymbology.getSymbology(intent.getStringExtra(ZebraDwIntents.DW_BARCODE_TYPE_EXTRA)).type));
            Log.d(LOG_TAG, "Received scan result: " + barcodes.get(0).getBarcode() + " (" + barcodes.get(0).getBarcodeType().toString() + ")");
            if (dataCb != null) {
                dataCb.onData(this, barcodes);
            }
        }

        // Command/action result?
        String command = intent.getStringExtra("COMMAND");
        if (intent.getAction() != null && intent.getAction().equals("com.symbol.datawedge.api.RESULT_ACTION")) {
            String commandIdentifier = intent.getStringExtra("COMMAND_IDENTIFIER");
            if (commandIdentifier == null && intent.getExtras() != null) {
                // Some commands do not obey the COMMAND_IDENTIFIER extra... in this case we use the extra name.
                commandIdentifier = intent.getExtras().keySet().iterator().next();
            }
            String result = intent.getStringExtra("RESULT");

            Log.d(LOG_TAG, "Received result for datawedge command " + command + " -  " + result + " - " + commandIdentifier);
            if (commandsWaitingForResult.containsKey(commandIdentifier)) {
                ZebraDwCommand cmd = commandsWaitingForResult.remove(commandIdentifier);
                if (cmd != null && cmd.getCallback() != null) {
                    if (result == null || result.equals("SUCCESS")) { // null result means COMMAND_IDENTIFIER not supported by this command.
                        cmd.getCallback().onSuccess(intent);
                    } else {
                        cmd.getCallback().onFailure(intent);
                    }
                }
            } else {
                Log.d(LOG_TAG, "Received result for datawedge command " + command + " not initiated here: " + commandIdentifier);
                if (intent.getExtras() != null) {
                    Log.d(LOG_TAG, intent.getExtras().toString());
                }
            }
        }

        // Config query result?
        if (intent.getExtras() != null && intent.getExtras().containsKey("com.symbol.datawedge.api.RESULT_GET_CONFIG")) {
            currentConfig = ZebraDwHelpers.getConfig(intent);
            if (currentConfig == null) {
                Log.w(LOG_TAG, "Could not query config - empty result");
            } else {
                Log.i(LOG_TAG, "Scanner configuration received");
                Log.i(LOG_TAG, currentConfig.toString());
                if (this.currentlyActiveProfile != null && this.currentlyActiveProfile.equals(currentConfig.getProfileName())) {
                    configureSymbologies();
                }
                final String illumination_mode = currentConfig.getParameter("BARCODE", "illumination_mode");
                illuminationOn = illumination_mode != null && !illumination_mode.equals("off");
            }
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
                        Log.i(LOG_TAG, "PROFILE_SWITCH: profileName: " + b.getString("PROFILE_NAME") + ", profileEnabled: " + b.getBoolean("PROFILE_ENABLED"));
                        currentlyActiveProfile = b.getString("PROFILE_NAME");
                        if (currentConfig != null && profileName.equals(currentlyActiveProfile)) {
                            Log.d(LOG_TAG, "Correct profile, configuring symbologies");
                            configureSymbologies();
                        }
                        if (!paused && !profileName.equals(currentlyActiveProfile)) {
                            // DW will change the profile between activities and apps. We must check each time a switch is done.
                            Log.d(LOG_TAG, "Incorrect profile " + currentlyActiveProfile + ", requesting profile change to " + profileName);
                            switchToOurProfile();
                        }
                        break;

                    case ZebraDwIntents.DW_NOTIFICATION_CHANGE_CONFIGURATION:
                        Log.i(LOG_TAG, "CONFIGURATION_UPDATE: status: " + b.getString("STATUS") + ", profileName: " + b.getString("PROFILE_NAME"));
                        break;

                    case ZebraDwIntents.DW_NOTIFICATION_CHANGE_WORKFLOW:
                        Log.d(LOG_TAG, "WORKFLOW_STATUS: status: " + b.getString("STATUS") + ", profileName: " + b.getString("PROFILE_NAME"));
                        break;

                    default:
                        Log.d(LOG_TAG, "Other notification: " + notificationType + " - status: " + b.getString("STATUS") + ", profileName: " + b.getString("PROFILE_NAME"));
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

    private void configureRuntimeItem(String key, String value) {
        Bundle b = new Bundle();
        b.putString(key, value);
        ZebraDwCommand cmd = ZebraDwCommand.create().putExtra(ZebraDwIntents.DW_API_PARAM_EXTRA, b)
                .setCallback(intent -> Log.d(LOG_TAG, "DW action is done - configured [" + key + "] to value [" + value + "]"));

        // Go!
        this.broadcastDwAction(cmd);
    }

    private void configureRuntimeItems(Map<String, String> items) {
        Bundle b = new Bundle();
        for (Map.Entry<String, String> e : items.entrySet()) {
            b.putString(e.getKey(), e.getValue());
        }
        ZebraDwCommand cmd = ZebraDwCommand.create().putExtra(ZebraDwIntents.DW_API_PARAM_EXTRA, b)
                .setCallback(intent -> Log.i(LOG_TAG, "DW action is done - configured " + items.toString()));

        // Go!
        this.broadcastDwAction(cmd);
    }

    private void getConfig(String bundleToQuery) {
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
        bundle.putString("PROFILE_NAME", bundleToQuery); // "Profile0 (default)");
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
        Log.d(LOG_TAG, "Sending configuration query");
        this.broadcastIntent(i);
    }

    private void createProfile() {
        broadcastDwAction(ZebraDwCommand.create("com.symbol.datawedge.api.CREATE_PROFILE", profileName).setCallback(intent -> {
            Log.i(LOG_TAG, "Profile " + profileName + " was created successfully");
            configureProfile();
        }));
    }

    private void queryProfileList() {
        ZebraDwCommand cmd = ZebraDwCommand.create()
                .putExtra("com.symbol.datawedge.api.GET_PROFILES_LIST", "")
                .setExpectedResultKey("com.symbol.datawedge.api.RESULT_GET_PROFILES_LIST") // No
                .setCallback(intent -> {
                    if (intent.hasExtra("com.symbol.datawedge.api.RESULT_GET_PROFILES_LIST")) {
                        String[] profilesList = intent.getStringArrayExtra("com.symbol.datawedge.api.RESULT_GET_PROFILES_LIST");
                        if (profilesList == null) {
                            throw new IllegalArgumentException("a datawedge intent must have a RESULT_GET_PROFILES_LIST extra when listing profiles");
                        }

                        Log.i(LOG_TAG, "The following DW profiles already exist:");
                        for (String profile : profilesList) {
                            Log.i(LOG_TAG, "\t" + profile);
                        }

                        boolean profileFound = false;
                        for (String profile : profilesList) {
                            if (profile.equals(profileName)) {
                                profileFound = true;
                                break;
                            }
                        }
                        if (profileFound) {
                            Log.i(LOG_TAG, "Profile " + profileName + " already exists");
                            configureProfile();
                        } else {
                            Log.i(LOG_TAG, "Profile " + profileName + " does not exist - begin creation");
                            createProfile();
                        }
                    } else {
                        throw new IllegalArgumentException("A datawedge intent must have a RESULT_GET_PROFILES_LIST extra when listing profiles");
                    }
                });

        broadcastDwAction(cmd);
    }

    private void queryActiveProfile() {
        broadcastDwAction(ZebraDwCommand.create("com.symbol.datawedge.api.GET_ACTIVE_PROFILE", "")
                .setExpectedResultKey("com.symbol.datawedge.api.RESULT_GET_ACTIVE_PROFILE")
                .setCallback(intent -> {
                    if (intent.hasExtra("com.symbol.datawedge.api.RESULT_GET_ACTIVE_PROFILE")) {
                        currentlyActiveProfile = intent.getExtras().getString("com.symbol.datawedge.api.RESULT_GET_ACTIVE_PROFILE");
                        Log.i(LOG_TAG, "Active profile is " + currentlyActiveProfile);

                        if (currentlyActiveProfile.equals(profileName)) {
                            // No need to create, just reconfigure
                            Log.d(LOG_TAG, "Correct profile, configuring profile");
                            configureProfile();
                        } else {
                            Log.d(LOG_TAG, "Incorrect profile " + currentlyActiveProfile + ", requesting profile change to " + profileName);
                            queryProfileList();
                        }
                    }
                }));
    }

    private void configureProfile() {
        // Root config
        Bundle rootBundle = new Bundle();
        rootBundle.putString("PROFILE_NAME", this.profileName);
        rootBundle.putString("PROFILE_ENABLED", "true");
        rootBundle.putString("CONFIG_MODE", "UPDATE");

        // Children
        ArrayList<Bundle> bundlePluginConfig = new ArrayList<>();
        rootBundle.putParcelableArrayList("PLUGIN_CONFIG", bundlePluginConfig);

        // Barcode input config
        Bundle barcodeInputBundle = new Bundle();
        bundlePluginConfig.add(barcodeInputBundle);
        barcodeInputBundle.putString("PLUGIN_NAME", "BARCODE");
        barcodeInputBundle.putString("RESET_CONFIG", "false");

        Bundle barcodeInputParams = new Bundle();
        barcodeInputBundle.putBundle("PARAM_LIST", barcodeInputParams);
        barcodeInputParams.putString("scanner_selection", "auto");
        barcodeInputParams.putString("scanner_input_enabled", "true");

        // Intent output config
        Bundle intentOutputBundle = new Bundle();
        bundlePluginConfig.add(intentOutputBundle);
        intentOutputBundle.putString("PLUGIN_NAME", "INTENT");
        intentOutputBundle.putString("RESET_CONFIG", "false");

        Bundle intentOutputParams = new Bundle();
        intentOutputBundle.putBundle("PARAM_LIST", intentOutputParams);
        intentOutputParams.putString("intent_output_enabled", "true");
        intentOutputParams.putString("intent_action", this.dwIntentPluginActionName);
        intentOutputParams.putString("intent_category", "");
        intentOutputParams.putInt("intent_delivery", 2); // 0 = start activity, 1 = start service, 2 = broadcast

        // Create intent with this configuration.
        ZebraDwCommand cmd = ZebraDwCommand.create().putExtra("com.symbol.datawedge.api.SET_CONFIG", rootBundle).setCallback(intent -> {
            getConfig(this.profileName);
            switchToOurProfile();
            getScannerStatus();
        });
        broadcastDwAction(cmd);
    }

    private void configureSymbologies() {
        if (currentConfig == null) {
            throw new IllegalStateException("configureSymbologies can only be called after a configuration query to ensure which barcode types to disable");
        }
        Map<String, String> result = new HashMap<>(symbologies.size());

        for (Map.Entry<String, String> entry : currentConfig.getPluginConfig("BARCODE").entrySet()) {
            if (!entry.getKey().startsWith("decoder_") || entry.getKey().lastIndexOf("_") != 7) {
                continue; // Not a decoder master switch
            }

            if (entry.getValue().equals("true")) {
                result.put(entry.getKey(), "false");
            }
        }

        for (BarcodeType bt : symbologies) {
            ZebraDwSymbology zds = ZebraDwSymbology.getSymbology(bt);
            if (zds == null) {
                Log.w(LOG_TAG, "This provider does not support symbology " + bt);
                continue;
            }

            if (result.containsKey(zds.intentExtraDecoderConfigName)) {
                // If present in result = we wanted to put it to false = it is already true!
                result.remove(zds.intentExtraDecoderConfigName);
                continue;
            }

            // If here we need to enable this symbology
            result.put(ZebraDwSymbology.getSymbology(bt).intentExtraDecoderConfigName, "true");
        }

        if (!result.isEmpty()) {
            configureRuntimeItems(result);
        }
    }

    private void switchToOurProfile() {
        if (this.currentlyActiveProfile != null && this.currentlyActiveProfile.equals(this.profileName)) {
            Log.d(LOG_TAG, "Not switching profile - already active");
            return;
        }
        //this.currentlyActiveProfile = this.profileName;

        broadcastDwAction(ZebraDwCommand.create("com.symbol.datawedge.api.SWITCH_TO_PROFILE", this.profileName).setCallback(i -> {
            Log.d(LOG_TAG, "Profile switch done with success");
        }));
    }

    private void broadcastDwAction(ZebraDwCommand command) {
        // Store the command - we are waiting for its result.
        commandsWaitingForResult.put(command.getId(), command);

        Log.d(LOG_TAG, "Sending DW action. " + command.toString());
        this.broadcastIntent(command.getIntentToSend());
    }

    private void getScannerStatus() {
        broadcastDwAction(ZebraDwCommand.create(ZebraDwIntents.DW_API_GET_STATUS_EXTRA, "").setExpectedResultKey("com.symbol.datawedge.api.RESULT_SCANNER_STATUS").setCallback(intent -> {
            String scannerStatus = intent.getStringExtra("com.symbol.datawedge.api.RESULT_SCANNER_STATUS");
            if (this.statusCb != null && scannerStatus != null) {
                this.statusCb.onStatusChanged(this, ZebraDwHelpers.getStatus(scannerStatus));
            }
            Log.d(LOG_TAG, "Scanner status:" + scannerStatus);
        }));
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
        String overloadedProfileName = applicationContext.getString(R.string.enioka_scan_zebra_dw_profile_name);
        profileName = overloadedProfileName.trim().isEmpty() ? applicationContext.getPackageName() : overloadedProfileName;
        dwIntentPluginActionName = applicationContext.getResources().getString(R.string.enioka_scan_zebra_dw_intent_name);

        if (dwIntentPluginActionName.isEmpty()) {
            dwIntentPluginActionName = ZebraDwIntents.DW_MAIN_CALLBACK_ACTION;
        }

        broadcastIntentFilters.add(dwIntentPluginActionName);
        broadcastIntentFilters.add(ZebraDwIntents.DW_CONFIGURATION_CALLBACK_ACTION);
        broadcastIntentFilters.add(ZebraDwIntents.DW_NOTIFICATION_ACTION);

        disableScanner = newIntent(ZebraDwIntents.DW_API_MAIN_ACTION, "com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN", "DISABLE_PLUGIN");
        enableScanner = newIntent(ZebraDwIntents.DW_API_MAIN_ACTION, "com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN", "ENABLE_PLUGIN");
    }

    @Override
    protected void configureAfterInit(Context ctx) {
        // Enable datawedge just in case.
        broadcastIntent(newIntent(ZebraDwIntents.DW_API_MAIN_ACTION, "com.symbol.datawedge.api.ENABLE_DATAWEDGE", true));
        super.configureAfterInit(ctx);

        // We want to know what is happening on the device, at least for logs
        notificationSubscription(ctx);

        // Create profile and configure it.
        // (this triggers a callback chain)
        queryActiveProfile();
    }

    @Override
    public void resume(@Nullable ScannerCommandCallbackProxy cb) {
        // Enable datawedge just in case.
        broadcastIntent(newIntent(ZebraDwIntents.DW_API_MAIN_ACTION, "com.symbol.datawedge.api.ENABLE_DATAWEDGE", true));
        super.resume(cb);
        switchToOurProfile();
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
        configureRuntimeItem("illumination_mode", "torch");
        illuminationOn = true;
    }

    @Override
    public void disableIllumination(@Nullable ScannerCommandCallbackProxy cb) {
        configureRuntimeItem("illumination_mode", "off");
        illuminationOn = false;
    }

    @Override
    public void toggleIllumination(@Nullable ScannerCommandCallbackProxy cb) {
        configureRuntimeItem("illumination_mode", illuminationOn ? "off" : "torch");
        illuminationOn = !illuminationOn;
    }

    @Override
    public boolean isIlluminationOn() {
        return illuminationOn;
    }
}
