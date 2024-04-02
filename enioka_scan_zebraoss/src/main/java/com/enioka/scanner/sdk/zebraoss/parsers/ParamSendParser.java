package com.enioka.scanner.sdk.zebraoss.parsers;

import android.support.v4.util.Pair;
import android.util.Log;

import com.enioka.scanner.sdk.zebraoss.data.ParamSend;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for handling data in response to a parameter query call.
 */
public class ParamSendParser implements PayloadParser<ParamSend> {
    private static final String LOG_TAG = "RsmResponseParser";

    @Override
    // FIXME: Legacy code handled multi-packets in a special way; which did not carry over to this rewrite. Will be worth re-checking if handling multi-packet ParamSend packets becomes needed.
    public ParamSend parseData(final byte[] dataBuffer) {
        if (dataBuffer.length < 1) {
            return null;
        }

        List<ParamSend.Param> parameters = new ArrayList<>();

        // The sound byte is repeated on each packet inside a multi-packet stream. Therefore analysis must be done packet per packet and not on the concatenated message.getData() byte array.
        parameters.addAll(parseChunk(dataBuffer, 1));  // Ignore first data byte, its a BEEP indicator we ignore.

        return new ParamSend(parameters);
    }

    private List<ParamSend.Param> parseChunk(byte[] paramData, int shift) {
        List<ParamSend.Param> parameters = new ArrayList<>();

        int curIdx = shift;
        while (curIdx < paramData.length) {
            byte curByte = paramData[curIdx];

            ParamSend.Param parameter = new ParamSend.Param();
            int dataLength;

            // Type modifiers
            switch (curByte) {
                case (byte) (0xF3 & 0xFF):
                    // STRING
                    parameter.type = ParamSend.PARAM_TYPE.STRING;
                    Pair<Integer, Integer> p = readParamNum(paramData, curIdx + 1);
                    dataLength = (paramData[curIdx + 1 + p.second] & 0xFF);
                    parameter.currentValue = new byte[dataLength];
                    System.arraycopy(paramData, curIdx + 1 + p.second + 1, parameter.currentValue, 0, dataLength);
                    curIdx += 1 + p.second + 1 + dataLength;
                    break;
                case (byte) (0xF4 & 0xFF):
                    // WORD (double byte)
                    parameter.type = ParamSend.PARAM_TYPE.WORD;
                    p = readParamNum(paramData, curIdx + 1);
                    parameter.number = p.first;
                    parameter.currentValue = new byte[]{paramData[curIdx + p.second + 1], paramData[curIdx + p.second + 2]};
                    curIdx += 3 + p.second;
                    break;
                case (byte) (0xF6 & 0xFF):
                    // ARRAY
                    parameter.type = ParamSend.PARAM_TYPE.ARRAY;
                    p = readParamNum(paramData, curIdx + 1);
                    parameter.number = p.first;
                    dataLength = (paramData[curIdx + 1 + p.second] & 0xFF);
                    parameter.currentValue = new byte[dataLength];
                    System.arraycopy(paramData, curIdx + 1 + p.second + 1, parameter.currentValue, 0, dataLength);
                    curIdx += 1 + p.second + 1 + dataLength;
                    break;
                case (byte) (0xF7 & 0xFF):
                    // MULTI PACKET - not supported.
                    parameter.number = (paramData[curIdx + 1] & 0xFF);
                    parameter.type = ParamSend.PARAM_TYPE.MULTIPACKET;
                    dataLength = dataLength = (paramData[curIdx + 2] & 0xFF);
                    curIdx += dataLength + 2 + dataLength + 1;
                    break;
                default:
                    // Standard one byte case.
                    p = readParamNum(paramData, curIdx);
                    parameter.type = ParamSend.PARAM_TYPE.BYTE;
                    parameter.number = p.first;
                    parameter.currentValue = new byte[]{(byte) (paramData[curIdx + p.second] & 0xFF)};
                    curIdx += p.second + 1;
            }

            parameters.add(parameter);
        }

        return parameters;
    }

    /**
     * @param buffer
     * @param start
     * @return param number and buffer position in this order.
     */
    private Pair<Integer, Integer> readParamNum(byte[] buffer, int start) {
        byte curByte = buffer[start];
        int curIdx = start;
        int number;

        if (curByte == (byte) (0x8e & 0xFF)) {
            Log.i(LOG_TAG, "rrrrrr"); // ???
        }

        switch (curByte) {
            case (byte) (0xF0 & 0xFF):
                // Means parameter from 256 to 495
                number = (buffer[curIdx + 1] & 0xFF) + 256;
                curIdx += 2;
                break;
            case (byte) (0xF1 & 0xFF):
                // Means parameter from 512 to 751
                number = (buffer[curIdx + 1] & 0xFF) + 512;
                curIdx += 2;
                break;
            case (byte) (0xF2 & 0xFF):
                // Means parameter from 768 to 1007
                number = (buffer[curIdx + 1] & 0xFF) + 768;
                curIdx += 2;
                break;
            case (byte) (0xF8 & 0xFF):
                // Means F8 HIGHBYTE LOWBYTE (1024 & higher)
                number = ((buffer[curIdx + 1] & 0xFF) << 8) | (buffer[curIdx + 2] & 0xFF);
                curIdx += 3;
                break;
            default:
                // Standard case, number 0 to 239
                number = curByte & 0xFF;
                curIdx += 1;
                break;
        }

        return Pair.create(number, curIdx - start);
    }
}
