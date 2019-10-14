package com.enioka.scanner.bt.api;

// TODO: simplify this, remove public access...

/**
 * The result of parsing data coming from the device input stream.
 *
 * @param <T>
 */
public class ParsingResult<T> {

    /**
     * Constructs a result "All is OK, here is the data".
     *
     * @param data parsed data which could be given to callers.
     */
    public ParsingResult(T data) {
        this.data = data;
        expectingMoreData = false;
        rejected = false;
    }

    /**
     * Constructs a result "All was read, this is a failure"
     *
     * @param result
     */
    public ParsingResult(MessageRejectionReason result) {
        this.result = result;
        expectingMoreData = false;
        rejected = true;
    }

    /**
     * Constructs a result "need more data to continue".
     */
    public ParsingResult() {
    }

    public boolean expectingMoreData = true;
    public boolean rejected = false;
    public MessageRejectionReason result;
    public T data;
    public Command acknowledger = null;

    MessageRejectionReason getResult() {
        return this.result;
    }

    T getData() {
        return this.data;
    }
}
