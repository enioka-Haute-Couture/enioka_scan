package com.enioka.scanner.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.service.ScannerService;
import com.enioka.scanner.service.ScannerServiceApi;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
    /**
     * List of available providers key
     */
    protected String[] availableProvidersKey = null;

    /**
     * Segmented button preferences keys
     */
    protected static final String PREFS_KEY = "segmentedButton";

    /**
     * Enable logging preferences key
     */
    public static final String ENABLE_LOGGING_KEY = "enableLogging";

    /**
     * List of provider views ids
     */
    protected List<Integer> providerViews = new ArrayList<>();

    /**
     * Save button
     */
    protected MaterialButton buttonSave;

    /**
     * topAppBar
     */
    protected MaterialToolbar topAppBar;

    /**
     * Select all segmented button
     */
    protected MaterialButton bt_all;

    /**
     * Select specific segmented button
     */
    protected MaterialButton bt_spec;

    /**
     * Select none segmented button
     */
    protected MaterialButton bt_none;

    /**
     * SegmentedButtons state
     */
    protected int segmentedButtonState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Setup save button listener
        buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(this::onClickSave);

        // Segmented toggle buttons
        bt_all = findViewById(R.id.button_all);
        bt_spec = findViewById(R.id.button_specific);
        bt_none = findViewById(R.id.button_none);

        // Add listener to segmented toggle buttons
        bindToggleButton();

        final SharedPreferences preferences = this.getSharedPreferences("ScannerSearchPreferences", MODE_PRIVATE);

        final ScannerSearchOptions options = ScannerSearchOptions.defaultOptions();
        ((MaterialSwitch) findViewById(R.id.switchWaitDisconnected)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_WAIT_DISCONNECTED_BOOLEAN, options.waitDisconnected));
        ((MaterialSwitch) findViewById(R.id.switchReturnOnlyFirst)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_RETURN_ONLY_FIRST_BOOLEAN, options.returnOnlyFirst));
        ((MaterialSwitch) findViewById(R.id.switchBluetooth)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_BT_BOOLEAN, options.useBlueTooth));
        ((MaterialSwitch) findViewById(R.id.switchLaterConnections)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_KEEP_SEARCHING_BOOLEAN, options.allowLaterConnections));
        ((MaterialSwitch) findViewById(R.id.switchInitialSearch)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INITIAL_SEARCH_BOOLEAN, options.allowInitialSearch));
        ((MaterialSwitch) findViewById(R.id.switchPairingFlow)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_PAIRING_FLOW_BOOLEAN, options.allowPairingFlow));
        ((MaterialSwitch) findViewById(R.id.switchIntentDevices)).setChecked(preferences.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INTENT_BOOLEAN, options.allowIntentDevices));
        ((MaterialSwitch) findViewById(R.id.switchEnableLogging)).setChecked(preferences.getBoolean(ENABLE_LOGGING_KEY, false));

        final Set<String> allowedProviderKeys = preferences.getStringSet(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, Collections.emptySet());

        // Set the state of the segmented button
        segmentedButtonState = preferences.getInt(PREFS_KEY, 0);

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
        int topViewId = R.id.toggleButtonProvider;

        for (String providerKey : availableProvidersKey) {

            // Create the UI for the allowed providers
            CheckBox checkBox = generateCheckBox(providerKey, topViewId);
            checkBox.setTag("checkbox_" + providerKey);

            if (allowedProviderKeys.contains(providerKey)) {
                checkBox.setChecked(true);
            }

            topViewId = checkBox.getId();
        }

        if (availableProvidersKey.length == 0) {
            // Hide the provider toggle button
            findViewById(R.id.toggleButtonProvider).setVisibility(View.GONE);
            findViewById(R.id.textEmptyProviderList).setVisibility(View.VISIBLE);
        } else {
            // Update the layout
            updateConstraintLayout(topViewId);
        }

        // Set the state of the segmented button (default is all)
        setSegmentedButtonState();
    }

    private void updateConstraintLayout(int topViewId) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) findViewById(R.id.constraintLayoutSettings));

        // Init marginTop
        int marginTop = getResources().getDimensionPixelSize(R.dimen.layout_margin_top_divider);

        // Set top bottom constraints
        constraintSet.connect(R.id.dividerSettingsSymbology, ConstraintSet.TOP, topViewId, ConstraintSet.BOTTOM, marginTop);

        // Apply constraints
        constraintSet.applyTo(findViewById(R.id.constraintLayoutSettings));
    }


    private CheckBox generateCheckBox(String providerKey, int topViewId) {
        MaterialCheckBox checkBox = new MaterialCheckBox(this);

        // Gen checkBox id
        int checkBoxId = View.generateViewId();

        checkBox.setId(checkBoxId);
        providerViews.add(checkBoxId);

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
        constraintSet.applyTo((ConstraintLayout) parentLayout);

        checkBox.setChecked(false);

        // Get the right text if the provider is known
        int textResources = getResources().getIdentifier(providerKey, "string", this.getPackageName());

        if (textResources != 0) {
            // Set the custom text if available
            checkBox.setText(textResources);
        } else {
            // Otherwise set to the provider key
            checkBox.setText(providerKey);
        }

        return checkBox;
    }

    public void onClickSave(View v) {
        final SharedPreferences.Editor editor = this.getSharedPreferences("ScannerSearchPreferences", MODE_PRIVATE).edit();

        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_WAIT_DISCONNECTED_BOOLEAN, ((MaterialSwitch) findViewById(R.id.switchWaitDisconnected)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_RETURN_ONLY_FIRST_BOOLEAN, ((MaterialSwitch) findViewById(R.id.switchReturnOnlyFirst)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_BT_BOOLEAN, ((MaterialSwitch) findViewById(R.id.switchBluetooth)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_KEEP_SEARCHING_BOOLEAN, ((MaterialSwitch) findViewById(R.id.switchLaterConnections)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INITIAL_SEARCH_BOOLEAN, ((MaterialSwitch) findViewById(R.id.switchInitialSearch)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_PAIRING_FLOW_BOOLEAN, ((MaterialSwitch) findViewById(R.id.switchPairingFlow)).isChecked());
        editor.putBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INTENT_BOOLEAN, ((MaterialSwitch) findViewById(R.id.switchIntentDevices)).isChecked());
        editor.putBoolean(ENABLE_LOGGING_KEY, ((MaterialSwitch) findViewById(R.id.switchEnableLogging)).isChecked());

        final Set<String> allowedProviderKeys = new HashSet<>();
        final Set<String> excludedProviderKeys = new HashSet<>();

        // Get parent ConstraintLayout
        ConstraintLayout parentLayout = findViewById(R.id.constraintLayoutSettings);

        // Save the CheckBoxes state for allowed and excluded providers
        for (String providerKey : availableProvidersKey) {
            // Retrieve the CheckBoxes with tags
            CheckBox checkBoxAllowed = parentLayout.findViewWithTag("checkbox_" + providerKey);

            if (checkBoxAllowed == null) {
                Log.w("SettingsActivity", "CheckBox not found for providerKey: " + providerKey);
                continue;
            }

            if (checkBoxAllowed.isChecked()) {
                allowedProviderKeys.add(providerKey);
            } else {
                excludedProviderKeys.add(providerKey);
            }
        }

        editor.putStringSet(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, allowedProviderKeys);
        editor.putStringSet(ScannerServiceApi.EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY, excludedProviderKeys);

        editor.putInt(PREFS_KEY, segmentedButtonState);

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
     * Bind toggle buttons
     */
    private void bindToggleButton() {
        bt_all.setOnClickListener(v -> {
            if (bt_all.isChecked()) {
                bt_all.setIcon(ContextCompat.getDrawable(this, R.drawable.check_all));
                bt_spec.setIcon(null);
                bt_none.setIcon(null);

                for (int providerViewId : providerViews) {
                    // Force check and disable
                    ((CheckBox) findViewById(providerViewId)).setChecked(true);
                    findViewById(providerViewId).setEnabled(false);
                }

                segmentedButtonState = 0;
            }
        });
        bt_spec.setOnClickListener(v -> {
            if (bt_spec.isChecked()) {
                bt_spec.setIcon(ContextCompat.getDrawable(this, R.drawable.search));
                bt_all.setIcon(null);
                bt_none.setIcon(null);

                for (int providerViewId : providerViews) {
                    // Force enable
                    findViewById(providerViewId).setEnabled(true);
                }

                segmentedButtonState = 1;
            }
        });
        bt_none.setOnClickListener(v -> {
            if (bt_none.isChecked()) {
                bt_none.setIcon(ContextCompat.getDrawable(this, R.drawable.cross));
                bt_spec.setIcon(null);
                bt_all.setIcon(null);

                for (int providerViewId : providerViews) {
                    // Force uncheck and disable
                    ((CheckBox) findViewById(providerViewId)).setChecked(false);
                    findViewById(providerViewId).setEnabled(false);
                }

                segmentedButtonState = 2;
            }
        });
    }

    /**
     * Set the state of segmented button
     */
    private void setSegmentedButtonState() {
        switch (segmentedButtonState) {
            case 0:
                bt_all.performClick();
                bt_spec.setChecked(false);
                bt_none.setChecked(false);
                break;
            case 1:
                bt_all.setChecked(false);
                bt_spec.performClick();
                bt_none.setChecked(false);
                break;
            case 2:
                bt_all.setChecked(false);
                bt_spec.setChecked(false);
                bt_none.performClick();
                break;
        }
    }
}
