package com.enioka.scanner.bt;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

public class OrderedInputHandler implements BtDeviceInputHandler {
    private static final String LOG_TAG = "InternalBtDevice";
    private byte[] terminator = new byte[]{0xD, 0x0A}; // CRLF

    private Queue<BtCommandWithAnswer> expectedAnswers = new LinkedList<>();
    private final BtDeviceInputHandler defaultInputHandler;

    OrderedInputHandler() {
        this.defaultInputHandler = new LoggingInputHandler();
    }

    void expectAnswer(BtCommandWithAnswer command) {
        this.expectedAnswers.add(command);
    }

    @Override
    public void process(byte[] buffer, int offset, int dataLength) {
        byte[] actualTerminator = terminator;
        BtDeviceInputHandler handler = expectedAnswers.peek();
        if (handler == null) {
            handler = defaultInputHandler;
        } else {
            byte[] overrideTerminator = expectedAnswers.peek().getAnswerTerminator();
            if (overrideTerminator != null) {
                actualTerminator = overrideTerminator;
            }
        }

        boolean terminated = true;
        for (int i = dataLength - 1; i >= 0 && i >= dataLength - actualTerminator.length; i--) {
            if (buffer[i] != actualTerminator[i - dataLength + actualTerminator.length]) {
                terminated = false;
                break;
            }
        }

        if (terminated) {
            if (terminator.length > 0) {
                Log.d(LOG_TAG, "terminated with " + terminator.length + " bytes");
            }
            handler.process(buffer, offset, dataLength - terminator.length);
            handler.endOfTransmission();
            expectedAnswers.poll();
        } else {
            handler.process(buffer, offset, dataLength);
        }
    }

    @Override
    public void endOfTransmission() {
        // Nothing - never called, as this is the class which does that kind of call...
    }


}
