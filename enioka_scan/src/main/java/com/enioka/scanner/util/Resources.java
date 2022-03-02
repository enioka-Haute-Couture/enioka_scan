package com.enioka.scanner.util;

import android.app.Application;

/**
 * Tool class used only to help retrieve application resources from classes that do not contain an application context.
 * FIXME - 2022/03/02: Apparently attribute stays null.
 */
public class Resources extends Application {
    private static android.content.res.Resources resources;

    @Override
    public void onCreate() {
        super.onCreate();

        resources = getResources();
    }

    public static android.content.res.Resources getAppResources() {
        assert (resources != null);
        return resources;
    }
}
