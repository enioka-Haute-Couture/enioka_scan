package com.enioka.scanner.sdk.generalscan.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.generalscan.data.DeviceId;

public class GetDeviceId implements Command<DeviceId> {

    @Override
    public byte[] getCommand() {
        return "{G1065}".getBytes();
    }

    @Override
    public Class<? extends DeviceId> getReturnType() {
        return DeviceId.class;
    }

    @Override
    public int getTimeOut() {
        return 100;
    }
}
