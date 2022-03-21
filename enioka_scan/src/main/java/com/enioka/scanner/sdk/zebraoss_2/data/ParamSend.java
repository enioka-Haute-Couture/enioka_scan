package com.enioka.scanner.sdk.zebraoss_2.data;

import static com.enioka.scanner.bt.api.Helpers.byteArrayToHex;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Answer to a parameter value query.
 */
public class ParamSend {
    public List<Param> parameters = new ArrayList<>();

    public enum PARAM_TYPE {
        BYTE, STRING, WORD, ARRAY, MULTIPACKET
    }

    public static class Param {
        public int number;
        public PARAM_TYPE type;
        public byte[] currentValue;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Parameter number ");
            sb.append(number);
            sb.append(" of type ");
            sb.append(type.toString());
            sb.append(" with data ");

            sb.append(getStringValue());

            return sb.toString();
        }

        public String getStringValue() {
            switch (type) {
                case STRING:
                    return new String(currentValue, StandardCharsets.US_ASCII);
                case BYTE:
                    return String.format("0x%02x", currentValue[0]);
                case ARRAY:
                    return byteArrayToHex(currentValue, currentValue.length);
                case WORD:
                    return "" + (((currentValue[0] & 0xFF) << 8) | (currentValue[1] & 0xFF));
                case MULTIPACKET:
                    return "NOT SUPPORTED";
            }
            throw new RuntimeException("cannot reach this code");
        }
    }

    public ParamSend(List<ParamSend.Param> parameters) {
        this.parameters = parameters;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Parameter description report\n");

        for (Param p : parameters) {
            sb.append("\t");
            sb.append(p.toString());
            sb.append("\n");
        }

        return sb.toString();
    }
}
