package com.enioka.scanner.activities;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.enioka.scanner.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ManualLogDialog {
    void launchDialog(Context context, String title, String logs, String textButton) {
        Drawable icon = ContextCompat.getDrawable(context, R.drawable.info);

        if (icon != null) {
            // set color of icon to black api 18
            icon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_primary), PorterDuff.Mode.SRC_IN));
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setIcon(icon)
                .setMessage(logs)
                .setNegativeButton(textButton, (dialog, which) -> dialog.dismiss())
                .show();
    }
}
