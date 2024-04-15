package com.enioka.scanner.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;

import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.sdk.athesi.RD50TE.AthesiRD50TEProvider;
import com.enioka.scanner.sdk.athesi.SPA43LTE.AthesiSPA43LTEProvider;
import com.enioka.scanner.sdk.bluebird.BluebirdProvider;
import com.enioka.scanner.sdk.generalscan.GsSppScannerProvider;
import com.enioka.scanner.sdk.honeywelloss.integrated.HoneywellOssIntegratedScannerProvider;
import com.enioka.scanner.sdk.honeywelloss.spp.HoneywellOssSppScannerProvider;
import com.enioka.scanner.sdk.proglove.ProgloveProvider;
import com.enioka.scanner.sdk.zebraoss.ZebraOssAttScannerProvider;
import com.enioka.scanner.sdk.zebraoss.ZebraOssSppScannerProvider;
import com.enioka.scanner.service.ScannerService;
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
        ((Switch) findViewById(R.id.switchIntentDevices)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INTENT_BOOLEAN, options.allowIntentDevices));

        final Set<String> allowedProviderKeys = preferences.getStringSet(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, Collections.emptySet());
        ((CheckBox) findViewById(R.id.checkAllowedHHTProvider)).setChecked(allowedProviderKeys.contains(AthesiSPA43LTEProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkAllowedE5LProvider)).setChecked(allowedProviderKeys.contains(AthesiRD50TEProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkAllowedBluebirdProvider)).setChecked(allowedProviderKeys.contains(BluebirdProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkAllowedProgloveProvider)).setChecked(allowedProviderKeys.contains(ProgloveProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkAllowedGsSppScannerProvider)).setChecked(allowedProviderKeys.contains(GsSppScannerProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkAllowedZebraOssSppScannerProvider)).setChecked(allowedProviderKeys.contains(ZebraOssSppScannerProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkAllowedZebraOssAttScannerProvider)).setChecked(allowedProviderKeys.contains(ZebraOssAttScannerProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkAllowedHoneywellOssSppScannerProvider)).setChecked(allowedProviderKeys.contains(HoneywellOssSppScannerProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkAllowedHoneywellOssIntegratedScannerProvider)).setChecked(allowedProviderKeys.contains(HoneywellOssIntegratedScannerProvider.PROVIDER_KEY));

        final Set<String> excludedProviderKeys = preferences.getStringSet(ScannerServiceApi.EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY, Collections.emptySet());
        ((CheckBox) findViewById(R.id.checkExcludedHHTProvider)).setChecked(excludedProviderKeys.contains(AthesiSPA43LTEProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkExcludedE5LProvider)).setChecked(excludedProviderKeys.contains(AthesiRD50TEProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkExcludedBluebirdProvider)).setChecked(excludedProviderKeys.contains(BluebirdProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkExcludedProgloveProvider)).setChecked(excludedProviderKeys.contains(ProgloveProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkExcludedGsSppScannerProvider)).setChecked(excludedProviderKeys.contains(GsSppScannerProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkExcludedZebraOssSppScannerProvider)).setChecked(excludedProviderKeys.contains(ZebraOssSppScannerProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkExcludedZebraOssAttScannerProvider)).setChecked(excludedProviderKeys.contains(ZebraOssAttScannerProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkExcludedHoneywellOssSppScannerProvider)).setChecked(excludedProviderKeys.contains(HoneywellOssSppScannerProvider.PROVIDER_KEY));
        ((CheckBox) findViewById(R.id.checkExcludedHoneywellOssIntegratedScannerProvider)).setChecked(excludedProviderKeys.contains(HoneywellOssIntegratedScannerProvider.PROVIDER_KEY));

        final Set<BarcodeType> symbologySelection = new HashSet<>();
        for(String symbology: preferences.getStringSet(ScannerServiceApi.EXTRA_SYMBOLOGY_SELECTION, ScannerService.defaultSymbologyByName())) {
           symbologySelection.add(BarcodeType.valueOf(symbology));
        }
        ((CheckBox) findViewById(R.id.checkSelectCode128)).setChecked(symbologySelection.contains(BarcodeType.CODE128));
        ((CheckBox) findViewById(R.id.checkSelectCode39)).setChecked(symbologySelection.contains(BarcodeType.CODE39));
        ((CheckBox) findViewById(R.id.checkSelectDis25)).setChecked(symbologySelection.contains(BarcodeType.DIS25));
        ((CheckBox) findViewById(R.id.checkSelectInt25)).setChecked(symbologySelection.contains(BarcodeType.INT25));
        ((CheckBox) findViewById(R.id.checkSelectEan13)).setChecked(symbologySelection.contains(BarcodeType.EAN13));
        ((CheckBox) findViewById(R.id.checkSelectQrCode)).setChecked(symbologySelection.contains(BarcodeType.QRCODE));
        ((CheckBox) findViewById(R.id.checkSelectAztec)).setChecked(symbologySelection.contains(BarcodeType.AZTEC));
    }

    public void onClickSave(View v) {
        final SharedPreferences.Editor editor = this.getSharedPreferences("ScannerSearchPreferences", MODE_PRIVATE).edit();

        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_WAIT_DISCONNECTED_BOOLEAN, ((Switch) findViewById(R.id.switchWaitDisconnected)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_RETURN_ONLY_FIRST_BOOLEAN, ((Switch) findViewById(R.id.switchReturnOnlyFirst)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_BT_BOOLEAN, ((Switch) findViewById(R.id.switchBluetooth)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_KEEP_SEARCHING_BOOLEAN, ((Switch) findViewById(R.id.switchLaterConnections)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INITIAL_SEARCH_BOOLEAN, ((Switch) findViewById(R.id.switchInitialSearch)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_PAIRING_FLOW_BOOLEAN, ((Switch) findViewById(R.id.switchPairingFlow)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INTENT_BOOLEAN, ((Switch) findViewById(R.id.switchIntentDevices)).isChecked());

        final Set<String> allowedProviderKeys = new HashSet<>();
        if (((CheckBox) findViewById(R.id.checkAllowedHHTProvider)).isChecked()) { allowedProviderKeys.add(AthesiSPA43LTEProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkAllowedE5LProvider)).isChecked()) { allowedProviderKeys.add(AthesiRD50TEProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkAllowedBluebirdProvider)).isChecked()) { allowedProviderKeys.add(BluebirdProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkAllowedProgloveProvider)).isChecked()) { allowedProviderKeys.add(ProgloveProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkAllowedGsSppScannerProvider)).isChecked()) { allowedProviderKeys.add(GsSppScannerProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkAllowedZebraOssSppScannerProvider)).isChecked()) { allowedProviderKeys.add(ZebraOssSppScannerProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkAllowedZebraOssAttScannerProvider)).isChecked()) { allowedProviderKeys.add(ZebraOssAttScannerProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkAllowedHoneywellOssSppScannerProvider)).isChecked()) { allowedProviderKeys.add(HoneywellOssSppScannerProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkAllowedHoneywellOssIntegratedScannerProvider)).isChecked()) { allowedProviderKeys.add(HoneywellOssIntegratedScannerProvider.PROVIDER_KEY); }
        editor.putStringSet(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, allowedProviderKeys);

        final Set<String> excludedProviderKeys = new HashSet<>();
        if (((CheckBox) findViewById(R.id.checkExcludedHHTProvider)).isChecked()) { excludedProviderKeys.add(AthesiSPA43LTEProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkExcludedE5LProvider)).isChecked()) { excludedProviderKeys.add(AthesiRD50TEProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkExcludedBluebirdProvider)).isChecked()) { excludedProviderKeys.add(BluebirdProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkExcludedProgloveProvider)).isChecked()) { excludedProviderKeys.add(ProgloveProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkExcludedGsSppScannerProvider)).isChecked()) { excludedProviderKeys.add(GsSppScannerProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkExcludedZebraOssSppScannerProvider)).isChecked()) { excludedProviderKeys.add(ZebraOssSppScannerProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkExcludedZebraOssAttScannerProvider)).isChecked()) { excludedProviderKeys.add(ZebraOssAttScannerProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkExcludedHoneywellOssSppScannerProvider)).isChecked()) { excludedProviderKeys.add(HoneywellOssSppScannerProvider.PROVIDER_KEY); }
        if (((CheckBox) findViewById(R.id.checkExcludedHoneywellOssIntegratedScannerProvider)).isChecked()) { excludedProviderKeys.add(HoneywellOssIntegratedScannerProvider.PROVIDER_KEY); }
        editor.putStringSet(ScannerServiceApi.EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY, excludedProviderKeys);

        final Set<String> symbologySelection = new HashSet<>();
        if (((CheckBox) findViewById(R.id.checkSelectCode128)).isChecked()) { symbologySelection.add(BarcodeType.CODE128.name()); }
        if (((CheckBox) findViewById(R.id.checkSelectCode39)).isChecked()) { symbologySelection.add(BarcodeType.CODE39.name()); }
        if (((CheckBox) findViewById(R.id.checkSelectDis25)).isChecked()) { symbologySelection.add(BarcodeType.DIS25.name()); }
        if (((CheckBox) findViewById(R.id.checkSelectInt25)).isChecked()) { symbologySelection.add(BarcodeType.INT25.name()); }
        if (((CheckBox) findViewById(R.id.checkSelectEan13)).isChecked()) { symbologySelection.add(BarcodeType.EAN13.name()); }
        if (((CheckBox) findViewById(R.id.checkSelectQrCode)).isChecked()) { symbologySelection.add(BarcodeType.QRCODE.name()); }
        if (((CheckBox) findViewById(R.id.checkSelectAztec)).isChecked()) { symbologySelection.add(BarcodeType.AZTEC.name()); }
        editor.putStringSet(ScannerServiceApi.EXTRA_SYMBOLOGY_SELECTION, symbologySelection);

        editor.apply();
        finish();
    }
}
