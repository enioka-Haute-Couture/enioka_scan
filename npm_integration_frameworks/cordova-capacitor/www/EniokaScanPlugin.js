var exec = require('cordova/exec');

function EniokaScanPlugin() {}

/**
 * @param targetActivity string, classpath of the activity to start, e.g. "com.enioka.scanner.activities.ScannerCompatActivity"
 * @param intentExtras Record<string, boolean | string[]>, extras passed to the activity intent
 */
EniokaScanPlugin.prototype.startActivityByName = function (targetActivity, intentExtras, successCallback, errorCallback) {
    var params = [{ targetActivity, intentExtras }]
    exec(successCallback, errorCallback, "ActivityStarterPlugin", "startActivityByName", params);
}

/**
 * @param intentExtras Record<string, boolean | string[]>, extras passed to the activity intent
 */
EniokaScanPlugin.prototype.startScannerCompatActivity = function (intentExtras, successCallback, errorCallback) {
    var params = [{ intentExtras }]
    exec(successCallback, errorCallback, "EniokaScanPlugin", "startActivity", params);
}

EniokaScanPlugin.prototype.getDefaultExtras = function () {
    return {
        "startSearchOnServiceBind": true,
        "useBlueTooth": true,
        "allowIntentDevices": true,
        "allowLaterConnections": true,
        "allowInitialSearch": true,
        "allowPairingFlow": true,
        "allowedProviderKeys": [],
        "excludedProviderKeys": [],
        "symbologySelection": [],
    };
}

module.exports = new EniokaScanPlugin();
