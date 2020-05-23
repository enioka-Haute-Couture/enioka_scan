package com.enioka.scanner.bt.manager;

import java.util.UUID;

@SuppressWarnings("unused")
public enum GattAttribute {
    /////////////////////////////////////////////
    // Standardized attributes

    // 0x2A... are normalized characteristics (https://www.bluetooth.com/specifications/gatt/characteristics/)
    DEVICE_NAME("00002a00-0000-1000-8000-00805f9b34fb", GattAttributeType.CHARACTERISTIC),
    APPEARANCE("00002a01-0000-1000-8000-00805f9b34fb", GattAttributeType.CHARACTERISTIC),
    PERIPHERAL_PRIVACY_FLAG("00002a02-0000-1000-8000-00805f9b34fb", GattAttributeType.CHARACTERISTIC),
    RECONNECTION_ADDRESS("00002a03-0000-1000-8000-00805f9b34fb", GattAttributeType.CHARACTERISTIC),
    MANUFACTURER_NAME_STRING("00002a04-0000-1000-8000-00805f9b34fb", GattAttributeType.CHARACTERISTIC),
    SERVICE_CHANGED("00002a05-0000-1000-8000-00805f9b34fb", GattAttributeType.CHARACTERISTIC),
    ALERT_LEVEL("00002a06-0000-1000-8000-00805f9b34fb", GattAttributeType.CHARACTERISTIC),
    PNP_ID("00002a50-0000-1000-8000-00805f9b34fb", GattAttributeType.CHARACTERISTIC),

    // Descriptors (https://www.bluetooth.com/specifications/gatt/descriptors/)
    CLIENT_CHARACTERISTIC_CONFIGURATION("00002902-0000-1000-8000-00805f9b34fb", GattAttributeType.DESCRIPTOR),

    // 0x18... are normalized services (https://www.bluetooth.com/specifications/gatt/services/)
    GENERIC_ACCESS("00001800-0000-1000-8000-00805f9b34fb", GattAttributeType.SERVICE),
    GENERIC_ATTRIBUTE("00001801-0000-1000-8000-00805f9b34fb", GattAttributeType.SERVICE),
    IMMEDIATE_ALERT("00001802-0000-1000-8000-00805f9b34fb", GattAttributeType.SERVICE),
    LINK_LOSS("00001803-0000-1000-8000-00805f9b34fb", GattAttributeType.SERVICE),
    TX_POWER("00001804-0000-1000-8000-00805f9b34fb", GattAttributeType.SERVICE),
    DEVICE_INFORMATION("0000180a-0000-1000-8000-00805f9b34fb", GattAttributeType.SERVICE),
    SCAN_PARAMETER("00001813-0000-1000-8000-00805f9b34fb", GattAttributeType.SERVICE),

    /////////////////////////////////////////////
    // Terminal IO non-standard interface

    TERMINAL_IO_SERVICE("0000fefb-0000-1000-8000-00805f9b34fb", GattAttributeType.SERVICE),
    TERMINAL_IO_UART_DATA_RX("00000001-0000-1000-8000-008025000000", GattAttributeType.CHARACTERISTIC),
    TERMINAL_IO_UART_DATA_TX("00000002-0000-1000-8000-008025000000", GattAttributeType.CHARACTERISTIC),
    TERMINAL_IO_UART_CREDITS_RX("00000003-0000-1000-8000-008025000000", GattAttributeType.CHARACTERISTIC),
    TERMINAL_IO_UART_CREDITS_TX("00000004-0000-1000-8000-008025000000", GattAttributeType.CHARACTERISTIC);


    public GattAttributeType type;
    public UUID id;

    GattAttribute(String id, GattAttributeType type) {
        this.type = type;
        this.id = UUID.fromString(id);
    }

    public static String getAttributeName(UUID id) {
        GattAttribute[] all = GattAttribute.values();
        for (GattAttribute gattAttribute : all) {
            if (gattAttribute.id.equals(id)) {
                return gattAttribute.name();
            }
        }
        return "unknown";
    }

    public static GattAttribute getAttribute(UUID id) {
        GattAttribute[] all = GattAttribute.values();
        for (GattAttribute gattAttribute : all) {
            if (gattAttribute.id.equals(id)) {
                return gattAttribute;
            }
        }
        return null;
    }
}
