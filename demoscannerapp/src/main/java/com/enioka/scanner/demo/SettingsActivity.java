package com.enioka.scanner.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.service.ScannerService;
import com.enioka.scanner.service.ScannerServiceApi;
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
     * Symbology segmented button preferences keys
     */
    protected static final String SYMBOLOGY_STATE_KEY = "segmentedSymbologyButton";

    /**
     * Enable logging preferences key
     */
    public static final String ENABLE_LOGGING_KEY = "enableLogging";
    /**
     * Allow camera fallback preferences key
     */
    public static final String ALLOW_CAMERA_FALLBACK_KEY = "allowCameraFallback";
    /**
     * Enable keep aspect ratio preferences key
     */
    public static final String ENABLE_KEEP_ASPECT_RATIO_KEY = "enableKeepAspectRatio";

    /**
     * List of provider views ids
     */
    protected List<Integer> providerViews = new ArrayList<>();

    /**
     * Save button
     */
    protected MaterialButton buttonSave;

    /**
     * Select all segmented button
     */
    protected MaterialButton btAll;

    /**
     * Select specific segmented providers button
     */
    protected MaterialButton btSpec;

    /**
     * Select none segmented providers button
     */
    protected MaterialButton btNone;

    /**
     * SegmentedButtons providers state
     */
    protected int segmentedButtonProvidersState = 0;

    /**
     * Select stretch segmented aspect ratio mode button
     */
    protected MaterialButton btStretch;

    /**
     * Select black bars segmented aspect ratio mode button
     */
    protected MaterialButton btBlackBars;

    /**
     * Select crop segmented aspect ratio mode button
     */
    protected MaterialButton btCrop;

    /**
     * SegmentedButtons aspect ratio mode state
     */
    protected int aspectRatioMode = 0;

    /**
     * SegmentedButtons state
     */
    protected int segmentedButtonState = 0;
    protected int segmentedSymbologyState = 0;
    /**
     * Select all segmented button symbology
     */
    protected MaterialButton btAllSymbology;
    /**
     * Select specific segmented button symbology
     */
    protected MaterialButton btSpecSymbology;

    /**
     * Select none segmented button symbology
     */
    protected MaterialButton btNoneSymbology;
    /**
     * Symbology selection expanded state
     */
    private boolean isSymbologyExpanded = false;
    /**
     * Provider selection expanded state
     */
    private boolean isProviderExpanded = false;
    /**
     * Animated vector drawables for the expand buttons
     */
    private AnimatedVectorDrawableCompat animProvider = null;
    private AnimatedVectorDrawableCompat animProviderReverse = null;
    private AnimatedVectorDrawableCompat animSymbology = null;
    private AnimatedVectorDrawableCompat animSymbologyReverse = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Setup save button listener
        buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(this::onClickSave);

        // Segmented providers toggle buttons
        btAll = findViewById(R.id.buttonAll);
        btSpec = findViewById(R.id.buttonSpecific);
        btNone = findViewById(R.id.buttonNone);

        // Segmented aspect ratio mode toggle buttons
        btStretch = findViewById(R.id.buttonFillStretch);
        btBlackBars = findViewById(R.id.buttonFillBlackBars);
        btCrop = findViewById(R.id.buttonFillCrop);

        // Segmented toggle buttons for symbology
        btAllSymbology = findViewById(R.id.buttonAllSymbology);
        btSpecSymbology = findViewById(R.id.buttonSpecificSymbology);
        btNoneSymbology = findViewById(R.id.buttonNoneSymbology);

        // Add listener to segmented toggle buttons
        bindToggleButtonAspectRatioMode();
        bindToggleButtonProviders();
        bindToggleButtonSymbology();

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
        ((MaterialSwitch) findViewById(R.id.switchAllowCameraFallback)).setChecked(preferences.getBoolean(ALLOW_CAMERA_FALLBACK_KEY, false));

        final Set<String> allowedProviderKeys = preferences.getStringSet(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, Collections.emptySet());

        // Set the state of the segmented button
        segmentedButtonProvidersState = preferences.getInt(PREFS_KEY, 0);

        // On previous version, the key was a boolean
        try {
            aspectRatioMode = preferences.getInt(ENABLE_KEEP_ASPECT_RATIO_KEY, 0);
        } catch (ClassCastException e) {
            aspectRatioMode = 0;
        }

        segmentedButtonState = preferences.getInt(PREFS_KEY, 0);
        segmentedSymbologyState = preferences.getInt(SYMBOLOGY_STATE_KEY, 0);

        final Set<BarcodeType> symbologySelection = new HashSet<>();
        for (String symbology : preferences.getStringSet(ScannerServiceApi.EXTRA_SYMBOLOGY_SELECTION, ScannerService.defaultSymbologyByName())) {
            symbologySelection.add(BarcodeType.valueOf(symbology));
        }

        // Get detected SDKs providers from the intent
        ArrayList<String> availableProvidersIntent = getIntent().getStringArrayListExtra("providers");

        if (availableProvidersIntent == null) {
            availableProvidersKey = new String[0];
        } else {
            availableProvidersKey = availableProvidersIntent.toArray(new String[0]);
        }

        // Generate the provider / symbology list in the UI
        genProviderList(availableProvidersKey, allowedProviderKeys);
        genSymbologyList(symbologySelection);

        animProviderReverse = AnimatedVectorDrawableCompat.create(this, R.drawable.arrow_up_to_down_provider);
        animSymbologyReverse = AnimatedVectorDrawableCompat.create(this, R.drawable.arrow_up_to_down_symbology);
        animProvider = AnimatedVectorDrawableCompat.create(this, R.drawable.arrow_down_to_up_provider);
        animSymbology = AnimatedVectorDrawableCompat.create(this, R.drawable.arrow_down_to_up_symbology);

        // Set the listener for the expand buttons
        findViewById(R.id.buttonExpandProviderSelection).setOnClickListener(this::onClickExpandProvider);
        findViewById(R.id.buttonExpandSymbologySelection).setOnClickListener(this::onClickExpandSymbology);
    }

    /**
     * Expand or collapse the symbology selection
     */
    private void onClickExpandSymbology(View v) {
        (findViewById(R.id.toggleButtonSymbology)).setVisibility(isSymbologyExpanded ? View.GONE : View.VISIBLE);

        // Get parent ConstraintLayout
        ConstraintLayout parentLayout = findViewById(R.id.constraintLayoutSettings);

        for (BarcodeType symbology : BarcodeType.values()) {
            if (symbology == BarcodeType.UNKNOWN) {
                continue;
            }
            CheckBox checkBox = parentLayout.findViewWithTag("checkbox_" + symbology.name());

            if (checkBox == null) {
                Log.w("SettingsActivity", "CheckBox not found for symbology: " + symbology.name());
                continue;
            }

            if (isSymbologyExpanded) {
                checkBox.setVisibility(View.GONE);
            } else {
                checkBox.setVisibility(View.VISIBLE);
            }
        }
        ((ImageButton) v).setImageDrawable(isSymbologyExpanded ? animSymbologyReverse : animSymbology);
        ((AnimatedVectorDrawableCompat) ((ImageButton) v).getDrawable()).start();

        isSymbologyExpanded = !isSymbologyExpanded;
    }

    /**
     * Expand or collapse the provider selection
     */
    private void onClickExpandProvider(View v) {
        (findViewById(R.id.toggleButtonProvider)).setVisibility(isProviderExpanded ? View.GONE : View.VISIBLE);

        // Get parent ConstraintLayout
        ConstraintLayout parentLayout = findViewById(R.id.constraintLayoutSettings);

        for (String providerKey : availableProvidersKey) {
            CheckBox checkBox = parentLayout.findViewWithTag("checkbox_" + providerKey);

            if (checkBox == null) {
                Log.w("SettingsActivity", "CheckBox not found for providerKey: " + providerKey);
                continue;
            }

            if (isProviderExpanded) {
                checkBox.setVisibility(View.GONE);
            } else {
                checkBox.setVisibility(View.VISIBLE);
            }
        }
        ((ImageButton) v).setImageDrawable(isProviderExpanded ? animProviderReverse : animProvider);
        ((AnimatedVectorDrawableCompat) ((ImageButton) v).getDrawable()).start();

        isProviderExpanded = !isProviderExpanded;
    }

    /**
     * Gen the symbology list
     */
    private void genSymbologyList(Set<BarcodeType> symbologySelection) {
        int topViewId = R.id.toggleButtonSymbology;

        for (BarcodeType symbology : BarcodeType.values()) {
            if (symbology == BarcodeType.UNKNOWN) {
                continue;
            }

            // Create the UI for the allowed symbologies
            CheckBox checkBox = generateCheckBox(symbology.name(), topViewId, false);
            checkBox.setTag("checkbox_" + symbology.name());

            if (symbologySelection.contains(symbology)) {
                checkBox.setChecked(true);
            }

            topViewId = checkBox.getId();
        }

        updateConstraintLayout(topViewId, R.id.marginBottom);

        setSymbologySegmentedButtonState();
    }

    /**
     * Gen the provider list
     */
    private void genProviderList(String[] availableProvidersKey, Set<String> allowedProviderKeys) {
        // Create the UI
        int topViewId = R.id.toggleButtonProvider;

        for (String providerKey : availableProvidersKey) {

            // Create the UI for the allowed providers
            CheckBox checkBox = generateCheckBox(providerKey, topViewId, true);
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
            updateConstraintLayout(topViewId, R.id.dividerSettingsSymbology);
        }

        // Set the state of the segmented button (default is all)
        setSegmentedButtonState(true);
        setSegmentedButtonState(false);
    }

    private void updateConstraintLayout(int topViewId, int bottomViewId) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) findViewById(R.id.constraintLayoutSettings));

        // Init marginTop
        int marginTop = getResources().getDimensionPixelSize(R.dimen.layout_margin_top_divider);

        // Set top bottom constraints
        constraintSet.connect(bottomViewId, ConstraintSet.TOP, topViewId, ConstraintSet.BOTTOM, marginTop);

        // Apply constraints
        constraintSet.applyTo(findViewById(R.id.constraintLayoutSettings));
    }


    private CheckBox generateCheckBox(String label, int topViewId, boolean provider) {
        MaterialCheckBox checkBox = new MaterialCheckBox(this);

        // Gen checkBox id
        int checkBoxId = View.generateViewId();

        checkBox.setId(checkBoxId);

        if (provider) {
            providerViews.add(checkBoxId);
        }

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
        checkBox.setVisibility(View.GONE);

        // Get the right text if the label is known
        int textResources = getResources().getIdentifier(label, "string", this.getPackageName());

        if (textResources != 0) {
            // Set the custom text if available
            checkBox.setText(textResources);
        } else {
            // Otherwise set to the provider key
            checkBox.setText(label);
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
        editor.putBoolean(ALLOW_CAMERA_FALLBACK_KEY, ((MaterialSwitch) findViewById(R.id.switchAllowCameraFallback)).isChecked());

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

        editor.putInt(PREFS_KEY, segmentedButtonProvidersState);
        editor.putInt(ENABLE_KEEP_ASPECT_RATIO_KEY, aspectRatioMode);

        editor.putInt(SYMBOLOGY_STATE_KEY, segmentedSymbologyState);

        final Set<String> symbologySelection = new HashSet<>();

        for (BarcodeType symbology : BarcodeType.values()) {
            CheckBox checkBox = parentLayout.findViewWithTag("checkbox_" + symbology.name());
            if (checkBox != null && checkBox.isChecked()) {
                symbologySelection.add(symbology.name());
            }
        }

        editor.putStringSet(ScannerServiceApi.EXTRA_SYMBOLOGY_SELECTION, symbologySelection);

        editor.apply();
        finish();
    }


    /**
     * Bind toggle buttons
     */
    private void bindToggleButtonProviders() {
        btAll.setOnClickListener(v -> {
            if (btAll.isChecked()) {
                btAll.setIcon(ContextCompat.getDrawable(this, R.drawable.check_all));
                btSpec.setIcon(null);
                btNone.setIcon(null);

                for (int providerViewId : providerViews) {
                    // Force check and disable
                    ((CheckBox) findViewById(providerViewId)).setChecked(true);
                    findViewById(providerViewId).setEnabled(false);
                }

                segmentedButtonProvidersState = 0;
            }
        });
        btSpec.setOnClickListener(v -> {
            if (btSpec.isChecked()) {
                btSpec.setIcon(ContextCompat.getDrawable(this, R.drawable.search));
                btAll.setIcon(null);
                btNone.setIcon(null);

                for (int providerViewId : providerViews) {
                    // Force enable
                    findViewById(providerViewId).setEnabled(true);
                }

                segmentedButtonProvidersState = 1;
            }
        });
        btNone.setOnClickListener(v -> {
            if (btNone.isChecked()) {
                btNone.setIcon(ContextCompat.getDrawable(this, R.drawable.cross));
                btSpec.setIcon(null);
                btAll.setIcon(null);

                for (int providerViewId : providerViews) {
                    // Force uncheck and disable
                    ((CheckBox) findViewById(providerViewId)).setChecked(false);
                    findViewById(providerViewId).setEnabled(false);
                }

                segmentedButtonProvidersState = 2;
            }
        });
    }

    /**
     * Bind toggle button aspect ratio mode
     */

    private void bindToggleButtonAspectRatioMode() {
        btCrop.setOnClickListener(v -> {
            if (btCrop.isChecked()) {
                btCrop.setText(R.string.fill_crop);
                btStretch.setText(null);
                btBlackBars.setText(null);

                aspectRatioMode = 0;
            }
        });
        btBlackBars.setOnClickListener(v -> {
            if (btBlackBars.isChecked()) {
                btBlackBars.setText(R.string.fill_black_bars);
                btStretch.setText(null);
                btCrop.setText(null);

                aspectRatioMode = 1;
            }
        });
        btStretch.setOnClickListener(v -> {
            if (btStretch.isChecked()) {
                btStretch.setText(R.string.fill_stretch);
                btBlackBars.setText(null);
                btCrop.setText(null);

                aspectRatioMode = 2;
            }
        });
    }

    /**
     * Map the symbology button
     */
    private void bindToggleButtonSymbology() {
        ConstraintLayout parentLayout = findViewById(R.id.constraintLayoutSettings);

        btAllSymbology.setOnClickListener(v -> {
            if (btAllSymbology.isChecked()) {
                btAllSymbology.setIcon(ContextCompat.getDrawable(this, R.drawable.check_all));
                btSpecSymbology.setIcon(null);
                btNoneSymbology.setIcon(null);

                for (BarcodeType symbology : BarcodeType.values()) {
                    CheckBox checkBox = parentLayout.findViewWithTag("checkbox_" + symbology.name());
                    if (checkBox != null) {
                        checkBox.setChecked(true);
                        checkBox.setEnabled(false);
                    }
                }
                segmentedSymbologyState = 0;
            }
        });
        btSpecSymbology.setOnClickListener(v -> {
            if (btSpecSymbology.isChecked()) {
                btSpecSymbology.setIcon(ContextCompat.getDrawable(this, R.drawable.search));
                btAllSymbology.setIcon(null);
                btNoneSymbology.setIcon(null);

                for (BarcodeType symbology : BarcodeType.values()) {
                    CheckBox checkBox = parentLayout.findViewWithTag("checkbox_" + symbology.name());
                    if (checkBox != null) {
                        checkBox.setEnabled(true);
                    }
                }
                segmentedSymbologyState = 1;
            }
        });
        btNoneSymbology.setOnClickListener(v -> {
            if (btNoneSymbology.isChecked()) {
                btAllSymbology.setIcon(null);
                btSpecSymbology.setIcon(null);
                btNoneSymbology.setIcon(ContextCompat.getDrawable(this, R.drawable.cross));

                for (BarcodeType symbology : BarcodeType.values()) {
                    CheckBox checkBox = parentLayout.findViewWithTag("checkbox_" + symbology.name());
                    if (checkBox != null) {
                        checkBox.setChecked(false);
                        checkBox.setEnabled(false);
                    }
                }
                segmentedSymbologyState = 2;
            }
        });
    }

    /**
     * Set the state of segmented button for provider and aspect ratio mode
     */
    private void setSegmentedButtonState(boolean isProvider) {
        MaterialButton bt_1 = isProvider ? btAll : btCrop;
        MaterialButton bt_2 = isProvider ? btSpec : btBlackBars;
        MaterialButton bt_3 = isProvider ? btNone : btStretch;

        int buttonState = isProvider ? segmentedButtonProvidersState : aspectRatioMode;

        switch (buttonState) {
            case 0:
                bt_1.performClick();
                bt_2.setChecked(false);
                bt_3.setChecked(false);
                break;
            case 1:
                bt_1.setChecked(false);
                bt_2.performClick();
                bt_3.setChecked(false);
                break;
            case 2:
                bt_1.setChecked(false);
                bt_2.setChecked(false);
                bt_3.performClick();
                break;
        }
    }

    /**
     * Set the state of the symbology segmented button
     */
    private void setSymbologySegmentedButtonState() {
        switch (segmentedSymbologyState) {
            case 0:
                btAllSymbology.performClick();
                btSpecSymbology.setChecked(false);
                btNoneSymbology.setChecked(false);
                break;
            case 1:
                btAllSymbology.setChecked(false);
                btSpecSymbology.performClick();
                btNoneSymbology.setChecked(false);
                break;
            case 2:
                btAllSymbology.setChecked(false);
                btSpecSymbology.setChecked(false);
                btNoneSymbology.performClick();
                break;
        }
    }
}
