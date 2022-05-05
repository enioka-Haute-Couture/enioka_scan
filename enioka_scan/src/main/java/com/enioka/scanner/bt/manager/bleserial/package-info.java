/**
 * Most BLE scanners are actually using some sort of serial emulation instead of using GATT attributes as they were intended.
 * This allows device constructors to re-use their existing proprietary serial protocols and have the same logic in BLE or Classic mode.
 * This package contains utilities to help present a BLE device as a stream.
 */
package com.enioka.scanner.bt.manager.bleserial;
