package com.enioka.scanner.activities;

import android.content.Context;
import android.view.View;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.enioka.scanner.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ManualPairingBarcodeDialog {
    static void launchDialog(Context context, View v) {
        Drawable icon = ContextCompat.getDrawable(context, R.drawable.info);

        if (icon != null) {
            // set color of icon to black api 18
            icon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_primary), PorterDuff.Mode.SRC_IN));
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle("Bluetooth pairing barcode")
                .setIcon(icon)
                .setView(v)
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
