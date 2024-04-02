package com.enioka.scanner.sdk.postech.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.postech.data.DeviceName;

public class GetDeviceName implements Command<DeviceName> {

    @Override
    public byte[] getCommand() {
        return "{GB100}{G1000?}".getBytes();
    }

    @Override
    public Class<? extends DeviceName> getReturnType() {
        return DeviceName.class;
    }

    @Override
    public int getTimeOut() {
        return 1000;
    }
}
