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
    private static final String TAG = "BARCODE";
    private static final String PREFERENCE_FORBIDDEN_REZ = "forbidden_resolutions";

    Resolution(Context context) {
        this.context = context;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Preview
    List<Point> supportedPreviewResolutions = new ArrayList<>(20);
    List<Point> allowedPreviewResolutions = new ArrayList<>(20);

    Point currentPreviewResolution = null;
    Point preferredPreviewResolution = null;

    ////////////////////////////////////////////////////////////////////////////
    // Photo
    List<Point> supportedPhotoResolutions = new ArrayList<>(20);
    Point currentPhotoResolution = null;
    boolean usePreviewForPhoto = false;

    ////////////////////////////////////////////////////////////////////////////
    // Adaptive resolution parameters
    boolean useAdaptiveResolution = true;
    boolean storePreferredResolution = true;
    int maxResolutionY;
    float maxDistortionRatio;

    ////////////////////////////////////////////////////////////////////////////
    // Misc
    float bytesPerPixel;
    private final Context context;

    void persistDefaultPreviewResolution(Point resolution) {
        if (!storePreferredResolution) {
            return;
        }
        Log.i(TAG, "Persisting default preview resolution: " + resolution.x + "*" + resolution.y);
        ViewHelpersPreferences.persistDefaultPreviewResolution(context, resolution);
    }

    Point getDefaultPreviewResolution() {
        if (!storePreferredResolution) {
            return null;
        }
        return ViewHelpersPreferences.getDefaultPreviewResolution(context);
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
            Log.i(TAG, "Resolution " + rez + " is removed from possible preview resolutions");
        }
    }

    public Point getNextPreviewResolution(boolean low) {
        if (!useAdaptiveResolution) {
            return null;
        }

        // Current resolution index?
        int currentResolutionIndex = -1;
        int i = -1;
        for (Point allowedResolution : allowedPreviewResolutions) {
            i++;
            if (allowedResolution.x == currentPreviewResolution.x && allowedResolution.y == currentPreviewResolution.y) {
                currentResolutionIndex = i;
                break;
            }
        }

        // Checks
        if (currentResolutionIndex == -1) {
            // Happens when the chosen resolution does not have the correct ratio.
            Log.d(TAG, "Out of bounds FPS but no suitable alternative resolution available");
            return null;
        }
        int indexShift;
        if (low) {
            if (currentResolutionIndex == 0) {
                // We already use the lowest resolution possible
                Log.d(TAG, "Low analysis FPS but already on the lowest possible resolution");
                return null;
            }
            indexShift = -1;
        } else {
            if (currentResolutionIndex == allowedPreviewResolutions.size() - 1) {
                // We already use the lowest resolution possible
                Log.d(TAG, "High analysis FPS but already on the highest possible resolution");
                return null;
            }
            indexShift = 1;
        }

        // We have a correct new preview resolution!
        Point newRez = allowedPreviewResolutions.get(currentResolutionIndex + indexShift);
        Log.i(TAG, "Changing preview resolution from " + currentPreviewResolution.x + "*" + currentPreviewResolution.y +
                " to " + newRez.x + "*" + newRez.y);

        // Set it
        return newRez;
    }
}
