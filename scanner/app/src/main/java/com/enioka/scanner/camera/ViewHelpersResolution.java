package com.enioka.scanner.camera;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.SurfaceView;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Logic for resolution choices.
 */
class ViewHelpersResolution {
    private static final String TAG = "BARCODE";

    static void setPreviewResolution(Context context, Resolution bag, SurfaceView camView) {
        Point previewResolution = null;

        // Get memory limit - we do not want a resolution so high as to cause an OOM with all the buffers
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int maxMb = 16; // default used to be 16MB - should work on even very old devices
        if (activityManager != null) {
            maxMb = activityManager.getMemoryClass();
        }
        Log.i(TAG, "Using memory limit (MB): " + maxMb);

        // Look for a resolution not too far from the view ratio.
        float preferredRatio = (float) camView.getMeasuredHeight() / (float) camView.getMeasuredWidth();
        if (preferredRatio < 1) {
            preferredRatio = 1 / preferredRatio;
        }
        Log.i(TAG, "Looking for the ideal preview resolution. View ratio is " + preferredRatio);
        boolean goodMatchFound = false;

        Set<String> forbiddenRezs = ViewHelpersPreferences.getPreferencesStringSet(context, "rezs_too_high");
        if (forbiddenRezs == null) {
            forbiddenRezs = new HashSet<>(0);
        }

        // First simply list resolutions (debug display & sorted res list creation)
        for (Point resolution : bag.supportedPreviewResolutions) {
            Log.d(TAG, "\tsupports preview resolution " + resolution.x + "*" + resolution.y + " - " + ((float) resolution.x / (float) resolution.y));

            if (Math.abs((float) resolution.x / (float) resolution.y - preferredRatio) < 0.3f) {
                if (forbiddenRezs.contains(resolution.x + "*" + resolution.y)) {
                    Log.d(TAG, "\t\tResolution is forbidden - FPS too low");
                    continue;
                }
                int previewBufferSize = (int) (resolution.x * resolution.y * bag.bytesPerPixel);
                if (previewBufferSize * Runtime.getRuntime().availableProcessors() * 2 / 1024 / 1024 > (maxMb * 0.75)) {
                    Log.d(TAG, "\t\tResolution is forbidden - too much memory would be used");
                    continue;
                }

                bag.allowedPreviewResolutions.add(resolution);
            }
        }
        Collections.sort(bag.allowedPreviewResolutions, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return o1.x < o2.x ? -1 : o1.x == o2.x ? 0 : 1;
            }
        });
        Log.v(TAG, "Allowed preview sizes (acceptable ratio): ");
        for (Point resolution : bag.allowedPreviewResolutions) {
            Log.v(TAG, "\t" + resolution.x + "*" + resolution.y + " - " + ((float) resolution.x / (float) resolution.y));
        }

        // Select the best resolution.
        // First try with only the preferred ratio.
        for (Point resolution : bag.allowedPreviewResolutions) {
            if (previewResolution == null || (resolution.x > previewResolution.x) && Math.abs((float) resolution.x / (float) resolution.y - preferredRatio) < 0.1f) {
                previewResolution = resolution;
                goodMatchFound = true;
            }
        }

        // If not found, try with any ratio.
        if (!goodMatchFound) {
            for (Point resolution : bag.allowedPreviewResolutions) {
                if (resolution.x <= 1980 && resolution.y <= 1080 && (previewResolution == null || (resolution.x > previewResolution.x))) {
                    previewResolution = resolution;
                }
            }
        }

        if (previewResolution == null) {
            throw new RuntimeException("no suitable preview resolution");
        }

        // Finally, if there is a preferred preview size, just use it.
        Point preferred = ViewHelpersPreferences.getDefaultPreviewResolution(context);
        if (preferred != null && preferred.x > 0 && preferred.y > 0) {
            for (Point resolution : bag.allowedPreviewResolutions) {
                if (preferred.x == resolution.x && preferred.y == resolution.y) {
                    previewResolution = resolution;
                    Log.i(TAG, "Loaded preferred preview resolution " + preferred);
                    break;
                }
            }
        }

        // We now have a preview resolution for sure.
        bag.currentPreviewResolution = previewResolution;
        bag.preferredPreviewResolution = ViewHelpersPreferences.getDefaultPreviewResolution(context);
    }


    static void setPictureResolution(Resolution bag) {
        // A preview resolution is often a picture resolution, so start with this.
        // Then, look for any higher resolution with the same ratio

        // Note the ratio looked for is the preview ratio, not the view ratio as it may be different.
        float preferredRatio = (float) bag.currentPreviewResolution.x / (float) bag.currentPreviewResolution.y;
        Log.i(TAG, "Looking for the ideal photo resolution. View ratio is " + preferredRatio);

        Point pictureSize = null;
        Point smallestResolution = bag.supportedPhotoResolutions.get(0);
        Point betterChoiceWrongRatio = null;
        boolean foundWithGoodRatio = false;

        for (Point resolution : bag.supportedPhotoResolutions) {
            // Is preview resolution a picture resolution?
            if (resolution.x == bag.currentPreviewResolution.x && resolution.y == bag.currentPreviewResolution.y) {
                pictureSize = resolution;
                foundWithGoodRatio = true;
            }
            if (resolution.x < smallestResolution.x) {
                smallestResolution = resolution;
            }
        }
        if (pictureSize == null) {
            pictureSize = smallestResolution;
        }

        for (Point resolution : bag.supportedPhotoResolutions) {
            Log.d(TAG, "\tsupports picture resolution " + resolution.x + "*" + resolution.y + " - " + ((float) resolution.x / (float) resolution.y));
            if (Math.abs((float) resolution.x / (float) resolution.y - preferredRatio) < 0.1f && resolution.x > pictureSize.x && resolution.x <= 2560 && resolution.y <= 1536) {
                pictureSize = resolution;
                foundWithGoodRatio = true;
            }
            if (resolution.x > pictureSize.x && resolution.x <= 2560 && resolution.y <= 1536) {
                betterChoiceWrongRatio = resolution;
            }
        }
        if (!foundWithGoodRatio) {
            Log.d(TAG, "Could not find a photo resolution with requested ratio " + preferredRatio + ". Going with wrong ratio resolution");
            pictureSize = betterChoiceWrongRatio;
        }
        if (pictureSize == null) {
            throw new RuntimeException("no suitable photo resolution");
        }

        bag.currentPhotoResolution = pictureSize;
        float camResRatio = ((float) bag.currentPhotoResolution.x) / bag.currentPhotoResolution.y;
        Log.i(TAG, "Using picture resolution " + pictureSize.x + "*" + pictureSize.y + ". Ratio is " + camResRatio + ". (Preferred ratio was " + preferredRatio + ")");
    }

}
