package com.enioka.scanner.demo;

import android.view.View;

import androidx.test.espresso.IdlingResource;

public class ViewVisibleIdlingResource implements IdlingResource {
    private final View view;
    private ResourceCallback callback;

    public ViewVisibleIdlingResource(View view) {
        this.view = view;
    }

    @Override
    public String getName() {
        return "Waiting for view " + view.getId() + " to be visible";
    }

    @Override
    public boolean isIdleNow() {
        // Check if this view is displayed
        boolean viewDisplayed = this.view.getVisibility() == View.VISIBLE;

        if (viewDisplayed && callback != null) {
            callback.onTransitionToIdle();
        }

        return viewDisplayed;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.callback = callback;
    }
}
