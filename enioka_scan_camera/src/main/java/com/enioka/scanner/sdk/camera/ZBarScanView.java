package com.enioka.scanner.sdk.camera;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Please use {@link CameraBarcodeScanViewV1} instead which is identical. Only here for compatibility.
 */
@Deprecated
public class ZBarScanView extends CameraBarcodeScanViewV1 {
    public ZBarScanView(Context context) {
        super(context);
    }

    public ZBarScanView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }
}
