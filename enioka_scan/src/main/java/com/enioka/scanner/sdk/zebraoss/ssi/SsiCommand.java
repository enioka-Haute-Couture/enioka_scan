package com.enioka.scanner.sdk.zebraoss.ssi;

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

public enum SsiCommand {
    // Only messages coming from the scanner are actually needed here. For completion sake, all messages were added.

    FLUSH_MACRO_PDF(0x10, SsiSource.HOST),
    ABORT_MACRO_PDF(0x11, SsiSource.HOST),
    CUSTOM_DEFAULTS(0x12, SsiSource.HOST),
    SEND_LOG(0x13, SsiSource.HOST),
    LOG_DATA(0x14, SsiSource.DECODER, true, new GenericParser()),
    MULTIPACKET_SEGMENT(0x73, SsiSource.DECODER), // Only observed for barcode scans, supported directly in the parser
    MULTIPACKET_ACK(0x74, SsiSource.HOST), // Triggered directly in the parser
    SSI_MGMT_COMMAND(0x80, SsiSource.BOTH, true, new RsmResponseParser()),
    SCANNER_INIT_COMMAND(0x90, SsiSource.HOST),
    SCANNER_INIT(0x91, SsiSource.DECODER, new ScannerInitParser()),
    TEMP_COMMAND(0x93, SsiSource.BOTH, true, new GenericParser()), // Not documented, source of problems
    REQUEST_REVISION(0xA3, SsiSource.HOST),
    REPLY_REVISION(0xA4, SsiSource.DECODER, false, new ReplyRevisionParser()),
    IMAGE_DATA(0xB1, SsiSource.DECODER, true, new GenericParser()),
    VIDEO_DATA(0xB4, SsiSource.DECODER, true, new GenericParser()),
    ILLUMINATION_OFF(0xC0, SsiSource.HOST),
    ILLUMINATION_ON(0xC1, SsiSource.HOST),
    AIM_OFF(0xC4, SsiSource.HOST),
    AIM_ON(0xC5, SsiSource.HOST),
    PARAM_SEND(0xC6, SsiSource.BOTH, true, new ParamSendParser()),
    PARAM_REQUEST(0xC7, SsiSource.HOST),
    PARAM_DEFAULTS(0xC8, SsiSource.HOST),
    CHANGE_ALL_CODE_TYPES(0xC9, SsiSource.HOST),
    PAGE_MOTOR_ACTIVATION(0xCA, SsiSource.HOST),
    CMD_ACK(0xD0, SsiSource.BOTH, false, new AckParser()),
    CMD_NACK(0xD1, SsiSource.BOTH, false, new ErrorParser()),
    FLUSH_QUEUE(0xD2, SsiSource.HOST),
    CAPABILITIES_REQUEST(0xD3, SsiSource.HOST),
    CAPABILITIES_REPLY(0xD4, SsiSource.DECODER, false, new CapabilitiesParser()),
    BATCH_REQUEST(0xD5, SsiSource.HOST),
    BATCH_DATA(0xD6, SsiSource.DECODER, true, new GenericParser()), // FIXME: needs parser
    CMD_ACK_ACTION(0xD8, SsiSource.HOST), // No ACK ever // Actual use??
    START_SESSION(0xE4, SsiSource.HOST),
    STOP_SESSION(0xE5, SsiSource.HOST),
    BEEP(0xE6, SsiSource.HOST),
    LED_ON(0xE7, SsiSource.HOST),
    LED_OFF(0xE8, SsiSource.HOST),
    SCAN_ENABLE(0xE9, SsiSource.HOST),
    SCAN_DISABLE(0xEA, SsiSource.HOST),
    SLEEP(0xEB, SsiSource.HOST),
    DECODE_DATA(0xF3, SsiSource.DECODER, true, new BarcodeParser()),
    EVENT(0xF6, SsiSource.DECODER, true, new EventParser()),
    IMAGER_MODE(0xF7, SsiSource.HOST),
    //WAKEUP(), // can only be a physical command
    NONE(0xFF, SsiSource.BOTH);

    private final byte opCode;
    private final SsiSource source;
    private final boolean acknowledger;
    private final PayloadParser parser;


    SsiCommand(int opCode, SsiSource source) {
        this((byte) opCode, source, false, null);
    }

    SsiCommand(int opCode, SsiSource source, PayloadParser parser) {
        this((byte) opCode, source, false, parser);
    }

    SsiCommand(int opCode, SsiSource source, boolean acknowledger) {
        this((byte) opCode, source, acknowledger, null);
    }

    SsiCommand(int opCode, SsiSource source, boolean acknowledger, PayloadParser parser) {
        this((byte) opCode, source, acknowledger, parser);
    }

    SsiCommand(byte opCode, SsiSource source, boolean acknowledger, PayloadParser parser) {
        this.opCode = opCode;
        this.source = source;
        this.acknowledger = acknowledger;
        this.parser = parser;
    }

    public byte getOpCode() {
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

    public static SsiCommand getCommand(byte opCode) {
        SsiCommand[] All = SsiCommand.values();
        for (int i = 0; i < All.length; i++) {
            if (All[i].opCode == opCode) {
                return All[i];
            }
        }
        return SsiCommand.NONE;
    }
}
