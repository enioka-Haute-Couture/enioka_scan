package com.enioka.scanner.activities;

import androidx.test.espresso.idling.CountingIdlingResource;

import com.google.android.material.snackbar.Snackbar;

public class SnackbarResource {
    public static CountingIdlingResource countingIdlingResource = new CountingIdlingResource("Snackbar");

    public static void increment() {
        countingIdlingResource.increment();
    }

    public static void decrement() {
        countingIdlingResource.decrement();
    }

    public static Snackbar.Callback getSnackbarCallback() {
        return new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) {
                decrement();
            }
        };
    }
}
