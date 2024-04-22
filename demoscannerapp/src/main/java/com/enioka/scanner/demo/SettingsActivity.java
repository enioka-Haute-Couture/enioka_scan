package com.enioka.scanner.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;

import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.service.ScannerService;
import com.enioka.scanner.service.ScannerServiceApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
    /**
     * List of possible providers
     */
    protected String[] providersList = null;
    /**
     * List of possible providers keys
     */
    protected String[] providersKeysList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final SharedPreferences preferences = this.getSharedPreferences("ScannerSearchPreferences", MODE_PRIVATE);

        // Detect which SDK providers are included inside the demo app
        providersList = getResources().getStringArray(R.array.providers);
        providersKeysList = getResources().getStringArray(R.array.providers_keys);
        List<String> includedProvidersSDK = detectIncludedProvidersSDK(providersList);

        final ScannerSearchOptions options = ScannerSearchOptions.defaultOptions();
        ((Switch) findViewById(R.id.switchWaitDisconnected)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_WAIT_DISCONNECTED_BOOLEAN, options.waitDisconnected));
        ((Switch) findViewById(R.id.switchReturnOnlyFirst)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_RETURN_ONLY_FIRST_BOOLEAN, options.returnOnlyFirst));
        ((Switch) findViewById(R.id.switchBluetooth)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_BT_BOOLEAN, options.useBlueTooth));
        ((Switch) findViewById(R.id.switchLaterConnections)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_KEEP_SEARCHING_BOOLEAN, options.allowLaterConnections));
        ((Switch) findViewById(R.id.switchInitialSearch)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INITIAL_SEARCH_BOOLEAN, options.allowInitialSearch));
        ((Switch) findViewById(R.id.switchPairingFlow)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_PAIRING_FLOW_BOOLEAN, options.allowPairingFlow));
        ((Switch) findViewById(R.id.switchIntentDevices)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INTENT_BOOLEAN, options.allowIntentDevices));

        final Set<String> allowedProviderKeys = preferences.getStringSet(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, Collections.emptySet());
        final Set<String> excludedProviderKeys = preferences.getStringSet(ScannerServiceApi.EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY, Collections.emptySet());

        int idx = -1;
        for (String provider : providersList) {
            String[] splitProviders = provider.split("\\.");
            String suffixProvider = splitProviders[splitProviders.length - 1];
            idx += 1;

            // Get the id of the checkboxes
            int idAllowed = getId("checkAllowed" + suffixProvider);
            int idExcluded = getId("checkExcluded" + suffixProvider);

            if (!includedProvidersSDK.contains(suffixProvider)) {
                findViewById(idAllowed).setVisibility(View.GONE);
                findViewById(idExcluded).setVisibility(View.GONE);
                findViewById(getId("textViewAllowed" + suffixProvider)).setVisibility(View.GONE);
                findViewById(getId("textViewExcluded" + suffixProvider)).setVisibility(View.GONE);
            } else {
                // Get the provider key of the current provider
                String providerKey = providersKeysList[idx];

                ((CheckBox) findViewById(idAllowed)).setChecked(allowedProviderKeys.contains(providerKey));
                ((CheckBox) findViewById(idExcluded)).setChecked(excludedProviderKeys.contains(providerKey));
            }
        }

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
        final Set<String> excludedProviderKeys = new HashSet<>();

        int idx = -1;
        for (String provider : providersList) {
            idx += 1;
            String[] splitProviders = provider.split("\\.");
            String suffixProvider = splitProviders[splitProviders.length - 1];

            // Get the id of the checkboxes
            int idAllowed = getId("checkAllowed" + suffixProvider);
            int idExcluded = getId("checkExcluded" + suffixProvider);

            if (!findViewById(idAllowed).isShown()) {
                continue;
            }

            if (((CheckBox) findViewById(idAllowed)).isChecked()) {
                allowedProviderKeys.add(providersKeysList[idx]);
            }
            if (((CheckBox) findViewById(idExcluded)).isChecked()) {
                excludedProviderKeys.add(providersKeysList[idx]);
            }
        }

        editor.putStringSet(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, allowedProviderKeys);
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

    /**
     * Detects which providers are included in the SDK and returns them.
     */
    private List<String> detectIncludedProvidersSDK(String[] providersList) {
        List<String> includedProviders = new ArrayList<>();

        for (String p : providersList) {
            try {
                Class.forName("com.enioka.scanner.sdk." + p);
                String[] splitProviders = p.split("\\.");
                includedProviders.add(splitProviders[splitProviders.length - 1]);
            } catch (ClassNotFoundException e) {
                continue;
            }
        }
        return includedProviders;
    }

    /**
     * Returns the id of a resource by its name.
     */
    private int getId(String name) {
        return getResources().getIdentifier(name, "id", getPackageName());
    }
}
