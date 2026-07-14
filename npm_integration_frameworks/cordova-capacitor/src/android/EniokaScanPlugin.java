package com.enioka.scanner.integration;

import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import java.lang.ClassNotFoundException;

public class EniokaScanPlugin extends CordovaPlugin {

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        if (action.equals("startActivityByName")) {
            var targetActivity = args.getJSONObject(0).getString("targetActivity");
            var intentExtras = args.getJSONObject(0).getJSONObject("intentExtras");
            return this.startActivityByName(targetActivity, intentExtras, callbackContext);
        }

        Log.e("eniokaScanIntegrationPlugin", "Invalid Action: " + action);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
        return false;
    }

    private boolean startActivityByName(String targetActivity, JSONObject intentExtras, final CallbackContext callbackContext) {
        Log.d("eniokaScanIntegrationPlugin", "EniokaScanPlugin::startActivityByName")
        var activity = this.cordova.getActivity();
        try {
            Class<?> klass = Class.forName(targetActivity);
            Intent intent = new Intent(activity, klass);

            var availableIntentExtras = intentExtras.keySet();

            if (availableIntentExtras.contains("startSearchOnServiceBind")) {
                intent.putExtra("startSearchOnServiceBind", intentExtras.getBoolean("startSearchOnServiceBind"));
            } else {
                intent.putExtra("startSearchOnServiceBind", true);
            }

            if (availableIntentExtras.contains("useBlueTooth")) {
                intent.putExtra("useBlueTooth", intentExtras.getBoolean("useBlueTooth"));
            } else {
                intent.putExtra("useBlueTooth", true);
            }

            if (availableIntentExtras.contains("allowIntentDevices")) {
                intent.putExtra("allowIntentDevices", intentExtras.getBoolean("allowIntentDevices"));
            } else {
                intent.putExtra("allowIntentDevices", true);
            }

            if (availableIntentExtras.contains("allowLaterConnections")) {
                intent.putExtra("allowLaterConnections", intentExtras.getBoolean("allowLaterConnections"));
            } else {
                intent.putExtra("allowLaterConnections", true);
            }

            if (availableIntentExtras.contains("allowInitialSearch")) {
                intent.putExtra("allowInitialSearch", intentExtras.getBoolean("allowInitialSearch"));
            } else {
                intent.putExtra("allowInitialSearch", true);
            }

            if (availableIntentExtras.contains("allowPairingFlow")) {
                intent.putExtra("allowPairingFlow", intentExtras.getBoolean("allowPairingFlow"));
            } else {
                intent.putExtra("allowPairingFlow", true);
            }

            if (availableIntentExtras.contains("allowedProviderKeys")) {
                var allowedProviderKeysJs = intentExtras.getJSONArray("allowedProviderKeys");
                var allowedProviderKeysJava = new ArrayList<String>();
                for (int i = 0; i < allowedProviderKeysJs.length; i++) {
                    allowedProviderKeysJava.add(allowedProviderKeysJs.getString(i));
                }
                intent.putExtra("allowedProviderKeys", allowedProviderKeysJava.toArray());
            } else {
                intent.putExtra("allowedProviderKeys", new ArrayList<string>().toArray());
            }

            if (availableIntentExtras.contains("excludedProviderKeys")) {
                var excludedProviderKeysJs = intentExtras.getJSONArray("excludedProviderKeys");
                var excludedProviderKeysJava = new ArrayList<String>();
                for (int i = 0; i < excludedProviderKeysJs.length; i++) {
                    excludedProviderKeysJava.add(excludedProviderKeysJs.getString(i));
                }
                intent.putExtra("excludedProviderKeys", excludedProviderKeysJava.toArray());
            } else {
                intent.putExtra("excludedProviderKeys", new ArrayList<string>().toArray());
            }

            if (availableIntentExtras.contains("symbologySelection")) {
                var symbologySelectionJs = intentExtras.getJSONArray("symbologySelection");
                var symbologySelectionJava = new ArrayList<String>();
                for (int i = 0; i < symbologySelectionJs.length; i++) {
                    symbologySelectionJava.add(symbologySelectionJs.getString(i));
                }
                intent.putExtra("symbologySelection", symbologySelectionJava.toArray());
            } else {
                intent.putExtra("symbologySelection", new ArrayList<string>().toArray());
            }

            activity.startActivity(intent);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
            return true;
        } catch (ClassNotFoundException e) {
            Log.e("eniokaScanIntegrationPlugin", e.toString());
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.CLASS_NOT_FOUND_EXCEPTION));
            return false;
        } catch (JSONException e) {
            Log.e("eniokaScanIntegrationPlugin", e.toString());
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
            return false;
        }
    }
}
