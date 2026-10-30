var exec = require('cordova/exec');

function EniokaScanPlugin() {}

/**
 * Starts an activity following a given classpath.
 * @param targetActivity string, classpath of the activity to start, e.g. "com.enioka.scanner.activities.ScannerCompatActivity"
 * @param intentExtras Record<string, boolean | string[]>, extras passed to the activity intent
 */
EniokaScanPlugin.prototype.startActivityByName = function (targetActivity, intentExtras, successCallback, errorCallback) {
    var params = [{ targetActivity, intentExtras }];
    exec(successCallback, errorCallback, "EniokaScanPlugin", "startActivityByName", params);
}

/**
 * Starts eniokaScan's default ScannerCompatActivity.
 * @param intentExtras Record<string, boolean | string[]>, extras passed to the activity intent
 */
EniokaScanPlugin.prototype.startScannerCompatActivity = function (intentExtras, successCallback, errorCallback) {
    var params = [{ intentExtras }];
    exec(successCallback, errorCallback, "EniokaScanPlugin", "startActivity", params);
}

/**
 * Starts and binds eniokaScan's ScannerService.
 * @param intentExtras Record<string, boolean | string[]>, extras passed to the activity intent
 * @param scanSuccessfulCallback The callback that will be called whenever a barcode gets scanned. Will receive an Array<{barcode: string, symbology: string}>
 * @param serviceDisconnectedCallback The callback that will be called if the service unbinds.
 */
EniokaScanPlugin.prototype.bindToScannerService = function (intentExtras, scanSuccessfulCallback, serviceDisconnectedCallback) {
    var params = [{ intentExtras }];
    exec(scanSuccessfulCallback, serviceDisconnectedCallback, "EniokaScanPlugin", "bindScanService", params);
}

/**
 * Unbinds eniokaScan's ScannerService.
 */
EniokaScanPlugin.prototype.unbindFromScannerService = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "EniokaScanPlugin", "unbindScanService", []);
}

/**
 * Disconnects all currently-connected scanners then starts the initialization process all over again.
 */
EniokaScanPlugin.prototype.restartScannerDiscovery = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "EniokaScanPlugin", "restartScannerDiscovery", []);
}

/**
 * Clears the cache of discovered scanner providers and re-starts their discovery. This is a costly operation.
 */
EniokaScanPlugin.prototype.restartProviderDiscovery = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "EniokaScanPlugin", "restartProviderDiscovery", []);
}

/**
 * Returns the list of keys of currently-available scanner providers, useful to tune scanner search options at runtime.
 * @param successCallback A callback that will receive an Array<string> containing the list of provider keys.
 */
EniokaScanPlugin.prototype.getAvailableProviders = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "EniokaScanPlugin", "getAvailableProviders", []);
}

/**
 * Updates the service's scanner search options.
 * @param intentExtras Record<string, boolean | string[]>, extras passed to the activity intent
 */
EniokaScanPlugin.prototype.updateScannerSearchOptions = function (intentExtras, successCallback, errorCallback) {
    var params = [{ intentExtras }];
    exec(successCallback, errorCallback, "EniokaScanPlugin", "updateScannerSearchOptions", []);
}

/**
 * The service keeps the scanners for itself but does not need it immediately. It may free whatever resources it has, or ignore this call. Idempotent.
 */
EniokaScanPlugin.prototype.pause = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "EniokaScanPlugin", "pause", []);
}

/**
 * Reverse the effects of pause(). The scanners are once again ready to scan after this call. Idempotent.
 */
EniokaScanPlugin.prototype.resume = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "EniokaScanPlugin", "resume", []);
}

/**
 * Disconnect all scanners from the service (the app does not need the scanner anymore).
 * In case scanners are needed again, restartScannerDiscovery() will need to be called first.
 */
EniokaScanPlugin.prototype.disconnect = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "EniokaScanPlugin", "disconnect", []);
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
