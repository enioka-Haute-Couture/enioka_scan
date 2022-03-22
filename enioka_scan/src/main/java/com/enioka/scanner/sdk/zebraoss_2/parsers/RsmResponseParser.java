package com.enioka.scanner.sdk.zebraoss_2.parsers;

import android.util.Log;

import com.enioka.scanner.bt.api.Helpers;
import com.enioka.scanner.sdk.zebraoss_2.data.RsmAttribute;
import com.enioka.scanner.sdk.zebraoss_2.data.RsmAttributeReply;

import java.nio.charset.StandardCharsets;

/**
 * SSI_MGMT_COMMAND answer parser. This is actually the encapsulation of another protocol named RSM which is only partially implemented here - and only for opcode 2, that is attribute query.
 */
public class RsmResponseParser implements PayloadParser<RsmAttributeReply> {
    private static final String LOG_TAG = "RsmResponseParser";

    @Override
    public RsmAttributeReply parseData(final byte[] dataBuffer) {
        int offset;
        RsmAttributeReply res = new RsmAttributeReply();
        if (dataBuffer.length < 4) {
            return null;
        }

        // First two bytes are RSM  packet (not data) length.
        int totalRsmLength = ((dataBuffer[0] & 0xFF) << 8) | (dataBuffer[1] & 0xFF);
        if (dataBuffer.length < totalRsmLength) {
            Log.w(LOG_TAG, "Received an incorrect RSM message - not enough bytes");
            return null;
        }

        // Third is opcode.
        int opCode = (int) dataBuffer[2] & 0xFF;
        if (opCode == 32) {
            // Special case needed at startup: dataBuffer size. Which we royally ignore afterward.
            RsmAttribute bufferAttribute = new RsmAttribute();
            bufferAttribute.id = 0xF0;
            bufferAttribute.data = "" + (((dataBuffer[6] & 0xFF) << 8) | (dataBuffer[7] & 0xFF));
            res.attributes.add(bufferAttribute);
            return res;
        } else if (opCode != 2) {
            // We for now only support 2 - attribute query.
            Log.w(LOG_TAG, "Received an unsupported RSM opcode " + opCode);
            return null;
        }

        // Fourth is status. We need 0.
        int status = (int) dataBuffer[3] & 0xFF;
        if (status != 0) {
            Log.w(LOG_TAG, "Received a failed RSM message. Status code is " + status);
            return null;
        }

        offset = 4; // offset is always at the start of an attribute answer block.
        while (offset + 2 < totalRsmLength) {
            RsmAttribute attribute = new RsmAttribute();

            // Two bytes for attribute ID (this is an attribute query response!)
            attribute.id = ((dataBuffer[offset] & 0xFF) << 8) | (dataBuffer[offset + 1] & 0xFF);

            if (attribute.id == 65535) {
                // FF FF is a termination string.
                break;
            }

            // Then one byte for attribute type
            int attributeType = (int) dataBuffer[offset + 2] & 0xFF;

            // offset+3 seems to be some metadata like read/write flags.

            // What happens next depends on attribute type - there may be more headers in complicated cases.
            switch (attributeType) {
                case 66: // B or 0x42
                    // Byte.
                    if (dataBuffer.length < offset + 4) {
                        Log.w(LOG_TAG, "Received an incorrect RSM message - not enough bytes for a byte attribute");
                        return null;
                    }
                    attribute.data = ((int) dataBuffer[offset + 4]) + "";

                    Log.d(LOG_TAG, "Message contains an RSM attribute of type byte");
                    offset += 4 + 1;
                    break;
                case 87: // W or 0x57
                    // Word.
                    if (dataBuffer.length < offset + 5) {
                        Log.w(LOG_TAG, "Received an incorrect RSM message - not enough bytes for a word attribute");
                        return null;
                    }
                    attribute.data = (((dataBuffer[offset + 4] & 0xFF) << 8) | (dataBuffer[offset + 5] & 0xFF)) + "";

                    Log.d(LOG_TAG, "Message contains an RSM attribute of type word");
                    offset += 4 + 2;
                    break;
                case 65: // A or 0x41
                    // Array (usually of bytes).
                    if (dataBuffer.length < offset + 13) {
                        Log.w(LOG_TAG, "Received an incorrect RSM message - not enough bytes for an array attribute");
                        return null;
                    }

                    int subType = (int) dataBuffer[offset + 4] & 0xFF;
                    int arrayLength = ((dataBuffer[offset + 5] & 0xFF) << 8) | (dataBuffer[offset + 6] & 0xFF);
                    //int arrayOffset = ((dataBuffer[offset + 7] & 0xFF) << 8) | (dataBuffer[offset + 8] & 0xFF);

                    Log.d(LOG_TAG, "Message contains an RSM attribute of type array, subtype is " + subType + " and array length is " + arrayLength);

                    if (dataBuffer.length < offset + 9 + arrayLength) {
                        Log.w(LOG_TAG, "Received an incorrect RSM message - not enough bytes for an array attribute of length " + arrayLength);
                        return null;
                    }
                    attribute.data = Helpers.byteArrayToHex(dataBuffer, offset + 9, arrayLength); // new String(dataBuffer, offset + 9, arrayLength, StandardCharsets.US_ASCII);

                    offset += 9 + arrayLength;
                    break;

                case 83: // S or 0x53
                    // String! Same as array but without subtype it seems.
                    if (dataBuffer.length < offset + 12) {
                        Log.w(LOG_TAG, "Received an incorrect RSM message - not enough bytes for a string attribute");
                        return null;
                    }

                    arrayLength = ((dataBuffer[offset + 4] & 0xFF) << 8) | (dataBuffer[offset + 5] & 0xFF);
                    //arrayOffset = ((dataBuffer[offset + 6] & 0xFF) << 8) | (dataBuffer[offset + 7] & 0xFF);

                    Log.d(LOG_TAG, "Message contains an RSM attribute of type string, string length is " + arrayLength);

                    if (dataBuffer.length < offset + 8 + arrayLength) {
                        Log.w(LOG_TAG, "Received an incorrect RSM message - not enough bytes for a string attribute of length " + arrayLength + " at offset " + offset + " of dataBuffer length " + dataBuffer.length);
                        return null;
                    }
                    attribute.data = new String(dataBuffer, offset + 8, arrayLength, StandardCharsets.US_ASCII).trim();

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
