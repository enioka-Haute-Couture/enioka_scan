# The `BarcodePairing` interface

This interface is extended by the `Scanner` interface, and contains features interfaces that
can be implemented by some scanners provider. It is used to pair a scanner with a device,
and to manage the pairing process, by generating specific barcodes that can be scanned by the
device.

:::{method} getPairingSupport() -> WithBarcodePairingSupport

Returns the `WithBarcodePairingSupport` interface, if the scanner supports barcode pairing. If not,
returns `null`.

:::

## The `WithBarcodePairingSupport` interface

:::{method} getPairingBarcode(PairingType type) -> Bitmap

Generates a barcode that can be scanned by the device to pair it with the scanner. The type of
pairing is specified by the `PairingType` enum.

:returns: A `Bitmap` containing the barcode to scan.

:::

:::{method} getPairingBarcode() -> Bitmap

Generates a barcode that can be scanned by the device to pair it with the scanner.

:returns: A `Bitmap` containing the barcode to scan.

:::

:::{method} activateBluetoothBarcode() -> Bitmap

Generates a barcode that can be scanned by the device to activate the bluetooth on the scanner.

:returns: A `Bitmap` containing the barcode to scan.

:::

:::{method} defaultSettingsBarcode(Defaults type) -> Bitmap

Generates a barcode that can be scanned by the device to reset the scanner to its default settings.
The type of default settings is specified by the `Defaults` enum.

:returns: A `Bitmap` containing the barcode to scan.

:::

:::{method} zebraSetBluetoothHostBarcode() -> Bitmap

Generates a barcode that can be scanned by the device to set the bluetooth host on the scanner.
Only available for Zebra bluetooth scanners providers.

:returns: A `Bitmap` containing the barcode to scan.

:::

:::{method} zebraConnectToAddressBarcode() -> Bitmap

Generates a barcode that can be scanned by the device to connect to a specific bluetooth address.
Only available for Zebra bluetooth scanners providers.

:returns: A `Bitmap` containing the barcode to scan.

:::

If a scanner or provider does not support a specific feature, the method will return `null`.

## The `WithBarcodePairingSupport` interface

:::{method} isNfcEnabled() -> boolean

Returns whether the scanner supports NFC pairing.

:returns: `true` if the scanner supports NFC pairing, `false` otherwise.

:::

:::{method} askNfcActivation() -> void

Asks the scanner to activate NFC pairing through an intent.

:::

## Usage

```java
import com.enioka.scanner.Scanner;

public class Example {
    public static void main(String[] args) {
        Scanner scanner = ...;
        WithBarcodePairingSupport pairingSupport = scanner.getBarcodePairingSupport();

        if (pairingSupport != null) {
            Bitmap pairingBarcode = pairingSupport.getPairingBarcode();
            // Use the barcode to pair the scanner with the device
        }
    }
}
```

Same things can be done while working with a `BtSppScannerProvider`:
```java
import com.enioka.scanner.Scanner;

public class Example {
    public static void main(String[] args) {
        BtSppScannerProvider provider = ...;
        WithBarcodePairingSupport pairingSupport = provider.getBarcodePairingSupport();

        if (pairingSupport != null) {
            Bitmap pairingBarcode = pairingSupport.getPairingBarcode();
            // Use the barcode to pair the scanner with the device
        }
    }
}
```
