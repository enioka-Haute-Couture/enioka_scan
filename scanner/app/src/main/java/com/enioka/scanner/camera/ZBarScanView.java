package com.enioka.scanner.camera;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Please use {@link CameraBarcodeScanView} instead which is identical. Only here for compatibility.
 */
@Deprecated
public class ZBarScanView extends CameraBarcodeScanView {
    public ZBarScanView(Context context) {
        super(context);
    }

    public ZBarScanView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }
}
