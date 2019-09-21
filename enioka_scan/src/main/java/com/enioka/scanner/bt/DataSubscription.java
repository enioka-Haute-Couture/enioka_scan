package com.enioka.scanner.bt;

import java.util.Calendar;

/**
 * Helper class storing execution context around a command callback.
 */
class DataSubscription {
    private final CommandCallback<?> callback;
    private Calendar timeOut = null;
    private final boolean permanent;

    DataSubscription(CommandCallback<?> callback, int timeOutMs, boolean permanent) {
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

    public CommandCallback<?> getCallback() {
        return callback;
    }

    boolean isPermanent() {
        return this.permanent;
    }
}
