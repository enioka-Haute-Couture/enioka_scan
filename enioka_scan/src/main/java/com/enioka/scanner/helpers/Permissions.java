package com.enioka.scanner.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * A set of helpers for scanner SDK providers.
 */
public final class Permissions {

    /**
     * Camera-related permissions
     */
    public static final String[] PERMISSIONS_CAMERA = new String[]{
            Manifest.permission.CAMERA,
    };

    /**
     * Bluetooth-related permissions
     */
    public static final String[] PERMISSIONS_BT = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? Manifest.permission.BLUETOOTH_CONNECT : Manifest.permission.BLUETOOTH_ADMIN,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? Manifest.permission.BLUETOOTH_SCAN : Manifest.permission.BLUETOOTH_ADMIN,
            Build.VERSION.SDK_INT > Build.VERSION_CODES.P ? Manifest.permission.ACCESS_FINE_LOCATION : Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    /**
     * Internet-related permissions
     */
    public static final String[] PERMISSIONS_INTERNET = new String[]{
            Manifest.permission.INTERNET,
    };

    /**
     * Request a set of permissions if they are not already granted.
     * @param ctx The calling activity
     * @param permissions The list of permissions to request if any is missing
     * @param requestId An identifier for the request
     */
    public static void requestPermissionSet(Activity ctx, String[] permissions, int requestId) {
        if (!hasPermissionSet(ctx, permissions)) {
            ActivityCompat.requestPermissions(ctx, permissions, requestId);
        }
    }

    /**
     * Checks whether a set of permissions is granted.
     * @param ctx The calling context
     * @param permissions The list of permissions to check
     * @return True if all permissions are granted, false if at least one is not granted
     */
    public static boolean hasPermissionSet(Context ctx, String[] permissions) {
        for (final String permission : permissions) {
            if (ContextCompat.checkSelfPermission(ctx, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
