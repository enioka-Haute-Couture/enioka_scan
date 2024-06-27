package com.enioka.scanner.demo;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

import android.app.UiAutomation;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.Arrays;
import java.util.List;

public class DemoAppWelcomeTest {
    static List<String> includedSdkProvider;
    static List<String> includedSdkProviderText;

    // Custom espresso matcher to check if a checkbox has a specific text
    public static Matcher<View> withCheckBoxText(final Object text) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("checkbox with text: ");
                description.appendValue(text);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof MaterialCheckBox)) {
                    return false;
                }

                MaterialCheckBox checkBox = (MaterialCheckBox) view;
                return text.equals(checkBox.getText());
            }
        };
    }

    // Custom espresso matcher to check if the view has a specific tag
    public static Matcher<View> withTag(final Object tag) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with tag: ");
                description.appendValue(tag);
            }

            @Override
            public boolean matchesSafely(View view) {
                return tag.equals(view.getTag());
            }
        };
    }

    // Get resource string
    public static String getResourceString(Integer text) {
        return InstrumentationRegistry.getInstrumentation().getTargetContext().getString(text);
    }


    @Rule
    public ActivityScenarioRule<WelcomeActivity> activityScenarioRule = new ActivityScenarioRule<>(WelcomeActivity.class);

    @Before
    public void setUp() {
        includedSdkProvider = Arrays.asList("AthesiE5LProvider", "AthesiHHTProvider", "BluebirdProvider", "ProgloveProvider", "BT_GeneralScanProvider", "BT_ZebraOssSPPProvider", "BT_ZebraOssATTProvider", "BT_HoneywellOssSppProvider", "HoneywellOssIntegratedProvider", "ZebraDwProvider");
        includedSdkProviderText = Arrays.asList("Athesi [E5L…]", "Athesi [SPA45…]", "Bluebird integrated [EF500…]", "Proglove [Glove Mark 2…]", "GeneralScan BLE ring", "Zebra BT Classic [RS5100…]", "Zebra BT Low Energy [RS5100…]", "HoneyWell BT [Voyager 1602…]", "HoneyWell integrated [EDA52…]", "Zebra DataWedge [TC25, TC27…]");
    }

    @Test
    public void testTextButtonScanner() {
        // Portrait mode
        onView(withId(R.id.bt_scanner)).check(matches(withText(R.string.test_scan)));

        // Landscape mode
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

        onView(withId(R.id.bt_scanner)).check(matches(withText(R.string.test_scan)));

        // Reset to portrait mode
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0);
    }

    @Test
    public void testTextButtonSettings() {
        // Portrait mode
        onView(withId(R.id.bt_settings)).check(matches(withText(R.string.settings)));

        // Landscape mode
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

        onView(withId(R.id.bt_settings)).check(matches(withText(R.string.settings)));

        // Reset to portrait mode
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0);
    }

    @Test
    public void testListOfSdkSettings() {
        onView(withId(R.id.bt_settings)).perform(click());

        // Check all providers clicked
        onView(withId(R.id.button_all)).perform(scrollTo()).perform(click()).check(matches(withText(R.string.toogle_provider_button_all)));
        for (int idx = 0; idx < includedSdkProvider.size(); idx++) {
            // Check if the SDK provider is in the lis
            onView(withTag("checkbox_" + includedSdkProvider.get(idx))).check(matches(withCheckBoxText(includedSdkProviderText.get(idx)))).check(matches(isChecked()));
        }

        // Check specific providers clicked
        onView(withId(R.id.button_specific)).perform(scrollTo()).perform(click()).check(matches(withText(R.string.toggle_provider_button_specific)));
        for (int idx = 0; idx < includedSdkProvider.size(); idx++) {
            // Check if the SDK provider is in the lis
            onView(withTag("checkbox_" + includedSdkProvider.get(idx))).check(matches(allOf(withCheckBoxText(includedSdkProviderText.get(idx)), isChecked(), isEnabled())));
        }

        // Check none providers clicked
        onView(withId(R.id.button_none)).perform(scrollTo()).perform(click()).check(matches(withText(R.string.toggle_provider_button_none)));
        for (int idx = 0; idx < includedSdkProvider.size(); idx++) {
            // Check if the SDK provider is in the lis
            onView(withTag("checkbox_" + includedSdkProvider.get(idx))).check(matches(allOf(withCheckBoxText(includedSdkProviderText.get(idx)), isNotChecked(), not(isEnabled()))));
        }
    }

    @Test
    public void testListOfSymbologiesSettings() {
        onView(ViewMatchers.withId(R.id.bt_settings)).perform(click());

        onView(withId(R.id.checkSelectCode128)).check(matches(withCheckBoxText(getResourceString(R.string.barcodeCode128))));
        onView(withId(R.id.checkSelectCode39)).check(matches(withCheckBoxText(getResourceString(R.string.barcodeCode39))));
        onView(withId(R.id.checkSelectDis25)).check(matches(withCheckBoxText(getResourceString(R.string.barcodeDis25))));
        onView(withId(R.id.checkSelectInt25)).check(matches(withCheckBoxText(getResourceString(R.string.barcodeInt25))));
        onView(withId(R.id.checkSelectEan13)).check(matches(withCheckBoxText(getResourceString(R.string.barcodeEan13))));
        onView(withId(R.id.checkSelectQrCode)).check(matches(withCheckBoxText(getResourceString(R.string.barcodeQrCode))));
        onView(withId(R.id.checkSelectAztec)).check(matches(withCheckBoxText(getResourceString(R.string.barcodeAztec))));
    }

    @Test
    public void testSwitchSettings() {
        onView(withId(R.id.bt_settings)).perform(click());

        // Portrait mode
        onView(withId(R.id.switchWaitDisconnected)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewWaitDisconnected)).check(matches(withText(R.string.wait_disconnected)));
        onView(withId(R.id.switchReturnOnlyFirst)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewReturnOnlyFirst)).check(matches(withText(R.string.return_only_first)));
        onView(withId(R.id.switchBluetooth)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewBluetooth)).perform(scrollTo()).check(matches(withText(R.string.use_bluetooth)));
        onView(withId(R.id.switchInitialSearch)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewInitialSearch)).check(matches(withText(R.string.allow_initial_search)));
        onView(withId(R.id.switchLaterConnections)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewLaterConnections)).check(matches(withText(R.string.allow_later_connections)));
        onView(withId(R.id.switchPairingFlow)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewPairingFlow)).check(matches(withText(R.string.allow_pairing_flow)));
        onView(withId(R.id.switchIntentDevices)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewIntentDevices)).check(matches(withText(R.string.allow_intent_devices)));
        onView(withId(R.id.switchEnableLogging)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewEnableLogging)).check(matches(withText(R.string.enable_log)));
        onView(withId(R.id.switchAllowCameraFallback)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewAllowCameraFallback)).check(matches(withText(R.string.allow_camera_fallback)));

        // Landscape mode
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

        onView(withId(R.id.switchWaitDisconnected)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewWaitDisconnected)).check(matches(withText(R.string.wait_disconnected)));
        onView(withId(R.id.switchReturnOnlyFirst)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewReturnOnlyFirst)).check(matches(withText(R.string.return_only_first)));
        onView(withId(R.id.switchBluetooth)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewBluetooth)).check(matches(withText(R.string.use_bluetooth)));
        onView(withId(R.id.switchInitialSearch)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewInitialSearch)).check(matches(withText(R.string.allow_initial_search)));
        onView(withId(R.id.switchLaterConnections)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewLaterConnections)).check(matches(withText(R.string.allow_later_connections)));
        onView(withId(R.id.switchPairingFlow)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewPairingFlow)).check(matches(withText(R.string.allow_pairing_flow)));
        onView(withId(R.id.switchIntentDevices)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewIntentDevices)).check(matches(withText(R.string.allow_intent_devices)));
        onView(withId(R.id.switchEnableLogging)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewEnableLogging)).check(matches(withText(R.string.enable_log)));
        onView(withId(R.id.switchAllowCameraFallback)).perform(scrollTo()).perform(click());
        onView(withId(R.id.textViewAllowCameraFallback)).check(matches(withText(R.string.allow_camera_fallback)));

        // Reset to portrait mode
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0);
    }

    @Test
    public void testToggleCameraRatioModeSettings() {
        onView(withId(R.id.bt_settings)).perform(click());
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0);

        try (ActivityScenario<SettingsActivity> activityScenario = ActivityScenario.launch(SettingsActivity.class)) {
            // Portrait mode
            onView(withId(R.id.button_fill_crop)).perform(scrollTo()).perform(click()).check(matches(withText(R.string.fill_crop)));
            activityScenario.onActivity(activity -> {
                activity.buttonSave.setVisibility(View.GONE);
                assertEquals(activity.aspectRatioMode, 0);
            });

            onView(withId(R.id.button_fill_black_bars)).perform(click()).check(matches(withText(R.string.fill_black_bars)));
            activityScenario.onActivity(activity -> {
                assertEquals(activity.aspectRatioMode, 1);
            });

            onView(withId(R.id.button_fill_stretch)).perform(click()).check(matches(withText(R.string.fill_stretch)));
            activityScenario.onActivity(activity -> {
                assertEquals(activity.aspectRatioMode, 2);
            });

            // Landscape mode
            uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

            onView(withId(R.id.button_fill_crop)).perform(scrollTo()).perform(click()).check(matches(withText(R.string.fill_crop)));
            activityScenario.onActivity(activity -> {
                activity.buttonSave.setVisibility(View.GONE);
                assertEquals(activity.aspectRatioMode, 0);
            });

            onView(withId(R.id.button_fill_black_bars)).perform(scrollTo()).perform(click()).check(matches(withText(R.string.fill_black_bars)));
            activityScenario.onActivity(activity -> {
                assertEquals(activity.aspectRatioMode, 1);
            });

            onView(withId(R.id.button_fill_stretch)).perform(scrollTo()).perform(click()).check(matches(withText(R.string.fill_stretch)));
            activityScenario.onActivity(activity -> {
                assertEquals(activity.aspectRatioMode, 2);
            });

            // Reset to portrait mode
            uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0);
        }
    }

    @Test
    public void testScannerLaunchedActivity() {
        // Start recording intent
        Intents.init();

        onView(withId(R.id.bt_scanner)).perform(click());

        // Check if main activity is launched
        intended(hasComponent(MainActivity.class.getName()));

        // Stop recording intent
        Intents.release();
    }
}
