package com.enioka.scanner.integration

import android.content.Intent
import android.util.Log

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableArray

class EniokaScanModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "EniokaScanModule"

    @ReactMethod
    fun startActivityByName(targetActivity: String, intentExtras: ReadableMap) {
        Log.d("eniokaScanIntegrationPlugin", "EniokaScanModule::startActivityByName")
        val activity = getReactApplicationContext().getCurrentActivity()
        try {
            val klass = Class.forName(targetActivity)
            val intent = Intent(activity, klass)

            val allowedProviderKeysJs =
                if (intentExtras.hasKey("allowedProviderKeys")) intentExtras.getArray("allowedProviderKeys") else null
            val allowedProviderKeysKt = mutableListOf<String>()
            if (allowedProviderKeysJs != null && allowedProviderKeysJs.size() > 0) {
                for (i in 0..<allowedProviderKeysJs.size()) {
                    if (allowedProviderKeysJs.getString(i) != null) allowedProviderKeysKt.add(allowedProviderKeysJs.getString(i)!!)
                }
            }

            val excludedProviderKeysJs =
                if (intentExtras.hasKey("excludedProviderKeys")) intentExtras.getArray("excludedProviderKeys") else null
            val excludedProviderKeysKt = mutableListOf<String>()
            if (excludedProviderKeysJs != null && excludedProviderKeysJs.size() > 0) {
                for (i in 0..<excludedProviderKeysJs.size()) {
                    if (excludedProviderKeysJs.getString(i) != null) excludedProviderKeysKt.add(excludedProviderKeysJs.getString(i)!!)
                }
            }

            val symbologySelectionJs =
                if (intentExtras.hasKey("symbologySelection")) intentExtras.getArray("symbologySelection") else null
            val symbologySelectionKt = mutableListOf<String>()
            if (symbologySelectionJs != null && symbologySelectionJs.size() > 0) {
                for (i in 0..<symbologySelectionJs.size()) {
                    if (symbologySelectionJs.getString(i) != null) symbologySelectionKt.add(symbologySelectionJs.getString(i)!!)
                }
            }

            intent.putExtra(
                "startSearchOnServiceBind",
                if (intentExtras.hasKey("startSearchOnServiceBind")) intentExtras.getBoolean("startSearchOnServiceBind") else true
            )
            intent.putExtra(
                "useBlueTooth",
                if (intentExtras.hasKey("useBlueTooth")) intentExtras.getBoolean("useBlueTooth") else true
            )
            intent.putExtra(
                "allowIntentDevices",
                if (intentExtras.hasKey("allowIntentDevices")) intentExtras.getBoolean("allowIntentDevices") else true
            )
            intent.putExtra(
                "allowLaterConnections",
                if (intentExtras.hasKey("allowLaterConnections")) intentExtras.getBoolean("allowLaterConnections") else true
            )
            intent.putExtra(
                "allowInitialSearch",
                if (intentExtras.hasKey("allowInitialSearch")) intentExtras.getBoolean("allowInitialSearch") else true
            )
            intent.putExtra(
                "allowPairingFlow",
                if (intentExtras.hasKey("allowPairingFlow")) intentExtras.getBoolean("allowPairingFlow") else true
            )
            intent.putExtra("allowedProviderKeys", allowedProviderKeysKt.toTypedArray())
            intent.putExtra("excludedProviderKeys", excludedProviderKeysKt.toTypedArray())
            intent.putExtra("symbologySelection", symbologySelectionKt.toTypedArray())

            activity!!.startActivity(intent);
        } catch (e: Exception) {
            Log.e("eniokaScanIntegrationPlugin", e.toString());
        }
    }
}
