package com.enioka.scanner.demo;

import androidx.test.espresso.idling.CountingIdlingResource;

public class EspressoSemaphore {
    public static CountingIdlingResource countingIdlingResource = new CountingIdlingResource("settings");

    public static void increment() {
        countingIdlingResource.increment();
    }

    public static void decrement() {
        countingIdlingResource.decrement();
    }
}
