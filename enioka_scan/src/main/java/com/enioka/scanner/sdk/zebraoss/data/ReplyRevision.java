package com.enioka.scanner.sdk.zebraoss.data;

import com.enioka.scanner.sdk.zebraoss.SsiMessage;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ReplyRevision {
    public String softwareRevision, boardType, engineCode;

    public ReplyRevision(byte[] buffer) {
        String temp = new String(buffer, Charset.forName("ASCII"));
        temp = temp.replaceAll(" {2,10}", " ");
        if (!temp.isEmpty()) {
            String[] segments = temp.split(" ");

            if (segments.length >= 3) {
                this.softwareRevision = segments[0];
                this.boardType = segments[1];
                this.engineCode = segments[2];
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("SSI device revision report\n");

        sb.append("S/W revision: ");
        sb.append(this.softwareRevision);
        sb.append("\n");

        sb.append("Board Type, flash or non-flash: ");
        sb.append(this.boardType);
        sb.append("\n");

        sb.append("Engine Code: ");
        sb.append(this.engineCode);
        sb.append("\n");

        return sb.toString();
    }
}
