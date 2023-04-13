package com.enioka.scanner.camera;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

/**
 * Helper view that encapsulates the ZBar (default) and ZXing (option) barcode analysis engines.
 * To be directly reused in layouts.
 * This view either uses Camera 1 or Camera 2 API according to the Android version.
 */
public class CameraBarcodeScanView extends FrameLayout {
    protected final static String LOG_TAG = "ScannerActivity";

    private final AttributeSet attrs;
    private final CameraApiLevel api;
    private CameraBarcodeScanViewV1 api1View;
    private CameraBarcodeScanViewV2 api2View;
    private CameraBarcodeScanViewBase proxiedView;

    public CameraBarcodeScanView(@NonNull Context context) {
        super(context);
        api = guessBestApiLevel();
        attrs = null;

        setLayout();
    }

    public CameraBarcodeScanView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        api = guessBestApiLevel();
        this.attrs = attrs;

        setLayout();
    }

    private CameraApiLevel guessBestApiLevel() {
        CameraApiLevel res = CameraApiLevel.Camera1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            res = CameraApiLevel.Camera2;
        }

        return res;
    }

    private void setLayout() {
        switch (api) {
            case Camera1:
                proxiedView = new CameraBarcodeScanViewV1(getContext(), attrs);
                break;
            case Camera2:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    proxiedView = new CameraBarcodeScanViewV2(getContext(), attrs);
                }
                break;
        }
        Log.i(LOG_TAG, "Using camera version: " + proxiedView.getClass().getName());
        this.addView(proxiedView);
    }

    public CameraBarcodeScanViewBase getProxiedView() {
        return proxiedView;
    }
}
