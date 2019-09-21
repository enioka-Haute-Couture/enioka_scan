package com.enioka.scanner.sdk.generalscan.commands;

import com.enioka.scanner.bt.CommandCallback;
import com.enioka.scanner.bt.CommandCallbackHolder;
import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.sdk.generalscan.data.DeviceId;

public class GetDeviceId implements ICommand<DeviceId> {
    private final CommandCallback<DeviceId> callback;

    public GetDeviceId(CommandCallback<DeviceId> callback) {
        this.callback = callback;
    }

    @Override
    public byte[] getCommand() {
        return "{G1065}".getBytes();
    }

    @Override
    public CommandCallbackHolder<DeviceId> getCallback() {
        return new CommandCallbackHolder<>(DeviceId.class, this.callback, false, 100);
    }
}
