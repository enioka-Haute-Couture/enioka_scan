package com.enioka.scanner.demo;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.Manifest;
import android.app.UiAutomation;
import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.enioka.scanner.activities.SnackbarResource;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.service.ScannerServiceApi;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

public class DemoAppMainActivityTest {
    private ViewVisibleIdlingResource viewVisibleIdlingResource;

    private Intent getExtraIntentSymbologies() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ArrayList<String> symbologySelection = new ArrayList<>();
        symbologySelection.add(BarcodeType.CODE128.name());
        symbologySelection.add(BarcodeType.AZTEC.name());
        symbologySelection.add(BarcodeType.EAN13.name());
        intent.putExtra(ScannerServiceApi.EXTRA_SYMBOLOGY_SELECTION, symbologySelection.toArray(new String[0]));

        return intent;
    }

    @Rule
    public GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        IdlingRegistry.getInstance().register(SnackbarResource.countingIdlingResource);
    }

    @After
    public void tearDown() {
        if (viewVisibleIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(viewVisibleIdlingResource);
        }
        IdlingRegistry.getInstance().unregister(SnackbarResource.countingIdlingResource);
    }

    @Test
    public void testScannerActivityButtons() {
        // Portrait mode
        onView(withId(com.enioka.scanner.R.id.scannerBtCamera)).check(matches(allOf(withText(com.enioka.scanner.R.string.camera_mode_button), isEnabled())));
        onView(withId(com.enioka.scanner.R.id.scannerBtProviderLogs)).check(matches(allOf(withText(com.enioka.scanner.R.string.provider_log), isEnabled())));

        // Landscape mode
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

        // Wait that main activity layout is correctly displayed
        activityScenarioRule.getScenario().onActivity(activity -> {
            viewVisibleIdlingResource = new ViewVisibleIdlingResource(activity.findViewById(com.enioka.scanner.R.id.scannerBtCamera));
            IdlingRegistry.getInstance().register(viewVisibleIdlingResource);
        });

        onView(withId(com.enioka.scanner.R.id.scannerBtCamera)).check(matches(allOf(withText(com.enioka.scanner.R.string.camera_mode_button), isEnabled())));
        onView(withId(com.enioka.scanner.R.id.scannerBtProviderLogs)).check(matches(allOf(withText(com.enioka.scanner.R.string.provider_log), isEnabled())));

        // Reset to portrait mode
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0);
    }

    @Test
    public void testScannerActivityTextBarcode() {
        try (ActivityScenario<MainActivity> activityScenarioRule = ActivityScenario.launch(getExtraIntentSymbologies())) {

            ArrayList<Barcode> testBarcode = new ArrayList<>();

            activityScenarioRule.onActivity(activity -> {
                testBarcode.add(new Barcode("1234567890", BarcodeType.CODE128));
                activity.onData(testBarcode);
            });
            onView(withId(com.enioka.scanner.R.id.scannerTextLastScan)).check(matches(withText("TYPE: CODE128 1234567890")));

            activityScenarioRule.onActivity(activity -> {
                testBarcode.clear();
                testBarcode.add(new Barcode("1234567890", BarcodeType.AZTEC));
                activity.onData(testBarcode);
            });
            onView(withId(com.enioka.scanner.R.id.scannerTextLastScan)).check(matches(withText("TYPE: AZTEC 1234567890")));

            activityScenarioRule.onActivity(activity -> {
                testBarcode.clear();
                testBarcode.add(new Barcode("89128391728", BarcodeType.EAN13));
                activity.onData(testBarcode);
            });
            onView(withId(com.enioka.scanner.R.id.scannerTextLastScan)).check(matches(withText("TYPE: EAN13 89128391728")));
        }
    }

    @Test
    public void testCameraActivityTextBarcode() {
        try (ActivityScenario<MainActivity> activityScenarioRule = ActivityScenario.launch(getExtraIntentSymbologies())) {
            // Go to camera mode
            onView(withId(com.enioka.scanner.R.id.scannerBtCamera)).perform(click());

            // Wait that camera layout is correctly displayed
            activityScenarioRule.onActivity(activity -> {
                viewVisibleIdlingResource = new ViewVisibleIdlingResource(activity.findViewById(com.enioka.scanner.sdk.camera.R.id.scannerZxingText));
                IdlingRegistry.getInstance().register(viewVisibleIdlingResource);
            });

            ArrayList<Barcode> testBarcode = new ArrayList<>();

            activityScenarioRule.onActivity(activity -> {
                testBarcode.add(new Barcode("code-128-testing", BarcodeType.CODE128));
                activity.onData(testBarcode);
            });
            onView(withId(com.enioka.scanner.R.id.scannerTextLastScan)).check(matches(withText("TYPE: CODE128 code-128-testing")));

            activityScenarioRule.onActivity(activity -> {
                testBarcode.clear();
                testBarcode.add(new Barcode("aztec-testing", BarcodeType.AZTEC));
                activity.onData(testBarcode);
            });
            onView(withId(com.enioka.scanner.R.id.scannerTextLastScan)).check(matches(withText("TYPE: AZTEC aztec-testing")));

            activityScenarioRule.onActivity(activity -> {
                testBarcode.clear();
                testBarcode.add(new Barcode("89128391728", BarcodeType.EAN13));
                activity.onData(testBarcode);
            });
            onView(withId(com.enioka.scanner.R.id.scannerTextLastScan)).check(matches(withText("TYPE: EAN13 89128391728")));
        }
    }

    @Test
    public void testCameraReaderMode() {
        // Portrait mode
        // Go to camera mode
        onView(withId(com.enioka.scanner.R.id.scannerBtCamera)).perform(click());

        activityScenarioRule.getScenario().onActivity(activity -> {
            viewVisibleIdlingResource = new ViewVisibleIdlingResource(activity.findViewById(com.enioka.scanner.sdk.camera.R.id.scannerZxingText));
            IdlingRegistry.getInstance().register(viewVisibleIdlingResource);
        });


        onView(withId(com.enioka.scanner.sdk.camera.R.id.scannerZxingText)).check(matches(withText(com.enioka.scanner.R.string.activity_scan_use_zxing)));

        // Should switch from ZXing to ZBar and show a snackbar
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scannerSwitchZxing)).check(matches(allOf(isDisplayed(), isEnabled()))).perform(click());

        onView(withText(com.enioka.scanner.R.string.snack_message_zxing)).check(matches(isDisplayed()));
        // Should switch from ZBar to ZXing and show a snackbar
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scannerSwitchZxing)).check(matches(allOf(isDisplayed(), isEnabled()))).perform(click());
        onView(withText(com.enioka.scanner.R.string.snack_message_zbar)).check(matches(isDisplayed()));

        onView(withId(com.enioka.scanner.sdk.camera.R.id.scannerPauseText)).check(matches(withText(com.enioka.scanner.R.string.activity_scan_pause_camera)));

        // Landscape mode
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

        activityScenarioRule.getScenario().onActivity(activity -> {
            viewVisibleIdlingResource = new ViewVisibleIdlingResource(activity.findViewById(com.enioka.scanner.sdk.camera.R.id.scannerZxingText));
            IdlingRegistry.getInstance().register(viewVisibleIdlingResource);
        });

        onView(withId(com.enioka.scanner.sdk.camera.R.id.scannerZxingText)).check(matches(withText(com.enioka.scanner.R.string.activity_scan_use_zxing)));

        // Should switch from ZXing to ZBar and show a snackbar
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scannerSwitchZxing)).check(matches(allOf(isDisplayed(), isEnabled()))).perform(click());

        onView(withText(com.enioka.scanner.R.string.snack_message_zxing)).check(matches(isDisplayed()));
        // Should switch from ZBar to ZXing and show a snackbar
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scannerSwitchZxing)).check(matches(allOf(isDisplayed(), isEnabled()))).perform(click());
        onView(withText(com.enioka.scanner.R.string.snack_message_zbar)).check(matches(isDisplayed()));

        onView(withId(com.enioka.scanner.sdk.camera.R.id.scannerPauseText)).check(matches(withText(com.enioka.scanner.R.string.activity_scan_pause_camera)));

        // Reset to portrait mode
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0);
    }

    @Test
    public void testCameraActivityButtons() {
        // Go to camera mode
        onView(withId(com.enioka.scanner.R.id.scannerBtCamera)).perform(click());

        // Wait that camera layout is correctly displayed
        activityScenarioRule.getScenario().onActivity(activity -> {
            viewVisibleIdlingResource = new ViewVisibleIdlingResource(activity.findViewById(com.enioka.scanner.sdk.camera.R.id.scannerZxingText));
            IdlingRegistry.getInstance().register(viewVisibleIdlingResource);
        });

        // [ Portrait mode test]

        // Scanner flashlight
        onView(withId(com.enioka.scanner.R.id.scannerFlashlight)).check(matches(isDisplayed()));
        // Scanner provider logs
        onView(withId(com.enioka.scanner.R.id.scannerBtProviderLogs)).check(matches(isDisplayed()));
        // Scanner reader mode switch
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scannerSwitchZxing)).check(matches(isDisplayed()));
        // Scanner camera pause / resume
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scannerPauseText)).check(matches(isDisplayed()));
        // Scanner provider status
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scannerProviderText)).check(matches(withText("CAMERA_SCANNER")));

        // [Landscape mode test]

        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

        // Wait that camera layout is correctly displayed
        activityScenarioRule.getScenario().onActivity(activity -> {
            viewVisibleIdlingResource = new ViewVisibleIdlingResource(activity.findViewById(com.enioka.scanner.sdk.camera.R.id.scannerZxingText));
            IdlingRegistry.getInstance().register(viewVisibleIdlingResource);
        });

        // Scanner flashlight
        onView(withId(com.enioka.scanner.R.id.scannerFlashlight)).check(matches(isDisplayed()));
        // Scanner provider logs
        onView(withId(com.enioka.scanner.R.id.scannerBtProviderLogs)).check(matches(isDisplayed()));
        // Scanner reader mode switch
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scannerSwitchZxing)).check(matches(isDisplayed()));
        // Scanner camera pause / resume
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scannerPauseText)).check(matches(isDisplayed()));
        // Scanner provider status
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scannerProviderText)).check(matches(allOf(withText("CAMERA_SCANNER"), isDisplayed())));

        // Reset to portrait mode
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0);
    }
}
