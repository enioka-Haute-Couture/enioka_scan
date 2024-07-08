package com.enioka.scanner.demo;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
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
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import androidx.core.widget.NestedScrollView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ScrollToAction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.service.ScannerServiceApi;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DemoAppWelcomeTest {
    private ViewVisibleIdlingResource viewVisibleIdlingResource;
    private static List<String> includedSdkProvider;
    private static List<String> includedSdkProviderText;
    private static List<String> includedSymbologies;

    @Before
    public void setUp() {
        IdlingRegistry.getInstance().register(EspressoSemaphore.countingIdlingResource);
        includedSdkProvider = Arrays.asList("AthesiE5LProvider", "AthesiHHTProvider", "BluebirdProvider", "ProgloveProvider", "BT_GeneralScanProvider", "BT_ZebraOssSPPProvider", "BT_ZebraOssATTProvider", "BT_HoneywellOssSppProvider", "HoneywellOssIntegratedProvider", "ZebraDwProvider");
        includedSdkProviderText = Arrays.asList("Athesi [E5L…]", "Athesi [SPA45…]", "Bluebird integrated [EF500…]", "Proglove [Glove Mark 2…]", "GeneralScan BLE ring", "Zebra BT Classic [RS5100…]", "Zebra BT Low Energy [RS5100…]", "HoneyWell BT [Voyager 1602…]", "HoneyWell integrated [EDA52…]", "Zebra DataWedge [TC25, TC27…]");

        includedSymbologies = new ArrayList<>();
        for (BarcodeType barcode : BarcodeType.values()) {
            if (barcode != BarcodeType.UNKNOWN) {
                includedSymbologies.add(barcode.name());
            }
        }
    }

    @After
    public void tearDown() {
        if (viewVisibleIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(viewVisibleIdlingResource);
        }
        IdlingRegistry.getInstance().unregister(EspressoSemaphore.countingIdlingResource);
    }

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

    // Custom view action to perform nested scroll
    public static ViewAction nestedScrollTo() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return CoreMatchers.allOf(
                        ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                        ViewMatchers.isDescendantOfA(CoreMatchers.anyOf(
                                ViewMatchers.isAssignableFrom(ScrollView.class),
                                ViewMatchers.isAssignableFrom(HorizontalScrollView.class),
                                ViewMatchers.isAssignableFrom(NestedScrollView.class)
                        ))
                );
            }

            @Override
            public String getDescription() {
                return "not found";
            }

            @Override
            public void perform(UiController uiController, View view) {
                new ScrollToAction().perform(uiController, view);
            }
        };
    }

    @Rule
    public ActivityScenarioRule<WelcomeActivity> activityScenarioRule = new ActivityScenarioRule<>(WelcomeActivity.class);

    @Test
    public void testTextButtonScanner() {
        // Portrait mode
        onView(withId(R.id.btScanner)).check(matches(withText(R.string.test_scan)));

        // Landscape mode
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

        onView(withId(R.id.btScanner)).check(matches(withText(R.string.test_scan)));

        // Reset to portrait mode
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0);
    }

    @Test
    public void testTextButtonSettings() {
        // Portrait mode
        onView(withId(R.id.btSettings)).check(matches(withText(R.string.settings)));

        // Landscape mode
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

        onView(withId(R.id.btSettings)).check(matches(withText(R.string.settings)));

        // Reset to portrait mode
        uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0);
    }

    @Test
    @Ignore("activity settings is launched from the tests, not from the settings button")
    public void testListOfSdkSettings() {
        try (ActivityScenario<SettingsActivity> activityScenario = ActivityScenario.launch(SettingsActivity.class)) {
            activityScenario.onActivity(activity -> {
                activity.buttonSave.setVisibility(View.GONE);
                viewVisibleIdlingResource = new ViewVisibleIdlingResource(activity.findViewById(R.id.toggleButtonProvider));
                IdlingRegistry.getInstance().register(viewVisibleIdlingResource);
            });

            onView(withId(R.id.toggleButtonProvider)).perform(nestedScrollTo());

            // Check all providers clicked
            onView(withId(R.id.buttonAll)).perform(click()).check(matches(withText(R.string.toogle_provider_button_all)));
            for (int idx = 0; idx < includedSdkProvider.size(); idx++) {
                // Check if the SDK provider is in the lis
                onView(withTag("checkbox_" + includedSdkProvider.get(idx))).check(matches(withCheckBoxText(includedSdkProviderText.get(idx)))).check(matches(isChecked()));
            }

            // Check specific providers clicked
            onView(withId(R.id.buttonSpecific)).perform(click()).check(matches(withText(R.string.toggle_provider_button_specific)));
            for (int idx = 0; idx < includedSdkProvider.size(); idx++) {
                // Check if the SDK provider is in the lis
                onView(withTag("checkbox_" + includedSdkProvider.get(idx))).check(matches(allOf(withCheckBoxText(includedSdkProviderText.get(idx)), isChecked(), isEnabled())));
            }

            // Check none providers clicked
            onView(withId(R.id.buttonNone)).perform(click()).check(matches(withText(R.string.toggle_provider_button_none)));
            for (int idx = 0; idx < includedSdkProvider.size(); idx++) {
                // Check if the SDK provider is in the lis
                onView(withTag("checkbox_" + includedSdkProvider.get(idx))).check(matches(allOf(withCheckBoxText(includedSdkProviderText.get(idx)), isNotChecked(), not(isEnabled()))));
            }
        }
    }

    @Test
    public void testListOfSymbologiesSettings() {
        try (ActivityScenario<SettingsActivity> activityScenario = ActivityScenario.launch(SettingsActivity.class)) {
            onView(withId(R.id.buttonExpandSymbologySelection)).perform(nestedScrollTo(), click());

            for (String symbology : includedSymbologies) {
                // Check if the symbology is in the list
                onView(withTag("checkbox_" + symbology)).check(matches(withCheckBoxText(symbology)));
            }
        }
    }

    @Test
    public void testSwitchSettings() {
        onView(withId(R.id.btSettings)).perform(click());
        SystemClock.sleep(200);
        try (ActivityScenario<SettingsActivity> activityScenario = ActivityScenario.launch(SettingsActivity.class)) {
            activityScenario.onActivity(activity -> {
                viewVisibleIdlingResource = new ViewVisibleIdlingResource(activity.findViewById(R.id.switchWaitDisconnected));
                IdlingRegistry.getInstance().register(viewVisibleIdlingResource);
            });

            // Portrait mode
            onView(withId(R.id.switchWaitDisconnected)).perform(scrollTo(), click());
            onView(withId(R.id.textViewWaitDisconnected)).check(matches(withText(R.string.wait_disconnected)));
            onView(withId(R.id.switchReturnOnlyFirst)).perform(scrollTo(), click());
            onView(withId(R.id.textViewReturnOnlyFirst)).check(matches(withText(R.string.return_only_first)));
            onView(withId(R.id.switchBluetooth)).perform(scrollTo(), click());
            onView(withId(R.id.textViewBluetooth)).perform(scrollTo()).check(matches(withText(R.string.use_bluetooth)));
            onView(withId(R.id.switchInitialSearch)).perform(scrollTo(), click());
            onView(withId(R.id.textViewInitialSearch)).check(matches(withText(R.string.allow_initial_search)));
            onView(withId(R.id.switchLaterConnections)).perform(scrollTo(), click());
            onView(withId(R.id.textViewLaterConnections)).check(matches(withText(R.string.allow_later_connections)));
            onView(withId(R.id.switchPairingFlow)).perform(scrollTo(), click());
            onView(withId(R.id.textViewPairingFlow)).check(matches(withText(R.string.allow_pairing_flow)));
            onView(withId(R.id.switchIntentDevices)).perform(scrollTo(), click());
            onView(withId(R.id.textViewIntentDevices)).check(matches(withText(R.string.allow_intent_devices)));
            onView(withId(R.id.switchEnableLogging)).perform(scrollTo(), click());
            onView(withId(R.id.textViewEnableLogging)).check(matches(withText(R.string.enable_log)));
            onView(withId(R.id.switchAllowCameraFallback)).perform(scrollTo(), click());
            onView(withId(R.id.textViewAllowCameraFallback)).check(matches(withText(R.string.allow_camera_fallback)));

            // Landscape mode
            UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
            uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

            activityScenario.onActivity(activity -> {
                viewVisibleIdlingResource = new ViewVisibleIdlingResource(activity.findViewById(R.id.switchWaitDisconnected));
                IdlingRegistry.getInstance().register(viewVisibleIdlingResource);
            });

            onView(withId(R.id.switchWaitDisconnected)).perform(scrollTo(), click());
            onView(withId(R.id.textViewWaitDisconnected)).check(matches(withText(R.string.wait_disconnected)));
            onView(withId(R.id.switchReturnOnlyFirst)).perform(scrollTo(), click());
            onView(withId(R.id.textViewReturnOnlyFirst)).check(matches(withText(R.string.return_only_first)));
            onView(withId(R.id.switchBluetooth)).perform(scrollTo(), click());
            onView(withId(R.id.textViewBluetooth)).perform(scrollTo()).check(matches(withText(R.string.use_bluetooth)));
            onView(withId(R.id.switchInitialSearch)).perform(scrollTo(), click());
            onView(withId(R.id.textViewInitialSearch)).check(matches(withText(R.string.allow_initial_search)));
            onView(withId(R.id.switchLaterConnections)).perform(scrollTo(), click());
            onView(withId(R.id.textViewLaterConnections)).check(matches(withText(R.string.allow_later_connections)));
            onView(withId(R.id.switchPairingFlow)).perform(scrollTo(), click());
            onView(withId(R.id.textViewPairingFlow)).check(matches(withText(R.string.allow_pairing_flow)));
            onView(withId(R.id.switchIntentDevices)).perform(scrollTo(), click());
            onView(withId(R.id.textViewIntentDevices)).check(matches(withText(R.string.allow_intent_devices)));
            onView(withId(R.id.switchEnableLogging)).perform(scrollTo(), click());
            onView(withId(R.id.textViewEnableLogging)).check(matches(withText(R.string.enable_log)));
            onView(withId(R.id.switchAllowCameraFallback)).perform(scrollTo(), click());
            onView(withId(R.id.textViewAllowCameraFallback)).check(matches(withText(R.string.allow_camera_fallback)));

            // Reset to portrait mode
            uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0);
        }

    }

    @Test
    public void testToggleCameraRatioModeSettings() {
        try (ActivityScenario<SettingsActivity> activityScenario = ActivityScenario.launch(SettingsActivity.class)) {
            activityScenario.onActivity(activity -> {
                activity.buttonSave.setVisibility(View.GONE);
                viewVisibleIdlingResource = new ViewVisibleIdlingResource(activity.findViewById(R.id.buttonFillCrop));
                IdlingRegistry.getInstance().register(viewVisibleIdlingResource);
            });

            // Portrait mode
            onView(withId(R.id.buttonFillCrop)).perform(nestedScrollTo(), click()).check(matches(withText(R.string.fill_crop)));
            activityScenario.onActivity(activity -> {
                assertEquals(activity.aspectRatioMode, 0);
            });

            onView(withId(R.id.buttonFillBlackBars)).perform(scrollTo()).perform(click()).check(matches(withText(R.string.fill_black_bars)));
            activityScenario.onActivity(activity -> {
                assertEquals(activity.aspectRatioMode, 1);
            });

            onView(withId(R.id.buttonFillStretch)).perform(scrollTo()).perform(click()).check(matches(withText(R.string.fill_stretch)));
            activityScenario.onActivity(activity -> {
                assertEquals(activity.aspectRatioMode, 2);
            });

            // Landscape mode
            UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
            uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

            activityScenario.onActivity(activity -> {
                viewVisibleIdlingResource = new ViewVisibleIdlingResource(activity.findViewById(R.id.buttonFillCrop));
                IdlingRegistry.getInstance().register(viewVisibleIdlingResource);
                activity.buttonSave.setVisibility(View.GONE);
            });

            onView(withId(R.id.buttonFillCrop)).perform(nestedScrollTo(), click());
            onView(withId(R.id.buttonFillCrop)).check(matches(withText(R.string.fill_crop)));
            activityScenario.onActivity(activity -> {
                assertEquals(activity.aspectRatioMode, 0);
            });

            onView(withId(R.id.buttonFillBlackBars)).perform(nestedScrollTo(), click());
            onView(withId(R.id.buttonFillBlackBars)).check(matches(withText(R.string.fill_black_bars)));
            activityScenario.onActivity(activity -> {
                assertEquals(activity.aspectRatioMode, 1);
            });

            onView(withId(R.id.buttonFillStretch)).perform(nestedScrollTo(), click());
            onView(withId(R.id.buttonFillStretch)).check(matches(withText(R.string.fill_stretch)));
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

        onView(withId(R.id.btScanner)).perform(click());

        // Check if main activity is launched
        intended(hasComponent(MainActivity.class.getName()));

        // Stop recording intent
        Intents.release();
    }
}
