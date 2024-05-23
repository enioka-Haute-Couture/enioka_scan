package com.enioka.scanner.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.enioka.scanner.LaserScanner;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.service.ScannerService;
import com.enioka.scanner.service.ScannerServiceApi;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {
    private List<String> availableProviders = new ArrayList<>();
    protected MaterialButton bt1 = null;
    protected FloatingActionButton bugReportBt = null;
    protected MaterialButton bt5 = null;

    protected MaterialToolbar toolbar = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean landscape = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        setContentView(R.layout.activity_welcome);

        // Dynamically switch the layout based on the orientation
        if (landscape) {
            switchWelcomeActivity(false);
        }

        LaserScanner.discoverProviders(this, () -> {});
        availableProviders = LaserScanner.getProviderCache();
        initViews();
    }

    // Show the menu in the top action tool bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void initViews() {
        bt1 = findViewById(R.id.bt_scanner);
        bt5 = findViewById(R.id.bt_settings);
        toolbar = findViewById(R.id.topAppBar);
        bugReportBt = findViewById(R.id.bug_report_button);

        bt1.setOnClickListener(this::onClickBt1);
        bt5.setOnClickListener(this::onClickBt5);
        bugReportBt.setOnClickListener(this::onClickReportBug);
        setSupportActionBar(toolbar);
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
        // add logging intent extra
        intent.putExtra(SettingsActivity.ENABLE_LOGGING_KEY, preferences.getBoolean(SettingsActivity.ENABLE_LOGGING_KEY, false));
        // add allow camera fallback intent extra
        intent.putExtra(SettingsActivity.ALLOW_CAMERA_FALLBACK_KEY, preferences.getBoolean(SettingsActivity.ALLOW_CAMERA_FALLBACK_KEY, false));

        startActivity(intent);
    }

    // Scanner settings activity
    public void onClickBt5(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putStringArrayListExtra("providers", (ArrayList<String>) availableProviders);
        startActivity(intent);
    }

    // Report bug function button
    public void onClickReportBug(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/enioka-Haute-Couture/enioka_scan/issues/new"));
        startActivity(browserIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.github_button) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/enioka-Haute-Couture/enioka_scan"));
            startActivity(browserIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switchWelcomeActivity(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    public void switchWelcomeActivity(boolean portrait) {
        View welcome_card = findViewById(R.id.welcome_card);
        View barcode_image = findViewById(R.id.barcode);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) welcome_card.getLayoutParams();
        LinearLayout.LayoutParams paramsBarcode = (LinearLayout.LayoutParams) barcode_image.getLayoutParams();

        if (portrait) {
            params.matchConstraintPercentWidth = 0.8f;
            paramsBarcode.height = 90 * (int) getResources().getDisplayMetrics().density;
        } else {
            params.matchConstraintPercentWidth = 0.5f;
            paramsBarcode.height = 100 * (int) getResources().getDisplayMetrics().density;
        }
        welcome_card.setLayoutParams(params);
        barcode_image.setLayoutParams(paramsBarcode);
    }
}
