package com.enioka.scanner.sdk.zebraoss.parsers;

import android.util.Log;

import com.enioka.scanner.bt.api.Helpers;
import com.enioka.scanner.sdk.zebraoss.SsiMultiPacketMessage;
import com.enioka.scanner.sdk.zebraoss.data.RsmAttribute;
import com.enioka.scanner.sdk.zebraoss.data.RsmAttributeReply;

import java.nio.charset.StandardCharsets;

/**
 * SSI_MGMT_COMMAND answer parser. This is actually the encapsulation of another protocol named RSM which is only partially implemented here - and only for opcode 2, that is attribute query.
 */
public class RsmResponseParser implements PayloadParser<RsmAttributeReply> {
    private static final String LOG_TAG = "SsiParser";

    @Override
    public RsmAttributeReply parseData(SsiMultiPacketMessage message) {
        byte[] buffer = message.getData();
        int offset;
        RsmAttributeReply res = new RsmAttributeReply();
        if (buffer.length < 4) {
            return null;
        }

        // First two bytes are RSM  packet (not data) length.
        int totalRsmLength = ((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF);
        if (buffer.length < totalRsmLength) {
            Log.w(LOG_TAG, "Received an incorrect RSM message - not enough bytes");
            return null;
        }

        // Third is opcode.
        int opCode = (int) buffer[2] & 0xFF;
        if (opCode == 32) {
            // Special case needed at startup: buffer size. Which we royally ignore afterward.
            RsmAttribute bufferAttribute = new RsmAttribute();
            bufferAttribute.id = 0xF0;
            bufferAttribute.data = "" + (((buffer[6] & 0xFF) << 8) | (buffer[7] & 0xFF));
            res.attributes.add(bufferAttribute);
            return res;
        } else if (opCode != 2) {
            // We for now only support 2 - attribute query.
            Log.w(LOG_TAG, "Received an unsupported RSM opcode " + opCode);
            return null;
        }

        // Fourth is status. We need 0.
        int status = (int) buffer[3] & 0xFF;
        if (status != 0) {
            Log.w(LOG_TAG, "Received a failed RSM message. Status code is " + status);
            return null;
        }

        offset = 4; // offset is always at the start of an attribute answer block.
        while (offset + 2 < totalRsmLength) {
            RsmAttribute attribute = new RsmAttribute();

            // Two bytes for attribute ID (this is an attribute query response!)
            attribute.id = ((buffer[offset] & 0xFF) << 8) | (buffer[offset + 1] & 0xFF);

            if (attribute.id == 65535) {
                // FF FF is a termination string.
                break;
            }

            // Then one byte for attribute type
            int attributeType = (int) buffer[offset + 2] & 0xFF;

            // offset+3 seems to be some metadata like read/write flags.

            // What happens next depends on attribute type - there may be more headers in complicated cases.
            switch (attributeType) {
                case 66: // B or 0x42
                    // Byte.
                    if (buffer.length < offset + 4) {
                        Log.w(LOG_TAG, "Received an incorrect RSM message - not enough bytes for a byte attribute");
                        return null;
                    }
                    attribute.data = ((int) buffer[offset + 4]) + "";

                    Log.d(LOG_TAG, "Message contains an RSM attribute of type byte");
                    offset += 4 + 1;
                    break;
                case 87: // W or 0x57
                    // Word.
                    if (buffer.length < offset + 5) {
                        Log.w(LOG_TAG, "Received an incorrect RSM message - not enough bytes for a word attribute");
                        return null;
                    }
                    attribute.data = (((buffer[offset + 4] & 0xFF) << 8) | (buffer[offset + 5] & 0xFF)) + "";

                    Log.d(LOG_TAG, "Message contains an RSM attribute of type word");
                    offset += 4 + 2;
                    break;
                case 65: // A or 0x41
                    // Array (usually of bytes).
                    if (buffer.length < offset + 13) {
                        Log.w(LOG_TAG, "Received an incorrect RSM message - not enough bytes for an array attribute");
                        return null;
                    }

                    int subType = (int) buffer[offset + 4] & 0xFF;
                    int arrayLength = ((buffer[offset + 5] & 0xFF) << 8) | (buffer[offset + 6] & 0xFF);
                    //int arrayOffset = ((buffer[offset + 7] & 0xFF) << 8) | (buffer[offset + 8] & 0xFF);

                    Log.d(LOG_TAG, "Message contains an RSM attribute of type array, subtype is " + subType + " and array length is " + arrayLength);

                    if (buffer.length < offset + 9 + arrayLength) {
                        Log.w(LOG_TAG, "Received an incorrect RSM message - not enough bytes for an array attribute of length " + arrayLength);
                        return null;
                    }
                    attribute.data = Helpers.byteArrayToHex(buffer, offset + 9, arrayLength); // new String(buffer, offset + 9, arrayLength, StandardCharsets.US_ASCII);

                    offset += 9 + arrayLength;
                    break;

                case 83: // S or 0x53
                    // String! Same as array but without subtype it seems.
                    if (buffer.length < offset + 12) {
                        Log.w(LOG_TAG, "Received an incorrect RSM message - not enough bytes for a string attribute");
                        return null;
                    }

                    arrayLength = ((buffer[offset + 4] & 0xFF) << 8) | (buffer[offset + 5] & 0xFF);
                    //arrayOffset = ((buffer[offset + 6] & 0xFF) << 8) | (buffer[offset + 7] & 0xFF);

                    Log.d(LOG_TAG, "Message contains an RSM attribute of type string, string length is " + arrayLength);

                    if (buffer.length < offset + 8 + arrayLength) {
                        Log.w(LOG_TAG, "Received an incorrect RSM message - not enough bytes for a string attribute of length " + arrayLength + " at offset " + offset + " of buffer length " + buffer.length);
                        return null;
                    }
                    attribute.data = new String(buffer, offset + 8, arrayLength, StandardCharsets.US_ASCII).trim();

                    offset += 8 + arrayLength;
                    break;
                default:
                    Log.w(LOG_TAG, "Unsupported attribute type detected - ignoring Rsm result. " + attributeType);
                    return null;
            }

            res.attributes.add(attribute);
        }

        return res;
    }
}
