package com.enioka.scanner.sdk.zebraoss.data;

import java.util.ArrayList;
import java.util.List;

public class RsmAttributeReply {
    public List<RsmAttribute> attributes = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RSM attributes \n");
        for (RsmAttribute attr : attributes) {
            sb.append("\t");
            sb.append(attr.toString());
            sb.append("\n");
        }

        return sb.toString();
    }
}
