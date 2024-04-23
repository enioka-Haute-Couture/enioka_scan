package com.enioka.scanner.sdk.zebra.dw;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

class ZebraDwCommand {
    private static final String LOG_TAG = "ZebraDwScanner";

    private String id = UUID.randomUUID().toString().replace("-", "").toUpperCase();

    private final Intent intent = new Intent();

    private Callback callback = null;

    static ZebraDwCommand create() {
        return new ZebraDwCommand();
    }

    @FunctionalInterface
    interface Callback {
        void onSuccess(Intent intent);

        default void onFailure(Intent intent) {
            // Do nothing but log by default
            if (intent.hasExtra("RESULT_INFO")) {
                Bundle bundle = intent.getBundleExtra("RESULT_INFO");
                Set<String> keys = bundle != null ? bundle.keySet() : new HashSet<>();
                StringBuilder sb = new StringBuilder();
                for (String key : keys) {
                    sb.append(key);
                    sb.append(" = ");
                    sb.append(bundle.get(key));
                    sb.append("\n");
                }
                sb.append(intent.toUri(0));
                Log.e(LOG_TAG, "Zebra DataWedge command failure: " + sb);
            } else {
                Log.e(LOG_TAG, "Zebra DataWedge command failure: " + intent);
            }
        }
    }

    static ZebraDwCommand create(String actionName, String actionArgument) {
        return new ZebraDwCommand().putExtra(actionName, actionArgument);
    }

    private ZebraDwCommand() {
    }

    /**
     * Only use when the command does not respect COMMAND_IDENTIFIER.
     */
    public ZebraDwCommand setExpectedResultKey(String key) {
        this.id = key;
        return this;
    }

    public ZebraDwCommand putExtra(String key, String value) {
        intent.putExtra(key, value);
        return this;
    }

    public ZebraDwCommand putExtra(String key, Bundle value) {
        intent.putExtra(key, value);
        return this;
    }

    ZebraDwCommand setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public Callback getCallback() {
        return callback;
    }

    public Intent getIntentToSend() {
        intent.setAction(ZebraDwIntents.DW_API_MAIN_ACTION);

        intent.putExtra(ZebraDwIntents.DW_API_CMD_ID_EXTRA, this.id);
        intent.putExtra("SEND_RESULT", "true"); // equivalent to LAST_RESULT in recent DW.

        return intent;
    }

    public String getId() {
        return id;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Bundle extras = getIntentToSend().getExtras();
        sb.append("DW command ID: ");
        sb.append(this.id);
        sb.append("\tIntent action: ");
        sb.append(this.getIntentToSend().getAction());
        sb.append("\n");
        if (extras != null && !extras.isEmpty()) {

            for (String key : extras.keySet()) {
                Object value = extras.get(key);

                sb.append("\t");
                sb.append(key);
                sb.append("=");
                sb.append(value != null ? value.toString() : "");
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
