package com.enioka.scanner.sdk.zebraoss;

import com.enioka.scanner.sdk.zebraoss.parsers.AckParser;
import com.enioka.scanner.sdk.zebraoss.parsers.BarcodeParser;
import com.enioka.scanner.sdk.zebraoss.parsers.CapabilitiesParser;
import com.enioka.scanner.sdk.zebraoss.parsers.ErrorParser;
import com.enioka.scanner.sdk.zebraoss.parsers.EventParser;
import com.enioka.scanner.sdk.zebraoss.parsers.GenericParser;
import com.enioka.scanner.sdk.zebraoss.parsers.ParamSendParser;
import com.enioka.scanner.sdk.zebraoss.parsers.PayloadParser;
import com.enioka.scanner.sdk.zebraoss.parsers.ReplyRevisionParser;
import com.enioka.scanner.sdk.zebraoss.parsers.RsmResponseParser;
import com.enioka.scanner.sdk.zebraoss.parsers.ScannerInitParser;

public enum SsiMessage {
    // Only messages coming from the scanner are actually needed here. For completion sake, all messages were added.

    ABORT_MACRO_PDF(0x11, SsiSource.HOST),
    AIM_OFF(0xC4, SsiSource.HOST),
    AIM_ON(0xC5, SsiSource.HOST),
    BATCH_DATA(0xD6, SsiSource.DECODER),
    BATCH_REQUEST(0xD5, SsiSource.HOST),
    BEEP(0xE6, SsiSource.HOST),
    CAPABILITIES_REQUEST(0xD3, SsiSource.HOST),
    CAPABILITIES_REPLY(0xD4, SsiSource.DECODER, false, new CapabilitiesParser()),
    CHANGE_ALL_CODE_TYPES(0xC9, SsiSource.HOST),
    CMD_ACK(0xD0, SsiSource.BOTH, false, new AckParser()),
    CMD_ACK_ACTION(0xD8, SsiSource.HOST), // No ACK ever // Actual use??
    CMD_NACK(0xD1, SsiSource.BOTH, false, new ErrorParser()),
    CUSTOM_DEFAULTS(0x12, SsiSource.HOST),
    DECODE_DATA(0xF3, SsiSource.DECODER, true, new BarcodeParser()),
    EVENT(0xF6, SsiSource.DECODER, true, new EventParser()),
    FLUSH_MACRO_PDF(0x10, SsiSource.HOST),
    FLUSH_QUEUE(0xD2, SsiSource.HOST),
    ILLUMINATION_OFF(0xC0, SsiSource.HOST),
    ILLUMINATION_ON(0xC1, SsiSource.HOST),
    IMAGE_DATA(0xB1, SsiSource.DECODER, true),
    IMAGER_MODE(0xF7, SsiSource.HOST),
    LED_OFF(0xE8, SsiSource.HOST),
    LED_ON(0xE7, SsiSource.HOST),
    PAGE_MOTOR_ACTIVATION(0xCA, SsiSource.HOST),
    PARAM_DEFAULTS(0xC8, SsiSource.HOST),
    PARAM_REQUEST(0xC7, SsiSource.HOST),
    PARAM_SEND(0xC6, SsiSource.BOTH, true, new ParamSendParser()),
    REPLY_REVISION(0xA4, SsiSource.DECODER, false, new ReplyRevisionParser()),
    REQUEST_REVISION(0xA3, SsiSource.HOST),
    SCAN_DISABLE(0xEA, SsiSource.HOST),
    SCAN_ENABLE(0xE9, SsiSource.HOST),
    SCANNER_INIT(0x91, SsiSource.DECODER, new ScannerInitParser()),
    SLEEP(0xEB, SsiSource.HOST),
    SSI_MGMT_COMMAND(0x80, SsiSource.BOTH, true, new RsmResponseParser()),
    START_SESSION(0xE4, SsiSource.HOST),
    STOP_SESSION(0xE5, SsiSource.HOST),
    VIDEO_DATA(0xB4, SsiSource.DECODER, true),
    //WAKEUP(), // can only be a physical command
    LOG_DATA(0x14, SsiSource.DECODER, true, new GenericParser()),
    SEND_LOG(0x13, SsiSource.HOST),
    NONE(0xFF, SsiSource.BOTH);

    private byte opCode;
    private SsiSource source;
    private boolean acknowledger;
    private PayloadParser parser;


    SsiMessage(int opCode, SsiSource source) {
        this((byte) opCode, source, false, null);
    }

    SsiMessage(int opCode, SsiSource source, PayloadParser parser) {
        this((byte) opCode, source, false, parser);
    }

    SsiMessage(int opCode, SsiSource source, boolean acknowledger) {
        this((byte) opCode, source, acknowledger, null);
    }

    SsiMessage(int opCode, SsiSource source, boolean acknowledger, PayloadParser parser) {
        this((byte) opCode, source, acknowledger, parser);
    }

    SsiMessage(byte opCode, SsiSource source, boolean acknowledger, PayloadParser parser) {
        this.opCode = opCode;
        this.source = source;
        this.acknowledger = acknowledger;
        this.parser = parser;
    }

    public byte getValue() {
        return opCode;
    }

    public SsiSource getSource() {
        return source;
    }

    public PayloadParser getParser() {
        return parser;
    }

    public boolean needsAck() {
        return acknowledger;
    }

    public static SsiMessage GetValue(byte opCode) {
        SsiMessage[] All = SsiMessage.values();
        for (int i = 0; i < All.length; i++) {
            if (All[i].opCode == opCode) {
                return All[i];
            }
        }
        return SsiMessage.NONE;
    }
}
