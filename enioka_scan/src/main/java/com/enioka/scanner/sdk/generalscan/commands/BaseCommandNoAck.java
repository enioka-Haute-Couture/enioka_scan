package com.enioka.scanner.sdk.generalscan.commands;

import com.enioka.scanner.bt.api.Command;

import java.nio.charset.Charset;

public class BaseCommandNoAck implements Command<Void> {
    protected String stringCommand;

    @Override
    public byte[] getCommand() {
        if (stringCommand != null) {
            return (stringCommand /*+ "{G1026}"*/).getBytes(Charset.forName("ASCII"));
        }
        return new byte[0];
    }

    @Override
    public Class<? extends Void> getReturnType() {
        return null;
    }

    @Override
    public int getTimeOut() {
        return 200;
    }
}
