package com.enioka.scanner.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;

import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.sdk.athesi.RD50TE.AthesiE5LProvider;
import com.enioka.scanner.sdk.athesi.SPA43LTE.AthesiHHTProvider;
import com.enioka.scanner.sdk.bluebird.BluebirdProvider;
import com.enioka.scanner.sdk.generalscan.GsSppScannerProvider;
import com.enioka.scanner.sdk.honeywelloss.HoneywellOssSppScannerProvider;
import com.enioka.scanner.sdk.proglove.ProgloveProvider;
import com.enioka.scanner.sdk.zebraoss.ZebraOssAttScannerProvider;
import com.enioka.scanner.sdk.zebraoss.ZebraOssSppScannerProvider;
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
        ((Switch) findViewById(R.id.switchWaitDisconnected)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_WAIT_DISCONNECTED_BOOLEAN, options.waitDisconnected));
        ((Switch) findViewById(R.id.switchReturnOnlyFirst)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_RETURN_ONLY_FIRST_BOOLEAN, options.returnOnlyFirst));
        ((Switch) findViewById(R.id.switchBluetooth)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_BT_BOOLEAN, options.useBlueTooth));
        ((Switch) findViewById(R.id.switchLaterConnections)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_KEEP_SEARCHING_BOOLEAN, options.allowLaterConnections));
        ((Switch) findViewById(R.id.switchInitialSearch)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INITIAL_SEARCH_BOOLEAN, options.allowInitialSearch));
        ((Switch) findViewById(R.id.switchPairingFlow)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_PAIRING_FLOW_BOOLEAN, options.allowPairingFlow));

        final Set<String> allowedProviderKeys = preferences.getStringSet(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, Collections.emptySet());
        ((CheckBox) findViewById(R.id.checkAllowedHHTProvider)).setChecked(allowedProviderKeys.contains(AthesiHHTProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkAllowedE5LProvider)).setChecked(allowedProviderKeys.contains(AthesiE5LProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkAllowedBluebirdProvider)).setChecked(allowedProviderKeys.contains(BluebirdProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkAllowedProgloveProvider)).setChecked(allowedProviderKeys.contains(ProgloveProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkAllowedGsSppScannerProvider)).setChecked(allowedProviderKeys.contains(GsSppScannerProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkAllowedZebraOssSppScannerProvider)).setChecked(allowedProviderKeys.contains(ZebraOssSppScannerProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkAllowedZebraOssAttScannerProvider)).setChecked(allowedProviderKeys.contains(ZebraOssAttScannerProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkAllowedHoneywellOssSppScannerProvider)).setChecked(allowedProviderKeys.contains(HoneywellOssSppScannerProvider.PROVIDER_KEY));

        final Set<String> excludedProviderKeys = preferences.getStringSet(ScannerServiceApi.EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY, Collections.emptySet());
        ((CheckBox) findViewById(R.id.checkExcludedHHTProvider)).setChecked(excludedProviderKeys.contains(AthesiHHTProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkExcludedE5LProvider)).setChecked(excludedProviderKeys.contains(AthesiE5LProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkExcludedBluebirdProvider)).setChecked(excludedProviderKeys.contains(BluebirdProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkExcludedProgloveProvider)).setChecked(excludedProviderKeys.contains(ProgloveProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkExcludedGsSppScannerProvider)).setChecked(excludedProviderKeys.contains(GsSppScannerProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkExcludedZebraOssSppScannerProvider)).setChecked(excludedProviderKeys.contains(ZebraOssSppScannerProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkExcludedZebraOssAttScannerProvider)).setChecked(excludedProviderKeys.contains(ZebraOssAttScannerProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkExcludedHoneywellOssSppScannerProvider)).setChecked(excludedProviderKeys.contains(HoneywellOssSppScannerProvider.PROVIDER_KEY));
    }

    public void onClickSave(View v) {
        final SharedPreferences.Editor editor = this.getSharedPreferences("ScannerSearchPreferences", MODE_PRIVATE).edit();

        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_WAIT_DISCONNECTED_BOOLEAN, ((Switch) findViewById(R.id.switchWaitDisconnected)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_RETURN_ONLY_FIRST_BOOLEAN, ((Switch) findViewById(R.id.switchReturnOnlyFirst)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_BT_BOOLEAN, ((Switch) findViewById(R.id.switchBluetooth)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_KEEP_SEARCHING_BOOLEAN, ((Switch) findViewById(R.id.switchLaterConnections)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INITIAL_SEARCH_BOOLEAN, ((Switch) findViewById(R.id.switchInitialSearch)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_PAIRING_FLOW_BOOLEAN, ((Switch) findViewById(R.id.switchPairingFlow)).isChecked());

        final Set<String> allowedProviderKeys = new HashSet<>();
        if (((CheckBox) findViewById(R.id.checkAllowedHHTProvider)).isChecked()) { allowedProviderKeys.add(AthesiHHTProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkAllowedE5LProvider)).isChecked()) { allowedProviderKeys.add(AthesiE5LProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkAllowedBluebirdProvider)).isChecked()) { allowedProviderKeys.add(BluebirdProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkAllowedProgloveProvider)).isChecked()) { allowedProviderKeys.add(ProgloveProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkAllowedGsSppScannerProvider)).isChecked()) { allowedProviderKeys.add(GsSppScannerProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkAllowedZebraOssSppScannerProvider)).isChecked()) { allowedProviderKeys.add(ZebraOssSppScannerProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkAllowedZebraOssAttScannerProvider)).isChecked()) { allowedProviderKeys.add(ZebraOssAttScannerProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkAllowedHoneywellOssSppScannerProvider)).isChecked()) { allowedProviderKeys.add(HoneywellOssSppScannerProvider.PROVIDER_KEY); }
        editor.putStringSet(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, allowedProviderKeys);

        final Set<String> excludedProviderKeys = new HashSet<>();
        if (((CheckBox) findViewById(R.id.checkExcludedHHTProvider)).isChecked()) { excludedProviderKeys.add(AthesiHHTProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkExcludedE5LProvider)).isChecked()) { excludedProviderKeys.add(AthesiE5LProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkExcludedBluebirdProvider)).isChecked()) { excludedProviderKeys.add(BluebirdProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkExcludedProgloveProvider)).isChecked()) { excludedProviderKeys.add(ProgloveProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkExcludedGsSppScannerProvider)).isChecked()) { excludedProviderKeys.add(GsSppScannerProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkExcludedZebraOssSppScannerProvider)).isChecked()) { excludedProviderKeys.add(ZebraOssSppScannerProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkExcludedZebraOssAttScannerProvider)).isChecked()) { excludedProviderKeys.add(ZebraOssAttScannerProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkExcludedHoneywellOssSppScannerProvider)).isChecked()) { excludedProviderKeys.add(HoneywellOssSppScannerProvider.PROVIDER_KEY); }
        editor.putStringSet(ScannerServiceApi.EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY, excludedProviderKeys);

        editor.apply();
        finish();
    }
}
