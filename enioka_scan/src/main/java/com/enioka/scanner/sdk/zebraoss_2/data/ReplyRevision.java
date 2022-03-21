package com.enioka.scanner.sdk.zebraoss_2.data;

import java.nio.charset.StandardCharsets;

public class ReplyRevision {
    public String softwareRevision, boardType, engineCode;

    public ReplyRevision(byte[] buffer) {
        String temp = new String(buffer, StandardCharsets.US_ASCII);
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
