package com.enioka.scanner.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;

import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.service.ScannerServiceApi;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final SharedPreferences preferences = this.getSharedPreferences("ScannerSearchPreferences", MODE_PRIVATE);

        final ScannerSearchOptions options = ScannerSearchOptions.defaultOptions();
        ((Switch) findViewById(R.id.switchBluetooth)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_BT_ALLOW_BT_BOOLEAN, options.useBlueTooth));
        ((Switch) findViewById(R.id.switchLaterConnections)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_KEEP_SEARCHING_BOOLEAN, options.allowLaterConnections));
        ((Switch) findViewById(R.id.switchInitialSearch)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INITIAL_SEARCH_BOOLEAN, options.allowInitialSearch));
        ((Switch) findViewById(R.id.switchPairingFlow)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_PAIRING_FLOW_BOOLEAN, options.allowPairingFlow));

        final Set<String> allowedProviderKeys = preferences.getStringSet(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, Collections.emptySet());
        ((CheckBox) findViewById(R.id.checkAllowedGenericHidProvider)).setChecked(allowedProviderKeys.contains("GenericHidProvider"));
        ((CheckBox) findViewById(R.id.checkAllowedHHTProvider)).setChecked(allowedProviderKeys.contains("Athesi HHT internal scanner"));
        ((CheckBox) findViewById(R.id.checkAllowedBluebirdProvider)).setChecked(allowedProviderKeys.contains("BluebirdProvider"));
        ((CheckBox) findViewById(R.id.checkAllowedProgloveProvider)).setChecked(allowedProviderKeys.contains("ProgloveProvider"));
        ((CheckBox) findViewById(R.id.checkAllowedSerialBtScannerProvider)).setChecked(allowedProviderKeys.contains("BtSppSdk"));

        final Set<String> excludedProviderKeys = preferences.getStringSet(ScannerServiceApi.EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY, Collections.emptySet());
        ((CheckBox) findViewById(R.id.checkExcludedGenericHidProvider)).setChecked(excludedProviderKeys.contains("GenericHidProvider"));
        ((CheckBox) findViewById(R.id.checkExcludedHHTProvider)).setChecked(excludedProviderKeys.contains("Athesi HHT internal scanner"));
        ((CheckBox) findViewById(R.id.checkExcludedBluebirdProvider)).setChecked(excludedProviderKeys.contains("BluebirdProvider"));
        ((CheckBox) findViewById(R.id.checkExcludedProgloveProvider)).setChecked(excludedProviderKeys.contains("ProgloveProvider"));
        ((CheckBox) findViewById(R.id.checkExcludedSerialBtScannerProvider)).setChecked(excludedProviderKeys.contains("BtSppSdk"));
    }

    public void onClickSave(View v) {
        final SharedPreferences.Editor editor = this.getSharedPreferences("ScannerSearchPreferences", MODE_PRIVATE).edit();

        editor.putBoolean(ScannerServiceApi.EXTRA_BT_ALLOW_BT_BOOLEAN, ((Switch) findViewById(R.id.switchBluetooth)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_KEEP_SEARCHING_BOOLEAN, ((Switch) findViewById(R.id.switchLaterConnections)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INITIAL_SEARCH_BOOLEAN, ((Switch) findViewById(R.id.switchInitialSearch)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_PAIRING_FLOW_BOOLEAN, ((Switch) findViewById(R.id.switchPairingFlow)).isChecked());

        final Set<String> allowedProviderKeys = new HashSet<>();
        if (((CheckBox) findViewById(R.id.checkAllowedGenericHidProvider)).isChecked()) { allowedProviderKeys.add("GenericHidProvider"); }
        if (((CheckBox) findViewById(R.id.checkAllowedHHTProvider)).isChecked()) { allowedProviderKeys.add("Athesi HHT internal scanner"); }
        if (((CheckBox) findViewById(R.id.checkAllowedBluebirdProvider)).isChecked()) { allowedProviderKeys.add("BluebirdProvider"); }
        if (((CheckBox) findViewById(R.id.checkAllowedProgloveProvider)).isChecked()) { allowedProviderKeys.add("ProgloveProvider"); }
        if (((CheckBox) findViewById(R.id.checkAllowedSerialBtScannerProvider)).isChecked()) { allowedProviderKeys.add("BtSppSdk"); }
        editor.putStringSet(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, allowedProviderKeys);

        final Set<String> excludedProviderKeys = new HashSet<>();
        if (((CheckBox) findViewById(R.id.checkExcludedGenericHidProvider)).isChecked()) { excludedProviderKeys.add("GenericHidProvider"); }
        if (((CheckBox) findViewById(R.id.checkExcludedHHTProvider)).isChecked()) { excludedProviderKeys.add("Athesi HHT internal scanner"); }
        if (((CheckBox) findViewById(R.id.checkExcludedBluebirdProvider)).isChecked()) { excludedProviderKeys.add("BluebirdProvider"); }
        if (((CheckBox) findViewById(R.id.checkExcludedProgloveProvider)).isChecked()) { excludedProviderKeys.add("ProgloveProvider"); }
        if (((CheckBox) findViewById(R.id.checkExcludedSerialBtScannerProvider)).isChecked()) { excludedProviderKeys.add("BtSppSdk"); }
        editor.putStringSet(ScannerServiceApi.EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY, excludedProviderKeys);

        editor.apply();
        finish();
    }
}
