package com.enioka.scanner.camera;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Bag for all resolution properties.
 */
class Resolution {
    private static final String PREFERENCE_FORBIDDEN_REZ = "forbidden_resolutions";

    Resolution(Context context) {
        this.context = context;
    }

    private Context context;

    List<Point> supportedPreviewResolutions = new ArrayList<>(20);
    List<Point> allowedPreviewResolutions = new ArrayList<>(20);

    List<Point> supportedPhotoResolutions = new ArrayList<>(20);

    Point currentPreviewResolution = null;
    Point preferredPreviewResolution = null;
    Point currentPhotoResolution = null;

    boolean usePreviewForPhoto = false;
    boolean useAdaptiveResolution = true;

    void persistDefaultPreviewResolution(Point resolution) {
        ViewHelpersPreferences.persistDefaultPreviewResolution(context, resolution);
    }

    void removeResolution(Point rez) {
        Point s = null;
        for (Point cs : allowedPreviewResolutions) {
            if (cs.y == rez.y && cs.x == rez.x) {
                s = cs;
                break;
            }
        }
        if (s != null) {
            allowedPreviewResolutions.remove(s);
            Set<String> forbiddenRezs = ViewHelpersPreferences.getPreferencesStringSet(context, PREFERENCE_FORBIDDEN_REZ);
            if (forbiddenRezs == null) {
                forbiddenRezs = new HashSet<>(1);
            }
            forbiddenRezs.add(rez.x + "*" + rez.y);
            ViewHelpersPreferences.storePreferences(context, PREFERENCE_FORBIDDEN_REZ, forbiddenRezs);
            Log.i("BARCODE", "Resolution " + rez + " is removed from possible preview resolutions");
        }
    }
}
