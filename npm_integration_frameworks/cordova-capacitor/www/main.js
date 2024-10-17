var exec = require('cordova/exec');

window.startActivity = function (params, successCallback, errorCallback) {
    exec(successCallback, errorCallback, "ActivityStarterPlugin", "startActivity", [params]);
};