package com.enioka.scanner.activities;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.enioka.scanner.R;

import java.util.HashMap;

/**
 * An helper class to manage the dynamic switch of UI between portrait and landscape mode, for the
 * camera and the laser activity.
 */
public class ViewSwitcher {
    protected final static String LOG_TAG = "ScannerActivity";
    public static void switchCameraOrientation(Context context, View view, HashMap<String, Integer> cameraResources, boolean portrait) {
        Integer constraintLayoutId = cameraResources.get("constraint_layout_id");
        Integer scannerFlashlightId = cameraResources.get("scanner_flashlight_id");
        Integer scannerBtKeyboardId = cameraResources.get("scanner_bt_provider_logs");
        int openLinkBtId = view.getResources().getIdentifier("open_link", "id", context.getPackageName());

        if (constraintLayoutId == null || scannerFlashlightId == null || scannerBtKeyboardId == null) {
            Log.w(LOG_TAG, "Cannot switch to landscape mode: missing resources");
            return;
        }

        ConstraintLayout constraintLayout = view.findViewById(constraintLayoutId);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        int margin = context.getResources().getDimensionPixelSize(R.dimen.layout_margin_border);

        // Set left constraints
        if (portrait) {
            constraintSet.connect(scannerFlashlightId, ConstraintSet.RIGHT, constraintLayout.getId(), ConstraintSet.RIGHT, margin);
            constraintSet.connect(scannerBtKeyboardId, ConstraintSet.RIGHT, constraintLayout.getId(), ConstraintSet.RIGHT, margin);
            constraintSet.connect(openLinkBtId, ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT, margin);

            constraintSet.clear(scannerFlashlightId, ConstraintSet.LEFT);
            constraintSet.clear(scannerBtKeyboardId, ConstraintSet.LEFT);
            constraintSet.clear(openLinkBtId, ConstraintSet.RIGHT);
        } else {
            constraintSet.connect(scannerFlashlightId, ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT, margin);
            constraintSet.connect(scannerBtKeyboardId, ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT, margin);
            constraintSet.connect(openLinkBtId, ConstraintSet.RIGHT, constraintLayout.getId(), ConstraintSet.RIGHT, margin);

            constraintSet.clear(scannerFlashlightId, ConstraintSet.RIGHT);
            constraintSet.clear(scannerBtKeyboardId, ConstraintSet.RIGHT);
            constraintSet.clear(openLinkBtId, ConstraintSet.LEFT);
        }

        // Apply constraints
        constraintSet.applyTo(constraintLayout);
    }

    public static void switchLaserOrientation(Context context, View view, int flashlightViewId, boolean portrait) {
        ConstraintLayout mainConstraintLayout = view.findViewById(R.id.constraint_layout);
        LinearLayout linearLayout = view.findViewById(R.id.bottom_layout);
        View scannerFlashlight = view.findViewById(flashlightViewId);
        View scannerBell = view.findViewById(R.id.scanner_bell);
        View scannerRedLed = view.findViewById(R.id.scanner_red_led);
        View buttonLogProvider = view.findViewById(R.id.scanner_bt_provider_logs);

        if (portrait) {
            // Move elements to main constraint layout
            linearLayout.removeView(scannerFlashlight);
            linearLayout.removeView(scannerBell);
            linearLayout.removeView(scannerRedLed);

            LinearLayout.LayoutParams oldParamsFlashLight = (LinearLayout.LayoutParams) scannerFlashlight.getLayoutParams();
            LinearLayout.LayoutParams oldParamsBell = (LinearLayout.LayoutParams) scannerBell.getLayoutParams();
            LinearLayout.LayoutParams oldParamsRedLed = (LinearLayout.LayoutParams) scannerRedLed.getLayoutParams();

            ConstraintLayout.LayoutParams newParamsFlashLight = new ConstraintLayout.LayoutParams(oldParamsFlashLight.width, oldParamsFlashLight.height);
            newParamsFlashLight.setMargins(oldParamsFlashLight.leftMargin, oldParamsFlashLight.topMargin, oldParamsFlashLight.rightMargin, oldParamsFlashLight.bottomMargin);

            ConstraintLayout.LayoutParams newParamsBell = new ConstraintLayout.LayoutParams(oldParamsBell.width, oldParamsBell.height);
            newParamsBell.setMargins(oldParamsBell.leftMargin, oldParamsBell.topMargin, oldParamsBell.rightMargin, oldParamsBell.bottomMargin);

            ConstraintLayout.LayoutParams newParamsRedLed = new ConstraintLayout.LayoutParams(oldParamsRedLed.width, oldParamsRedLed.height);
            newParamsRedLed.setMargins(oldParamsRedLed.leftMargin, oldParamsRedLed.topMargin, oldParamsRedLed.rightMargin, oldParamsRedLed.bottomMargin);

            mainConstraintLayout.addView(scannerFlashlight, newParamsFlashLight);
            mainConstraintLayout.addView(scannerBell, newParamsBell);
            mainConstraintLayout.addView(scannerRedLed, newParamsRedLed);

            // Apply constraints
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(mainConstraintLayout);

            int margin = context.getResources().getDimensionPixelSize(R.dimen.layout_margin_border);

            constraintSet.connect(scannerFlashlight.getId(), ConstraintSet.TOP, R.id.scanner_enable_text, ConstraintSet.BOTTOM, margin);
            constraintSet.connect(scannerFlashlight.getId(), ConstraintSet.END, mainConstraintLayout.getId(), ConstraintSet.END, margin);

            constraintSet.connect(scannerBell.getId(), ConstraintSet.TOP, scannerFlashlight.getId(), ConstraintSet.BOTTOM, margin);
            constraintSet.connect(scannerBell.getId(), ConstraintSet.END, mainConstraintLayout.getId(), ConstraintSet.END, margin);

            constraintSet.connect(scannerRedLed.getId(), ConstraintSet.TOP, scannerBell.getId(), ConstraintSet.BOTTOM, margin);
            constraintSet.connect(scannerRedLed.getId(), ConstraintSet.END, mainConstraintLayout.getId(), ConstraintSet.END, margin);

            constraintSet.applyTo(mainConstraintLayout);

            // Apply constraints on show log button
            LinearLayout.LayoutParams paramsLogButton = (LinearLayout.LayoutParams) buttonLogProvider.getLayoutParams();
            paramsLogButton.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            paramsLogButton.weight = 0;

            buttonLogProvider.setLayoutParams(paramsLogButton);
        } else {
            // Move elements to linear layout
            mainConstraintLayout.removeView(scannerFlashlight);
            mainConstraintLayout.removeView(scannerBell);
            mainConstraintLayout.removeView(scannerRedLed);

            ConstraintLayout.LayoutParams oldParamsFlashLight = (ConstraintLayout.LayoutParams) scannerFlashlight.getLayoutParams();
            ConstraintLayout.LayoutParams oldParamsBell = (ConstraintLayout.LayoutParams) scannerBell.getLayoutParams();
            ConstraintLayout.LayoutParams oldParamsRedLed = (ConstraintLayout.LayoutParams) scannerRedLed.getLayoutParams();

            LinearLayout.LayoutParams newParamsFlashLight = new LinearLayout.LayoutParams(oldParamsFlashLight.width, oldParamsFlashLight.height);
            newParamsFlashLight.setMargins(0, oldParamsFlashLight.topMargin, oldParamsFlashLight.rightMargin, oldParamsFlashLight.bottomMargin);

            LinearLayout.LayoutParams newParamsBell = new LinearLayout.LayoutParams(oldParamsBell.width, oldParamsBell.height);
            newParamsBell.setMargins(0, oldParamsBell.topMargin, oldParamsBell.rightMargin, oldParamsBell.bottomMargin);

            LinearLayout.LayoutParams newParamsRedLed = new LinearLayout.LayoutParams(oldParamsRedLed.width, oldParamsRedLed.height);
            newParamsRedLed.setMargins(0, oldParamsRedLed.topMargin, oldParamsRedLed.rightMargin, oldParamsRedLed.bottomMargin);

            linearLayout.addView(scannerFlashlight, newParamsFlashLight);
            linearLayout.addView(scannerBell, newParamsBell);
            linearLayout.addView(scannerRedLed, newParamsRedLed);

            // Apply constraints on show log button
            LinearLayout.LayoutParams paramsLogButton = (LinearLayout.LayoutParams) buttonLogProvider.getLayoutParams();
            paramsLogButton.width = 0;
            paramsLogButton.weight = 1;

            buttonLogProvider.setLayoutParams(paramsLogButton);
        }
    }
}
