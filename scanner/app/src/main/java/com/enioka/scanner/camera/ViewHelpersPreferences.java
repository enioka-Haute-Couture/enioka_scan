package com.enioka.scanner.camera;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.util.Log;

import java.util.Set;

/**
 * A few methods helping to deal with views (factorization to help Camera1/2 split).
 */
class ViewHelpersPreferences {
    private static final String TAG = "ViewHelpersPreferences";

    private static final String PREFERENCE_PREFERRED_PREVIEW_REZ_X = "preferred_preview_rez_x";
    private static final String PREFERENCE_PREFERRED_PREVIEW_REZ_Y = "preferred_preview_rez_y";

    /**
     * Fetch the activity containing a context (often a view) by going up to parents. Null if not found, not an activity, etc.
     *
     * @param context the context to find containing activity of.
     * @return null if nt found
     */
    static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    static Set<String> getPreferencesStringSet(Context context, String key) {
        Activity a = getActivity(context);
        if (a == null) {
            return null;
        }

        try {
            SharedPreferences p = a.getPreferences(Context.MODE_PRIVATE);
            return p.getStringSet(key, null);
        } catch (Exception e) {
            Log.w(TAG, "Could not retrieve preferences");
            return null;
        }
    }

    static int getPreferenceInt(Context context, String key) {
        Activity a = getActivity(context);
        if (a == null) {
            return -1;
        }

        try {
            SharedPreferences p = a.getPreferences(Context.MODE_PRIVATE);
            return p.getInt(key, -1);
        } catch (Exception e) {
            Log.w(TAG, "Could not retrieve preferences");
            return -1;
        }
    }

    static Point getDefaultPreviewResolution(Context context) {
        try {
            int x = ViewHelpersPreferences.getPreferenceInt(context, PREFERENCE_PREFERRED_PREVIEW_REZ_X);
            int y = ViewHelpersPreferences.getPreferenceInt(context, PREFERENCE_PREFERRED_PREVIEW_REZ_Y);
            if (x > 0 && y > 0) {
                return new Point(x, y);
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not retrieve preferences");
            return null;
        }
    }

    static void storePreferences(Context context, String key, int value) {
        Activity a = getActivity(context);
        if (a == null) {
            return;
        }

        SharedPreferences p = a.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor e = p.edit();
        e.putInt(key, value);
        e.apply();
    }

    static void storePreferences(Context context, String key, Set<String> values) {
        Activity a = getActivity(context);
        if (a == null) {
            return;
        }

        SharedPreferences p = a.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor e = p.edit();
        e.putStringSet(key, values);
        e.apply();
    }

    static void persistDefaultPreviewResolution(Context context, Point p) {
        ViewHelpersPreferences.storePreferences(context, PREFERENCE_PREFERRED_PREVIEW_REZ_X, p.x);
        ViewHelpersPreferences.storePreferences(context, PREFERENCE_PREFERRED_PREVIEW_REZ_Y, p.y);
    }
}
