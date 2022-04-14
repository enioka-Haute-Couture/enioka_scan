package com.enioka.scanner.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.service.ScannerServiceApi;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    // Scanner activity
    public void onClickBt1(View v) {
        Intent intent = new Intent(this, MainActivity.class);

        final SharedPreferences preferences = this.getSharedPreferences("ScannerSearchPreferences", MODE_PRIVATE);
        final ScannerSearchOptions options = ScannerSearchOptions.defaultOptions();
        options.waitDisconnected = preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_WAIT_DISCONNECTED_BOOLEAN, options.waitDisconnected);
        options.returnOnlyFirst = preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_RETURN_ONLY_FIRST_BOOLEAN, options.returnOnlyFirst);
        options.useBlueTooth = preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_BT_BOOLEAN, options.useBlueTooth);
        options.allowLaterConnections = preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_KEEP_SEARCHING_BOOLEAN, options.allowLaterConnections);
        options.allowInitialSearch = preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INITIAL_SEARCH_BOOLEAN, options.allowInitialSearch);
        options.allowPairingFlow = preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_PAIRING_FLOW_BOOLEAN, options.allowPairingFlow);
        options.allowedProviderKeys = preferences.getStringSet(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, options.allowedProviderKeys);
        options.excludedProviderKeys = preferences.getStringSet(ScannerServiceApi.EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY, options.excludedProviderKeys);
        options.toIntentExtras(intent);

        startActivity(intent);
    }

    // Scan2 activity, WIP
    public void onClickBt3(View v) {
        Intent intent = new Intent(this, Scan2Activity.class);
        startActivity(intent);
    }

    // Scanner Test activity
    public void onClickBt4(View v) {
        Intent intent = new Intent(this, ScannerTesterActivity.class);
        startActivity(intent);
    }

    // Scanner settings activity
    public void onClickBt5(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
