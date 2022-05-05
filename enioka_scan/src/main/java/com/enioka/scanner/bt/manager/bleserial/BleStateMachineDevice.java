package com.enioka.scanner.bt.manager.bleserial;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/**
 * Some devices can actually be seen as state machines, modified on specific stimuli. This interface factors this behaviour.
 */
public interface BleStateMachineDevice {

    /**
     * The different stimuli which can change the state of the state-machine.
     */
    enum BleEventNature {
        DESCRIPTOR_WRITE_SUCCESS, CHARACTERISTIC_WRITE_SUCCESS, CHARACTERISTIC_READ_SUCCESS, CHARACTERISTIC_CHANGED_SUCCESS, RESET
    }

    /**
     * Description of a stimuli.
     */
    class BleEvent {
        static BleEvent RESET_EVENT = new BleEvent(BleEventNature.RESET);

        BleEventNature nature;
        GattAttribute targetAttribute;
        GattAttribute parentAttribute;
        public byte[] data;

        BleEvent(BluetoothGattCharacteristic characteristic, BleEventNature nature) {
            this.nature = nature;
            this.targetAttribute = GattAttribute.getAttribute(characteristic.getUuid());
            this.data = characteristic.getValue();
        }

        BleEvent(BluetoothGattDescriptor descriptor, BleEventNature nature) {
            this.nature = nature;
            this.targetAttribute = GattAttribute.getAttribute(descriptor.getUuid());
            this.parentAttribute = GattAttribute.getAttribute(descriptor.getCharacteristic().getUuid());
            this.data = null;
        }

        private BleEvent(BleEventNature nature) {
            this.nature = nature;
        }
    }

    /**
     * Called when the state machine has received a stimuli and should update its state.
     *
     * @param event the stimuli to interpret.
     */
    void onEvent(BleEvent event);
}
