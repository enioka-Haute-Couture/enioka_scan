package com.enioka.scanner.sdk.hid;

import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;

import java.util.ArrayList;
import java.util.Collections;

public class GenericHidScanner implements Scanner {
    private final static String LOG_TAG = "GenericHidScanner";

    private String keyboardInput = "";
    private boolean paused = false;
    private ScannerDataCallback dataCb;

    @Override
    public void initialize(Activity ctx, ScannerInitCallback cb0, final ScannerDataCallback cb1, ScannerStatusCallback cb2, Mode mode) {
        this.dataCb = cb1;

        // Register an OnKeyListener onto the Activity root view, if any.
        View rootParentView = ctx.findViewById(android.R.id.content);

        if (rootParentView == null) {
            cb0.onConnectionFailure(this);
            Log.w(LOG_TAG, "Tried to listen to a HID but no view is available - only views allow to listen to key presses");
        }

        if (rootParentView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) rootParentView;
            if (viewGroup.getChildCount() == 1) {
                // This happens when the layout has been set through setContentView (parent is likely android.support.v7.widget.ContentFrameLayout in that case)
                rootParentView = viewGroup.getChildAt(0);
            }
        }

        rootParentView.setFocusable(true); // a precaution, as events may not be passed down if there is nothing focusable inside the view.
        rootParentView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (paused) {
                    return false;
                }

                if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    // The ending CR is most often a simple UP without DOWN.
                    Barcode b = new Barcode(GenericHidScanner.this.keyboardInput, BarcodeType.UNKNOWN);
                    GenericHidScanner.this.dataCb.onData(null, new ArrayList<>(Collections.singleton(b)));
                    GenericHidScanner.this.keyboardInput = "";
                } else if (!event.isPrintingKey()) {
                    // Skip un-printable characters.
                } else if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    // Only use DOWN event - UP events are not synchronized with SHIFT events.
                    GenericHidScanner.this.keyboardInput += (char) event.getKeyCharacterMap().get(event.getKeyCode(), event.getMetaState());
                    Log.i(LOG_TAG, GenericHidScanner.this.keyboardInput);
                }
                return true;
            }
        });

        Log.i(LOG_TAG, "HID scanner initialized");
        cb0.onConnectionSuccessful(this);
    }

    @Override
    public void setDataCallBack(ScannerDataCallback cb) {
        this.dataCb = cb;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void beepScanSuccessful() {

    }

    @Override
    public void beepScanFailure() {

    }

    @Override
    public void beepPairingCompleted() {

    }

    @Override
    public void enableIllumination() {

    }

    @Override
    public void disableIllumination() {

    }

    @Override
    public void toggleIllumination() {

    }

    @Override
    public boolean isIlluminationOn() {
        return false;
    }

    @Override
    public boolean supportsIllumination() {
        // We consider that most scanner HID beep by themselves and illuminate by themselves.
        return false;
    }

    @Override
    public String getProviderKey() {
        return GenericHidProvider.LOG_TAG;
    }
}
