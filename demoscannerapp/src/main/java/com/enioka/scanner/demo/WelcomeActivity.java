package com.enioka.scanner.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.enioka.scanner.LaserScanner;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.service.ScannerService;
import com.enioka.scanner.service.ScannerServiceApi;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {
    private List<String> availableProviders = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        LaserScanner.discoverProviders(this, () -> {});
        availableProviders = LaserScanner.getProviderCache();
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
        options.allowIntentDevices = preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INTENT_BOOLEAN, options.allowIntentDevices);
        options.allowedProviderKeys = preferences.getStringSet(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, options.allowedProviderKeys);
        options.excludedProviderKeys = preferences.getStringSet(ScannerServiceApi.EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY, options.excludedProviderKeys);

        options.toIntentExtras(intent);
        // add symbology
        final String[] symbologies = preferences.getStringSet(ScannerServiceApi.EXTRA_SYMBOLOGY_SELECTION, ScannerService.defaultSymbologyByName()).toArray(new String[0]);
        intent.putExtra(ScannerServiceApi.EXTRA_SYMBOLOGY_SELECTION, symbologies);

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
        intent.putStringArrayListExtra("providers", (ArrayList<String>) availableProviders);
        startActivity(intent);
    }
}
