package com.enioka.scanner.sdk.zebraoss_2.data;

import com.enioka.scanner.sdk.zebraoss_2.commons.SsiCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CapabilitiesReply {
    private final Set<Byte> opCodes;
    private final boolean supportsMultiPacket;

    public CapabilitiesReply(boolean supportsMultiPacket, Set<Byte> opCodes) {
        this.opCodes = opCodes;
        this.supportsMultiPacket = supportsMultiPacket;
    }

    public boolean supportsMultiPacket() {
        return this.supportsMultiPacket;
    }

    public boolean supportsOpCode(byte opCode) {
        return this.opCodes.contains(opCode);
    }

    public boolean supportsMessage(SsiCommand command) {
        return this.supportsOpCode(command.getOpCode());
    }

    @Override
    public String toString() {
        // We want order!
        List<Byte> sortedList = new ArrayList<>(this.opCodes);
        Collections.sort(sortedList);
        StringBuilder sb = new StringBuilder();

        sb.append("SSI device capabilities report\n");

        for (Byte b : sortedList) {
            SsiCommand message = SsiCommand.getCommand(b);

            if (message != SsiCommand.NONE) {
                sb.append("Supports command ");
                sb.append(message.name());
                sb.append(" (");
                sb.append(String.format("0x%02x", b));
                sb.append(")");
                sb.append("\n");
            } else {
                sb.append("Supports unknown command ");
                sb.append(String.format("0x%02x ", b));
                sb.append("\n");
            }
        }

        for (SsiCommand message : SsiCommand.values()) {
            if (!this.supportsMessage(message)) {
                sb.append("Does not support command ");
                sb.append(message.name());
                sb.append(" (");
                sb.append(String.format("0x%02x", message.getOpCode()));
                sb.append(")");
                sb.append("\n");
            }
        }

        sb.append("Supports multi packets: ");
        sb.append(this.supportsMultiPacket);
        sb.append("\n");

        return sb.toString();
    }
}
