package com.enioka.scanner.sdk.honeywelloss.spp.helpers;

import android.util.Log;

import com.enioka.scanner.sdk.honeywelloss.spp.SymbologyId;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MessagePrinter {
    private static final String LOG_TAG = "HonOssDriver";

    /**
     * This method is purely for debugging / research purposes, it is not optimized and should
     * not be used in release. It pretty-prints the content of a received buffer into the debug log output.
     */
    public static void prettyPrint(byte[] buffer, int offset, int dataLength) {
        StringBuilder printedStr = new StringBuilder();
        boolean inData = (buffer[0] != 0x16);
        boolean inPayloadSize = false;
        boolean inDataSize = false;

        for (int i = offset; i < dataLength; i++) {
            if (inPayloadSize) { // Message header decoding (payload length)
                byte[] bytes = { buffer[i], buffer[i+1], buffer[i+2], buffer[i+3] };
                int payloadSize = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
                printedStr.append("<Payload Size = ").append(payloadSize).append("> ");

                inPayloadSize = false;
                i += 3;
                continue;
            } else if (inDataSize) { // Payload header decoding (data length and codifications)
                int dataSize = 1000 * (buffer[i] - 0x30) + 100 * (buffer[i+1] - 0x30) + 10 * (buffer[i+2] - 0x30) + (buffer[i+3] - 0x30);
                printedStr.append("<Data Size = ").append(dataSize).append("> ");
                printedStr.append("<Honeywell = ").append(SymbologyId.honeywellIdMap.get((int) buffer[i + 4])).append("> ");
                printedStr.append("<AIM = ").append(SymbologyId.AIMIdMap.get((int) buffer[i+5])).append(" mod ").append((char) buffer[i+6]).append("> ");

                inDataSize = false;
                i += 6;
                continue;
            }

            // Regular printing (payload data and other header characters)
            switch (buffer[i]) {
                case 5: // 0x05 Enquiry: invalid tag or subtag
                    printedStr.append("<ENQ> ");
                    break;
                case 6: // 0x06 Acknowledge: ok
                    printedStr.append("<ACK> ");
                    break;
                case 13: // 0x0D Carriage Return: end of section
                    printedStr.append("<CR>\n");
                    break;
                case 21: // 0x15 Negative ACK: invalid data
                    printedStr.append("<NAK> ");
                    break;
                case 22: // 0x16 Synchronous Idle: synchronization
                    printedStr.append("Payload Header --- <SYN> ");
                    break;
                case 29: // 0x16 Group Separator: end of subsection
                    printedStr.append("<GS>\nData           --- ");
                    inData = true;
                    break;
                case 33: // 0x21 Exclamation mark (!): end of command, RAM
                    printedStr.append(">>RAM ");
                    break;
                case 46: // 0x2E Period (.): end of command, ROM
                    printedStr.append(">>ROM ");
                    break;
                case 77: // 0x54 T: end of identifier
                    printedStr.append(inData ? "" : "Data Header    --- ");
                    printedStr.append("M ");
                    break;
                case 84: // 0x54 T: end of identifier
                    printedStr.append("T ");
                    inDataSize = !inData;
                    break;
                case (byte) 254: // 0xFE: padding?
                    printedStr.append("<0xFE> ");
                    inPayloadSize = !inData;
                    break;
                default:
                    printedStr.append(buffer[i] > 31 && buffer[i] < 127 ? ((char) buffer[i]) : ("<0x" + Integer.toHexString(buffer[i]).toUpperCase() + ">")).append(" ");
                    break;
            }
        }
        Log.d(LOG_TAG, "Decoded received data:\n" + printedStr.toString().trim());
    }
}
