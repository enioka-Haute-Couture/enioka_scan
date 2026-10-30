package com.enioka.scanner.integration;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.service.ScannerClient;
import com.enioka.scanner.service.ScannerService;
import com.enioka.scanner.service.ScannerServiceApi;
import com.enioka.scanner.service.ScannerServiceBinderHelper;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.lang.ClassNotFoundException;

public class EniokaScanPlugin extends CordovaPlugin implements ScannerClient {
    private static final String LOG_TAG = "eniokaScanIntegrationPlugin";

    private boolean serviceBound = false;
    private ScannerServiceApi scannerService = null;

    private CallbackContext boundCallbackContext = null;

    private List<String> connectedProviders = new ArrayList<>();

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    /**
     * Plugin methods
     */

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        Log.i(LOG_TAG, "Received action: " + action);
        try {
            if (action.equals("startActivityByName")) {
                var targetActivity = args.getJSONObject(0).getString("targetActivity");
                var intentExtras = args.getJSONObject(0).getJSONObject("intentExtras");
                return this.startActivityByName(targetActivity, intentExtras, callbackContext);
            }
            if (action.equals("startActivity")) {
                var targetActivity = "com.enioka.scanner.activities.ScannerCompatActivity";
                var intentExtras = args.getJSONObject(0).getJSONObject("intentExtras");
                return this.startActivityByName(targetActivity, intentExtras, callbackContext);
            }
            if (action.equals("bindScanService")) {
                var intentExtras = args.getJSONObject(0).getJSONObject("intentExtras");
                return this.bindScanService(intentExtras, callbackContext);
            }
            if (action.equals("unbindScanService")) {
                return this.unbindScanService(callbackContext);
            }
            if (action.equals("restartScannerDiscovery")) {
                return this.restartScannerDiscovery(callbackContext);
            }
            if (action.equals("restartProviderDiscovery")) {
                return this.restartProviderDiscovery(callbackContext);
            }
            if (action.equals("getAvailableProviders")) {
                return this.getAvailableProviders(callbackContext);
            }
            if (action.equals("updateScannerSearchOptions")) {
                var intentExtras = args.getJSONObject(0).getJSONObject("intentExtras");
                return this.updateScannerSearchOptions(intentExtras, callbackContext);
            }
            if (action.equals("pause")) {
                return this.pause(callbackContext);
            }
            if (action.equals("resume")) {
                return this.resume(callbackContext);
            }
            if (action.equals("disconnect")) {
                return this.disconnect(callbackContext);
            }

            Log.e(LOG_TAG, "Invalid Action: " + action);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            return false;
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
            return false;
        }
    }

    private boolean startActivityByName(String targetActivity, JSONObject intentExtras, final CallbackContext callbackContext) {
        Log.d(LOG_TAG, "EniokaScanPlugin::startActivityByName");
        var activity = this.cordova.getActivity();
        try {
            Class<?> klass = Class.forName(targetActivity);
            Intent intent = new Intent(activity, klass);
            applyExtras(intent, intentExtras);
            activity.startActivity(intent);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
            return true;
        } catch (ClassNotFoundException e) {
            Log.e(LOG_TAG, e.toString());
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.CLASS_NOT_FOUND_EXCEPTION));
            return false;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.toString());
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
            return false;
        }
    }

    private boolean bindScanService(JSONObject intentExtras, final CallbackContext callbackContext) {
        if (serviceBound) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Scanner Service is already bound"));
            return false;
        }

        // Bind to ScannerService service
        var activity = this.cordova.getActivity();
        Intent intent = new Intent(activity, ScannerService.class);
        try {
            applyExtras(intent, intentExtras);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.toString());
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
            return false;
        }
        boundCallbackContext = callbackContext;
        activity.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        return true;
    }

    private boolean unbindScanService(final CallbackContext callbackContext) {
        if (serviceBound) {
            var activity = this.cordova.getActivity();
            scannerService.unregisterClient(this);
            activity.unbindService(connection);
            boundCallbackContext = null;
            serviceBound = false;
            return true;
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Scanner Service is not bound"));
            return false;
        }
    }

    private boolean restartScannerDiscovery(final CallbackContext callbackContext) {
        if (serviceBound) {
            scannerService.restartScannerDiscovery();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
            return true;
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Scanner Service is not bound"));
            return false;
        }
    }

    private boolean restartProviderDiscovery(final CallbackContext callbackContext) {
        if (serviceBound) {
            scannerService.restartProviderDiscovery();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
            return true;
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Scanner Service is not bound"));
            return false;
        }
    }

    private boolean getAvailableProviders(final CallbackContext callbackContext) {
        if (serviceBound) {
            var providers = scannerService.getAvailableProviders();
            JSONArray resultBarcodes = new JSONArray(providers);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, resultBarcodes));
            return true;
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Scanner Service is not bound"));
            return false;
        }
    }

    private boolean updateScannerSearchOptions(JSONObject intentExtras, final CallbackContext callbackContext) {
        if (serviceBound) {
            var activity = this.cordova.getActivity();
            Intent intent = new Intent(activity, ScannerService.class);
            try {
                applyExtras(intent, intentExtras);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.toString());
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                return false;
            }
            scannerService.updateScannerSearchOptions(new ScannerSearchOptions().fromIntentExtras(intent));
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
            return true;
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Scanner Service is not bound"));
            return false;
        }
    }

    private boolean pause(final CallbackContext callbackContext) {
        if (serviceBound) {
            scannerService.pause();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
            return true;
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Scanner Service is not bound"));
            return false;
        }
    }

    private boolean resume(final CallbackContext callbackContext) {
        if (serviceBound) {
            scannerService.resume();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
            return true;
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Scanner Service is not bound"));
            return false;
        }
    }

    private boolean disconnect(final CallbackContext callbackContext) {
        if (serviceBound) {
            scannerService.disconnect();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
            return true;
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Scanner Service is not bound"));
            return false;
        }
    }

    /**
     * Service methods
     */

    @Override
    public void onStatusChanged(final Scanner scanner, final ScannerStatusCallback.Status newStatus) {
        if (scanner != null && (newStatus == Status.CONNECTED || newStatus == Status.DISCONNECTED)) {
            String provider = scanner.getProviderKey();

            if (newStatus == Status.CONNECTED) {
                if (!connectedProviders.contains(provider)) {
                    connectedProviders.add(provider);
                }
            } else if (connectedProviders.contains(provider)) {
                connectedProviders.remove(provider);
            }
        }
    }

    @Override
    public void onScannerInitEnded(int scannerCount) {
        Log.i(LOG_TAG, "Scanner service finished initializing all received scanners (" + scannerCount + ")");
    }

    @Override
    public void onProviderDiscoveryEnded() {
        Log.d(LOG_TAG, "Scanner service finished provider discovery");
    }

    @Override
    public void onData(List<Barcode> data) {
        if (boundCallbackContext == null) {
            return;
        }

        try {
            JSONArray resultBarcodes = new JSONArray();
            for (Barcode b : data) {
                Log.d(LOG_TAG, "Received barcode from scanner: " + b.getBarcode() + " - " + b.getBarcodeType().code);
                JSONObject mappedBarcode = new JSONObject().put("barcode", b.getBarcode()).put("symbology", b.getBarcodeType().code);
                resultBarcodes.put(mappedBarcode);
            }

            PluginResult result = new PluginResult(PluginResult.Status.OK, resultBarcodes);
            result.setKeepCallback(true);
            boundCallbackContext.sendPluginResult(result);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.toString());
        }
    }

    /**
     * Callbacks for service binding
     */
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to ScannerService, cast the IBinder and get the ScannerServiceApi instance
            Log.d(LOG_TAG, "Service is connected to plugin");
            ScannerService.LocalBinder binder = (ScannerService.LocalBinder) service;
            scannerService = binder.getService();
            serviceBound = true;
            scannerService.registerClient(EniokaScanPlugin.this);
            scannerService.resume();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d(LOG_TAG, "Service is disconnected from plugin");
            if (boundCallbackContext != null) {
                boundCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Scanner Service disconnected"));
            }
            boundCallbackContext = null;
            scannerService = null;
            serviceBound = false;
        }
    };

    /**
     * Helper methods
     */

    private void applyExtras(Intent intent, JSONObject intentExtras) throws JSONException {
        if (intentExtras.has("startSearchOnServiceBind")) {
            intent.putExtra("startSearchOnServiceBind", intentExtras.getBoolean("startSearchOnServiceBind"));
        } else {
            intent.putExtra("startSearchOnServiceBind", true);
        }

        if (intentExtras.has("useBlueTooth")) {
            intent.putExtra("useBlueTooth", intentExtras.getBoolean("useBlueTooth"));
        } else {
            intent.putExtra("useBlueTooth", true);
        }

        if (intentExtras.has("allowIntentDevices")) {
            intent.putExtra("allowIntentDevices", intentExtras.getBoolean("allowIntentDevices"));
        } else {
            intent.putExtra("allowIntentDevices", true);
        }

        if (intentExtras.has("allowLaterConnections")) {
            intent.putExtra("allowLaterConnections", intentExtras.getBoolean("allowLaterConnections"));
        } else {
            intent.putExtra("allowLaterConnections", true);
        }

        if (intentExtras.has("allowInitialSearch")) {
            intent.putExtra("allowInitialSearch", intentExtras.getBoolean("allowInitialSearch"));
        } else {
            intent.putExtra("allowInitialSearch", true);
        }

        if (intentExtras.has("allowPairingFlow")) {
            intent.putExtra("allowPairingFlow", intentExtras.getBoolean("allowPairingFlow"));
        } else {
            intent.putExtra("allowPairingFlow", true);
        }

        if (intentExtras.has("allowedProviderKeys")) {
            var allowedProviderKeysJs = intentExtras.getJSONArray("allowedProviderKeys");
            var allowedProviderKeysJava = new ArrayList<String>();
            for (int i = 0; i < allowedProviderKeysJs.length(); i++) {
                allowedProviderKeysJava.add(allowedProviderKeysJs.getString(i));
            }
            intent.putExtra("allowedProviderKeys", allowedProviderKeysJava.toArray());
        } else {
            intent.putExtra("allowedProviderKeys", new ArrayList<String>().toArray());
        }

        if (intentExtras.has("excludedProviderKeys")) {
            var excludedProviderKeysJs = intentExtras.getJSONArray("excludedProviderKeys");
            var excludedProviderKeysJava = new ArrayList<String>();
            for (int i = 0; i < excludedProviderKeysJs.length(); i++) {
                excludedProviderKeysJava.add(excludedProviderKeysJs.getString(i));
            }
            intent.putExtra("excludedProviderKeys", excludedProviderKeysJava.toArray());
        } else {
            intent.putExtra("excludedProviderKeys", new ArrayList<String>().toArray());
        }

        if (intentExtras.has("symbologySelection")) {
            var symbologySelectionJs = intentExtras.getJSONArray("symbologySelection");
            var symbologySelectionJava = new ArrayList<String>();
            for (int i = 0; i < symbologySelectionJs.length(); i++) {
                symbologySelectionJava.add(symbologySelectionJs.getString(i));
            }
            intent.putExtra("symbologySelection", symbologySelectionJava.toArray());
        } else {
            intent.putExtra("symbologySelection", new ArrayList<String>().toArray());
        }
    }
}
