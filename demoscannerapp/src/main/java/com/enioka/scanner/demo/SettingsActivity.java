package com.enioka.scanner.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;

import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.service.ScannerService;
import com.enioka.scanner.service.ScannerServiceApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
    /**
     * List of available providers key
     */
    protected String[] availableProvidersKey = null;

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
        final Set<String> excludedProviderKeys = preferences.getStringSet(ScannerServiceApi.EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY, Collections.emptySet());

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

        // Get detected SDKs providers from the intent
        ArrayList<String> availableProvidersIntent = getIntent().getStringArrayListExtra("providers");

        if (availableProvidersIntent == null) {
            availableProvidersKey = new String[0];
        } else {
            availableProvidersKey = availableProvidersIntent.toArray(new String[0]);
        }

        // Create the UI
        int topViewIdAllowed = R.id.textViewAllowedProviders;
        int topViewIdExcluded = R.id.textViewExcludedProviders;

        for (String providerKey : availableProvidersKey) {

            // Create the UI for the allowed providers
            TextView textViewAllowed = generateTextView(providerKey, topViewIdAllowed);
            textViewAllowed.setTag("allowed_text_" + providerKey);
            CheckBox checkBoxAllowed = generateCheckBox(providerKey, topViewIdAllowed, textViewAllowed.getId());
            checkBoxAllowed.setTag("allowed_check_" + providerKey);

            if (allowedProviderKeys.contains(providerKey)) {
                checkBoxAllowed.setChecked(true);
            }

            // Create the UI for the excluded providers
            TextView textViewExcluded = generateTextView(providerKey, topViewIdExcluded);
            textViewExcluded.setTag("excluded_text_" + providerKey);
            CheckBox checkBoxExcluded = generateCheckBox(providerKey, topViewIdExcluded, textViewExcluded.getId());
            checkBoxExcluded.setTag("excluded_check_" + providerKey);

            if (excludedProviderKeys.contains(providerKey)) {
                checkBoxExcluded.setChecked(true);
            }

            topViewIdAllowed = textViewAllowed.getId();
            topViewIdExcluded = textViewExcluded.getId();
        }

        // Update the layout
        updateConstraintLayout(topViewIdAllowed, topViewIdExcluded);
    }

    private void updateConstraintLayout(int topViewIdAllowed, int topViewIdExcluded) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) findViewById(R.id.constraintLayoutSettings));

        // Init marginTop
        int marginTop = getResources().getDimensionPixelSize(R.dimen.layout_margin_top_header_text);

        // Set top bottom constraints
        constraintSet.connect(R.id.textViewExcludedProviders, ConstraintSet.TOP, topViewIdAllowed, ConstraintSet.BOTTOM, marginTop);
        constraintSet.connect(R.id.textSymbologySelection, ConstraintSet.TOP, topViewIdExcluded, ConstraintSet.BOTTOM, marginTop);

        // Apply constraints
        constraintSet.applyTo(findViewById(R.id.constraintLayoutSettings));
    }

    private TextView generateTextView(String providerKey, int topViewId) {
        TextView textView = new TextView(this);
        textView.setId(View.generateViewId());

        // Add TextView to the parent layout
        ViewGroup parentLayout = findViewById(R.id.constraintLayoutSettings);
        parentLayout.addView(textView);

        // Set layout parameters
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.layout_height));

        textView.setLayoutParams(layoutParams);

        // Get the right text if the provider is known
        int textResources = getResources().getIdentifier(providerKey, "string", this.getPackageName());

        if (textResources != 0) {
            textView.setText(textResources);
        } else {
            textView.setText(providerKey);
        }

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) parentLayout);

        // Set margins
        int marginStart = getResources().getDimensionPixelSize(R.dimen.layout_margin_start_text);
        int marginEnd = getResources().getDimensionPixelSize(R.dimen.layout_margin_end);
        int marginTop = getResources().getDimensionPixelSize(R.dimen.layout_margin_top);

        // Set constraints for the TextView
        constraintSet.connect(textView.getId(), ConstraintSet.START, parentLayout.getId(), ConstraintSet.START, marginStart);
        constraintSet.connect(textView.getId(), ConstraintSet.END, parentLayout.getId(), ConstraintSet.END, marginEnd);
        constraintSet.connect(textView.getId(), ConstraintSet.TOP, topViewId, ConstraintSet.BOTTOM, marginTop);
        constraintSet.applyTo((ConstraintLayout) parentLayout);

        return textView;
    }


    private CheckBox generateCheckBox(String providerKey, int topViewId, int rightViewId) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setId(View.generateViewId());
        ViewGroup parentLayout = findViewById(R.id.constraintLayoutSettings);

        // Add CheckBox to the parent layout
        parentLayout.addView(checkBox);

        // Set layout parameters
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.layout_height));

        checkBox.setLayoutParams(layoutParams);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) parentLayout);

        // Set margins
        int marginStart = getResources().getDimensionPixelSize(R.dimen.layout_margin_start_checkbox);
        int marginEnd = getResources().getDimensionPixelSize(R.dimen.layout_margin_end);
        int marginTop = getResources().getDimensionPixelSize(R.dimen.layout_margin_top);

        // Set constraints for the CheckBox
        constraintSet.connect(checkBox.getId(), ConstraintSet.START, parentLayout.getId(), ConstraintSet.START, marginStart);
        constraintSet.connect(checkBox.getId(), ConstraintSet.END, parentLayout.getId(), ConstraintSet.END, marginEnd);
        constraintSet.connect(checkBox.getId(), ConstraintSet.TOP, topViewId, ConstraintSet.BOTTOM, marginTop);
        constraintSet.connect(checkBox.getId(), ConstraintSet.RIGHT, rightViewId, ConstraintSet.LEFT, 0);
        constraintSet.applyTo((ConstraintLayout) parentLayout);

        checkBox.setChecked(false);

        return checkBox;
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

        // Get parent ConstraintLayout
        ConstraintLayout parentLayout = findViewById(R.id.constraintLayoutSettings);

        // Save the CheckBoxes state for allowed and excluded providers
        for (String providerKey : availableProvidersKey) {
            // Retrieve the CheckBoxes with tags
            CheckBox checkBoxAllowed = parentLayout.findViewWithTag("allowed_check_" + providerKey);
            CheckBox checkBoxExcluded = parentLayout.findViewWithTag("excluded_check_" + providerKey);

            if (checkBoxAllowed.isChecked()) {
                allowedProviderKeys.add(providerKey);
            }

            if (checkBoxExcluded.isChecked()) {
                excludedProviderKeys.add(providerKey);
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
}
