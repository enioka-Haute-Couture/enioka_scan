package com.enioka.scanner.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

public interface BleStateMachineDevice {

    enum BleEventNature {
        DESCRIPTOR_WRITE_SUCCESS, CHARACTERISTIC_WRITE_SUCCESS, CHARACTERISTIC_READ_SUCCESS, CHARACTERISTIC_CHANGED_SUCCESS, RESET
    }

    class BleEvent {
        public static BleEvent RESET_EVENT = new BleEvent(BleEventNature.RESET);

        public BleEventNature nature;
        public GattAttribute targetAttribute;
        public GattAttribute parentAttribute;
        public byte[] data;

        public BleEvent(BluetoothGattCharacteristic characteristic, BleEventNature nature) {
            this.nature = nature;
            this.targetAttribute = GattAttribute.getAttribute(characteristic.getUuid());
            this.data = characteristic.getValue();
        }

        public BleEvent(BluetoothGattDescriptor descriptor, BleEventNature nature) {
            this.nature = nature;
            this.targetAttribute = GattAttribute.getAttribute(descriptor.getUuid());
            this.parentAttribute = GattAttribute.getAttribute(descriptor.getCharacteristic().getUuid());
            this.data = data;
        }

        private BleEvent(BleEventNature nature) {
            this.nature = nature;
        }
    }

    void onEvent(BleEvent event);
}
