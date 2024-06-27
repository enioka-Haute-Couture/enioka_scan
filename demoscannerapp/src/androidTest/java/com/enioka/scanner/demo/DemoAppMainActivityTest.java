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

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

public class DemoAppMainActivityTest {
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

    @Test
    public void testScannerActivityButtons() {
        // Portrait mode
        onView(withId(com.enioka.scanner.R.id.scanner_bt_camera)).check(matches(allOf(withText(com.enioka.scanner.R.string.camera_mode_button), isEnabled())));
        onView(withId(com.enioka.scanner.R.id.scanner_bt_provider_logs)).check(matches(allOf(withText(com.enioka.scanner.R.string.provider_log), isEnabled())));

        // Landscape mode
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

        onView(withId(com.enioka.scanner.R.id.scanner_bt_camera)).check(matches(allOf(withText(com.enioka.scanner.R.string.camera_mode_button), isEnabled())));
        onView(withId(com.enioka.scanner.R.id.scanner_bt_provider_logs)).check(matches(allOf(withText(com.enioka.scanner.R.string.provider_log), isEnabled())));

        // Reset to portrait mode
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0);
    }

    @Test
    public void testScannerActivityTextBarcode() {
        try (ActivityScenario<MainActivity> activityScenarioRule = ActivityScenario.launch(MainActivity.class)) {

            ArrayList<Barcode> testBarcode = new ArrayList<>();

            activityScenarioRule.onActivity(activity -> {
                testBarcode.add(new Barcode("1234567890", BarcodeType.CODE128));
                activity.onData(testBarcode);
            });
            onView(withId(com.enioka.scanner.R.id.scanner_text_last_scan)).check(matches(withText("TYPE: CODE128 1234567890")));

            activityScenarioRule.onActivity(activity -> {
                testBarcode.clear();
                testBarcode.add(new Barcode("1234567890", BarcodeType.AZTEC));
                activity.onData(testBarcode);
            });
            onView(withId(com.enioka.scanner.R.id.scanner_text_last_scan)).check(matches(withText("TYPE: AZTEC 1234567890")));

            activityScenarioRule.onActivity(activity -> {
                testBarcode.clear();
                testBarcode.add(new Barcode("89128391728", BarcodeType.EAN13));
                activity.onData(testBarcode);
            });
            onView(withId(com.enioka.scanner.R.id.scanner_text_last_scan)).check(matches(withText("TYPE: EAN13 89128391728")));
        }
    }

    @Test
    public void testCameraActivityTextBarcode() {
        try (ActivityScenario<MainActivity> activityScenarioRule = ActivityScenario.launch(MainActivity.class)) {
            // Go to camera mode
            onView(withId(com.enioka.scanner.R.id.scanner_bt_camera)).perform(click());

            ArrayList<Barcode> testBarcode = new ArrayList<>();

            activityScenarioRule.onActivity(activity -> {
                testBarcode.add(new Barcode("code-128-testing", BarcodeType.CODE128));
                activity.onData(testBarcode);
            });
            onView(withId(com.enioka.scanner.R.id.scanner_text_last_scan)).check(matches(withText("TYPE: CODE128 code-128-testing")));

            activityScenarioRule.onActivity(activity -> {
                testBarcode.clear();
                testBarcode.add(new Barcode("aztec-testing", BarcodeType.AZTEC));
                activity.onData(testBarcode);
            });
            onView(withId(com.enioka.scanner.R.id.scanner_text_last_scan)).check(matches(withText("TYPE: AZTEC aztec-testing")));

            activityScenarioRule.onActivity(activity -> {
                testBarcode.clear();
                testBarcode.add(new Barcode("89128391728", BarcodeType.EAN13));
                activity.onData(testBarcode);
            });
            onView(withId(com.enioka.scanner.R.id.scanner_text_last_scan)).check(matches(withText("TYPE: EAN13 89128391728")));
        }
    }

    @Test
    public void testCameraReaderMode() {
        // Portrait mode

        // Go to camera mode
        onView(withId(com.enioka.scanner.R.id.scanner_bt_camera)).perform(click());

        onView(withId(com.enioka.scanner.sdk.camera.R.id.scanner_zxing_text)).check(matches(withText(com.enioka.scanner.R.string.activity_scan_use_zxing)));

        // Should switch from ZXing to ZBar and show a snackbar
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scanner_switch_zxing)).check(matches(allOf(isDisplayed(), isEnabled()))).perform(click());

        onView(withText(com.enioka.scanner.R.string.snack_message_zxing)).check(matches(isDisplayed()));
        // Should switch from ZBar to ZXing and show a snackbar
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scanner_switch_zxing)).check(matches(allOf(isDisplayed(), isEnabled()))).perform(click());
        onView(withText(com.enioka.scanner.R.string.snack_message_zbar)).check(matches(isDisplayed()));

        onView(withId(com.enioka.scanner.sdk.camera.R.id.scanner_pause_text)).check(matches(withText(com.enioka.scanner.R.string.activity_scan_pause_camera)));

        // Landscape mode
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

        onView(withId(com.enioka.scanner.sdk.camera.R.id.scanner_zxing_text)).check(matches(withText(com.enioka.scanner.R.string.activity_scan_use_zxing)));

        // Should switch from ZXing to ZBar and show a snackbar
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scanner_switch_zxing)).check(matches(allOf(isDisplayed(), isEnabled()))).perform(click());

        onView(withText(com.enioka.scanner.R.string.snack_message_zxing)).check(matches(isDisplayed()));
        // Should switch from ZBar to ZXing and show a snackbar
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scanner_switch_zxing)).check(matches(allOf(isDisplayed(), isEnabled()))).perform(click());
        onView(withText(com.enioka.scanner.R.string.snack_message_zbar)).check(matches(isDisplayed()));

        onView(withId(com.enioka.scanner.sdk.camera.R.id.scanner_pause_text)).check(matches(withText(com.enioka.scanner.R.string.activity_scan_pause_camera)));

        // Reset to portrait mode
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0);
    }

    @Test
    public void testCameraActivityButtons() {
        // Go to camera mode
        onView(withId(com.enioka.scanner.R.id.scanner_bt_camera)).perform(click());

        // Portrait mode

        // Scanner flashlight
        onView(withId(com.enioka.scanner.R.id.scanner_flashlight)).check(matches(isDisplayed()));
        // Scanner provider logs
        onView(withId(com.enioka.scanner.R.id.scanner_bt_provider_logs)).check(matches(isDisplayed()));
        // Scanner reader mode switch
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scanner_switch_zxing)).check(matches(isDisplayed()));
        // Scanner camera pause / resume
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scanner_pause_text)).check(matches(isDisplayed()));
        // Scanner provider status
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scanner_provider_text)).check(matches(withText("CAMERA_SCANNER")));

        // Landscape mode

        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

        // Scanner flashlight
        onView(withId(com.enioka.scanner.R.id.scanner_flashlight)).check(matches(isDisplayed()));
        // Scanner provider logs
        onView(withId(com.enioka.scanner.R.id.scanner_bt_provider_logs)).check(matches(isDisplayed()));
        // Scanner reader mode switch
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scanner_switch_zxing)).check(matches(isDisplayed()));
        // Scanner camera pause / resume
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scanner_pause_text)).check(matches(isDisplayed()));
        // Scanner provider status
        onView(withId(com.enioka.scanner.sdk.camera.R.id.scanner_provider_text)).check(matches(allOf(withText("CAMERA_SCANNER"), isDisplayed())));

        // Reset to portrait mode
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0);
    }
}
