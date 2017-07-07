package com.enioka.scanner.helpers;

import com.enioka.scanner.camera.ZbarScanView;

/**
 * A set of helpers for scanner SDK providers.
 */
public final class Common {
    /**
     * Short high beep to indicate successful scan
     */
    public static void beepScanSuccessful() {
        ZbarScanView.beepOk();
    }

    /**
     * Long low beep to indicate unsuccessful scan
     */
    public static void beepScanFailure() {
        ZbarScanView.beepKo();
    }

    /**
     * Different beep to indicate a completed barcode pairing
     */
    public static void beepPairingCompleted() {
        ZbarScanView.beepWaiting();
    }
}
