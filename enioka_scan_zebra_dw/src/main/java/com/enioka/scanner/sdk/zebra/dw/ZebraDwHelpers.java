package com.enioka.scanner.sdk.zebra.dw;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.enioka.scanner.api.callbacks.ScannerStatusCallback;

import java.util.ArrayList;
import java.util.Set;

public class ZebraDwHelpers {
    private static final String LOG_TAG = "ZebraDwScanner";

    static ZebraDwConfig getConfig(Intent intent) {
        Bundle bundle = intent.getBundleExtra("com.symbol.datawedge.api.RESULT_GET_CONFIG");
        if (bundle == null || bundle.isEmpty()) {
            return null;
        }

        String profileName = bundle.getString("PROFILE_NAME");
        ZebraDwConfig res = new ZebraDwConfig(profileName);
        Log.d(LOG_TAG, "Received configuration for profile " + profileName);

        String profileEnabled = bundle.getString("PROFILE_ENABLED");
        Log.d(LOG_TAG, "This profile is " + (profileEnabled != null && profileEnabled.equals("true") ? "enabled" : "disabled"));

        ArrayList<Bundle> configBundleList = bundle.getParcelableArrayList("PLUGIN_CONFIG");
        if (configBundleList == null || configBundleList.isEmpty()) {
            Log.d(LOG_TAG, bundle.toString());
            Log.d(LOG_TAG, "This profile configuration query result has no plugin data");
            return null;
        }
        Log.d(LOG_TAG, "Configuration query was on " + configBundleList.size() + " DW plugins");

        for (Bundle configBundle : configBundleList) {
            String pluginName = configBundle.getString("PLUGIN_NAME");
            Log.d(LOG_TAG, "\n Plugin Name :" + pluginName);
            Set<String> bundleConfigKeys = configBundle.keySet();

            for (String bundleConfigKey : bundleConfigKeys) {
                if (!bundleConfigKey.equalsIgnoreCase("PARAM_LIST")) {
                    continue;
                }
                Bundle params = configBundle.getBundle("PARAM_LIST");
                if (params == null) {
                    continue; // For linter, otherwise stupid.
                }
                Set<String> paramKeys = params.keySet();
                for (String key : paramKeys) {
                    // TODO: keep type information.
                    Object value = params.get(key);
                    res.addConfigItem(pluginName, key, value != null ? value.toString() : null);
                }
            }
        }

        return res;
    }

    static ScannerStatusCallback.Status getStatus(String dwData) {
        switch (dwData) {
            case "WAITING":
                return ScannerStatusCallback.Status.READY;
            case "SCANNING":
                return ScannerStatusCallback.Status.SCANNING;
            case "CONNECTED":
                return ScannerStatusCallback.Status.CONNECTED;
            case "DISCONNECTED":
                return ScannerStatusCallback.Status.DISCONNECTED;
            case "DISABLED":
                return ScannerStatusCallback.Status.DISABLED;
            case "IDLE":
                return ScannerStatusCallback.Status.INITIALIZING;
            default:
                return ScannerStatusCallback.Status.UNKNOWN;
        }
    }
}
