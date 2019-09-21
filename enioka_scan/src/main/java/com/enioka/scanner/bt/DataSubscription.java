package com.enioka.scanner.bt;

import java.util.Calendar;

/**
 * Helper class storing execution context around a command callback.
 */
class DataSubscription {
    private final CommandCallbackHolder<?> callback;
    private Calendar timeOut = null;

    DataSubscription(CommandCallbackHolder callback) {
        this.callback = callback;

        if (callback.getTimeOutMs() > 0) {
            timeOut = Calendar.getInstance();
            timeOut.add(Calendar.MILLISECOND, callback.getTimeOutMs());
        }
    }

    boolean isTimedOut() {
        return this.timeOut != null && this.timeOut.before(Calendar.getInstance());
    }

    public CommandCallbackHolder<?> getCallback() {
        return callback;
    }
}
