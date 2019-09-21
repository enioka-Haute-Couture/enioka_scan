package com.enioka.scanner.bt.manager;

import com.enioka.scanner.bt.api.DataSubscriptionCallback;

import java.util.Calendar;

/**
 * Helper class storing execution context around a command callback.
 */
class DataSubscription {
    private final DataSubscriptionCallback<?> callback;
    private Calendar timeOut = null;
    private final boolean permanent;

    DataSubscription(DataSubscriptionCallback<?> callback, int timeOutMs, boolean permanent) {
        this.callback = callback;
        this.permanent = permanent;

        if (timeOutMs > 0) {
            timeOut = Calendar.getInstance();
            timeOut.add(Calendar.MILLISECOND, timeOutMs);
        }
    }

    boolean isTimedOut() {
        return this.timeOut != null && this.timeOut.before(Calendar.getInstance());
    }

    public DataSubscriptionCallback<?> getCallback() {
        return callback;
    }

    boolean isPermanent() {
        return this.permanent;
    }
}
