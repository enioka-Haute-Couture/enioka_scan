# Dependencies and compatible devices

**enioka Scan** is split over multiple AAR dependencies, all available through the Maven Central
repository, allowing you to only include the compatibility layers you need.

The compatibility matrix at the end of this page explains more clearly what devices are supported by
each scanner provider, and how each device is detected by the library.

While most providers are standalone and include their own open-source SDK (artifact marked 'os',
open source), some providers (artifact marked 'cs', closed source) still require proprietary code in
order to work with their device. Depending on devices, this code can take the form of a JAR or AAR
file made available to you by the vendor, or a companion app, service or SDK installed separately on
the device. In the case of a library file, you will need to manually add the archive to your
application's classpath. If a required SDK is missing, the provider will simply not show up as
compatible during use.

## enioka Scan artefacts

Each artefact can be imported to your project as `com.enioka.scanner:<artefact>:<version>:aar`.

### `enioka-scan-core`

Core components of the library, **required** for it to work. It includes the scanner APIs, default
scanning activity and service that manages the search and lifecycle of the scanners.

### `provider-os-camera`

Includes the `CAMERA_SCANNER` provider. It includes camera-as-a-scanner functionality, and
default camera layout for the scanner activity.

### `provider-os-athesi-e5l`

Includes the `AthesiE5LProvider` provider, adding support for Athesi E5L integrated devices.

This provider works only when the device name is strictly `RD50TE`.

### `provider-os-athesi-spa43`

Includes the `AthesiHHTProvider` provider, adding support for Athesi SPA43 integrated devices.

This provider works only when the device name is strictly `SPA43LTE`.

### `provider-os-bluebird`

Includes the `BluebirdProvider` provider, adding support for Bluebird integrated devices.

This provider requires the Bluebird service to be installed and enabled on your device, listening for the `kr.co.bluebird.android.bbapi.action.BARCODE_OPEN` intent.

### `provider-os-generalscan-bt`

Includes the `BT_GeneralScanProvider` provider, adding support for GeneralScan bluetooth ring.

This provider requires the bluetooth device to respond to the "Get device ID" command.

### `provider-os-honeywell-bt`

Includes the `BT_HoneywellOssSppProvider` provider, adding support for Honeywell bluetooth scanners.

This provider requires the bluetooth device to respond to the "Get firmware" command.

### `provider-os-honeywell-integrated`

Includes the `HoneywellOssIntegratedProvider` provider, adding support for Honeywell integrated
devices.

This provider requires the Honeywell service `com.honeywell.decode.DecodeService` to be installed
and enabled on your device.

### ~~`provider-cs-postech`~~

Includes the `BT_PostechProvider` provider, adding support for Postech ring scanners.

This provider could not be properly tested and may not work as expected.

### `provider-os-zebra-bt`

Includes the `BT_ZebraOssSPPProvider` and `BT_ZebraOssATTProvider` providers, adding support for
Zebra bluetooth scanners using the SSI protocol (respectively classic and low energy).

`BT_ZebraOssSPPProvider` requires the bluetooth device to respond to the "CAPABILITIES_REQUEST"
command and be a Bluetooth Classic (non-BLE) device.

`BT_ZebraOssATTProvider` requires the bluetooth device to respond to the "CAPABILITIES_REQUEST"
command and be a Bluetooth Low Energy (BLE) device.

### `provider-cs-proglove`

Includes the `ProgloveProvider` provider, adding support for Proglove devices interfacing with their
application.

This provider requires the Proglove application package, `de.proglove.connect`, to be installed and
enabled on your device.

### `provider-cs-honeywell`

Includes the `HONEYWELL_AIDC` provider, adding support for Honeywell AIDC (Intermec) integrated
devices.

This provider requires the "Android data collection" (AIDC), or Intermec, SDK, which needs to be
installed either in your application's classpath or device to resolve `com.honeywell.aidc.`
packages.

This provider also requires the `com.honeywell.decode.DecodeService` Android service to be installed
and enabled on your device.

### `provider-cs-koamtac`

Includes the `Koamtac` provider, adding support for Koamtac KDC-family bluetooth
scanners.

This provider requires an external SDK provided by Koamtac, which needs to be included in your
application's classpath to resolve `koamtac.kdc.sdk.*` packages.

### `provider-cs-m3`

Includes the `M3RingScannerProvider` provider, adding support for M3 ring devices.

This provider requires an external SDK, provided by M3Mobile, which needs to be included in your
application's classpath to resolve `com.m3.ringscannersdk.*` packages.

This provider also requires the `Ring Scanner` companion app, which should be installed and enabled
on your device to resolve `com.m3.ringscanner.*` packages.

### `provider-cs-zebra`

Includes the following providers:

- `BtZebraProvider`: Support for most Zebra bluetooth devices.

  This provider requires an external SDK, provided by Zebra, which needs to be included in your
  application's classpath to resolve `com.zebra.scannercontrol.*` packages.

- `Zebra EMDK`: Support for Zebra EMDK integrated devices.

  This provider requires an external SDK, which should be installed on your device by default, to
  resolve `com.symbol.emdk.*` packages.

### `provider-os-zebra-dw`

Includes the `ZebraDwProvider` provider, adding support for most integrated devices supporting
the Zebra Datawedge service (TC26, TC27, TC55...).

> Warning: there are multiple different providers that can manage those devices! Take care only to
> have the one you want enabled, using the include or exclude provider search parameters.

This provider requires the Datawedge service, in the form of the `com.symbol.datawedge` application
package, to be installed and enabled on your device.

## Compatibility matrix

This table aggregates all currently-available scanner providers and lists which devices they
support, their external requirements if applicable, and how each provider detects compatible
devices.

:::{table}
:widths: auto
:align: center

| Artefact                           | Provider name                    | Supported devices                                               | Tested devices                     | Device type   | External requirements                                          | Device compatible if                                                                                                              |
|------------------------------------|----------------------------------|-----------------------------------------------------------------|------------------------------------|---------------|----------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| `enioka-scan-core`                 | no provider                      | Does not support any device alone, it is a required dependency. |                                    |               |                                                                |                                                                                                                                   |
| `provider-os-camera`               | no provider                      | Any device with a camera                                        | Smartphones, integrated devices... | Camera        |                                                                | Device has a camera                                                                                                               |
| `provider-os-athesi-e5l`           | `AthesiE5LProvider`              | Athesi E5L                                                      | Athesi E5L                         | Integrated    |                                                                | Device name is strictly `RD50TE`                                                                                                  |
| `provider-os-athesi-spa43`         | `AthesiHHTProvider`              | Athesi SPA43                                                    | Athesi SPA43                       | Integrated    |                                                                | Device name is strictly `SPA43LTE`                                                                                                |
| `provider-os-bluebird`             | `BluebirdProvider`               | Bluebird integrated scanners                                    | Bluebird EF500                     | Integrated    | Bluebird service (should be preinstalled on device)            | Intent `kr.co.bluebird.android.bbapi.action.BARCODE_OPEN` has a listener                                                          |
| `provider-os-generalscan-bt`       | `BT_GeneralScanProvider`         | GeneralScan bluetooth ring scanners                             | GeneralScan GS R5000BT             | BT Classic    |                                                                | Bluetooth device responds to "Get device ID" command                                                                              |
| `provicer-os-honeywell-bt`         | `BT_HoneywellOssSppProvider`     | Honeywell bluetooth scanners                                    | Honeywell Voyager 1602g            | BT Classic    |                                                                | Bluetooth device responds to "Get firmware" command                                                                               |
| `provider-os-zebra-bt`             | `BT_ZebraOssSPPProvider`         | Zebra bluetooth scanners using the SSI protocol                 | Zebra RS5100                       | BT Classic    |                                                                | Bluetooth device is not BLE and responds to "CAPABILITIES_REQUEST" command                                                        |
| `provider-os-zebra-bt`             | `BT_ZebraOssATTProvider`         | Zebra bluetooth scanners using the SSI protocol                 | Zebra RS5100                       | BT Low Energy |                                                                | Bluetooth device is BLE and responds to "CAPABILITIES_REQUEST" command                                                            |
| `provider-os-honeywell-integrated` | `HoneywellOssIntegratedProvider` | Honeywell integrated scanners                                   | Honeywell EDA52                    | Integrated    | Honeywell service (should be preinstalled on device)           | Intent `com.honeywell.decode.DecodeService` has a listener                                                                        |
| `provider-cs-proglove`             | `ProgloveProvider`               | Proglove devices interfacing with their application             | Proglove Glove Mark II             | BT Low Energy | Proglove application                                           | Application package `de.proglove.connect` exists                                                                                  |
| `provider-cs-honeywell`            | `HONEYWELL_AIDC`                 | Honeywell AIDC / Intermec integrated devices                    | Honeywell EDA50                    | Integrated    | AIDC SDK, Honeywell service (should be preinstalled on device) | Intent `com.honeywell.decode.DecodeService` has a listener and AIDC SDK exists                                                    |
| `provider-cs-koamtac`              | `Koamtac`                        | Koamtac bluetooth KDC devices                                   | Koamtac KDC180                     | BT Low Energy | Koamtac SDK                                                    | Class `koamtac.kdc.sdk.KDCReader` exists and scanner device is found                                                              |
| `provider-cs-m3`                   | `M3RingScannerProvider`          | M3Mobile bluetooth ring scanners                                | M3 Ring Scanner                    | BT Classic    | Ring Scanner SDK, Ring Scanner application                     | Class `com.m3.ringscannersdk.RingScannerService` exists, application package `com.m3.ringscanner` exists, scanner device is found |
| `provider-cs-zebra`                | `BtZebraProvider`                | Zebra bluetooth scanners                                        | Zebra RS6000, RS5100               | BT Classic    | Zebra SDK                                                      | Class `com.zebra.scannercontrol.SDKHandler` exists and scanner device is found                                                    |
| `provider-cs-zebra`                | `Zebra EMDK`                     | Zebra EMDK integrated scanners                                  | Zebra TC25                         | Integrated    | Zebra EMDK SDK (should be preinstalled on device)              | Class `com.symbol.emdk.EMDKManager` exists                                                                                        |
| `provider-os-zebra-dw`             | `ZebraDwProvider`                | Any device compatible with Zebra Datawedge                      | Zebra TC25, Zebra TC27             | Integrated    | Zebra Datawedge service (should be preinstalled on device)     | Application package `com.symbol.datawedge` exists                                                                                 |

:::

## Supported barcode types

| Barcode type         | provider-os-athesi-spa43 | provider-os-bluebird | provider-os-honeywell-integrated | provider-cs-proglove | provider-cs-honeywell | provider-cs-koamtac | provider-cs-zebra | provider-os-zebra-dw | camera Zxing  | camera Zbar |
|----------------------|--------------------------|----------------------|----------------------------------|----------------------|-----------------------|---------------------|-------------------|----------------------|---------------|-------------|
| CODE 39              | ✓                        | ✓                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| CODE 39 FULL ASCII   | x                        | x                    | x                                | x                    | ✓                     | x                   | ✓                 | x                    | x             | x           |
| CODE 11              | ✓                        | x                    | ✓                                | x                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| CODE 93              | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| CODE 128             | ✓                        | ✓                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| CODABAR              | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| INT25                | ✓                        | ✓                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| DIS25                | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| QRCODE               | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| UPCA                 | x                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| UPCE                 | x                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| EAN8                 | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| EAN13                | ✓                        | ✓                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| DATAMATRIX           | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | x           |
| GRIDMATRIX           | ✓                        | x                    | ✓                                | x                    | x                     | x                   | x                 | ✓                    | x             | x           |
| MAXICODE             | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | x           |
| GS1 DATABAR EXPANDED | x                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| GS1 DATABAR          | x                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| GS1 DATABAR LIMITED  | x                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| ISBN 10              | x                        | x                    | x                                | x                    | x                     | x                   | x                 | x                    | x             | ✓           |
| GS1 128              | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| MSI                  | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| PDF417               | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| AZTEC                | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | x           |
| AZTEC RUNNER         | x                        | x                    | x                                | ✓                    | x                     | ✓                   | ✓                 | x                    | x             | x           |
| JAPAN POSTAL         | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| DUTCH POSTAL         | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| UK POSTAL            | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| AUSTRALIA POSTAL     | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| KOREAN POSTAL        | ✓                        | x                    | ✓                                | x                    | ✓                     | ✓                   | x                 | ✓                    | x             | x           |
| CHINA POSTAL         | ✓                        | x                    | ✓                                | x                    | ✓                     | ✓                   | ✓                 | x                    | x             | x           |
| CANADIAN POSTAL      | x                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| HAN XIN              | ✓                        | x                    | ✓                                | x                    | ✓                     | ✓                   | x                 | ✓                    | x             | x           |

## Known issues

### `Zebra TC-25`

The Zebra TC-25 device may experience issues with the camera preview, which may not display
correctly on screen orientation changes. One solution is to call `orientationChanged()`, when
switching between portrait and landscape modes, on the `CameraBarcodeScanView` instance. Sometimes,
the camera may be corrupted, and the preview will not display correctly. 

### `Honeywell EDA52`

The Honeywell EDA52 device may experience issues with the camera scanner, which may not be able to
detect barcodes at all. This is probably due to some internal services not allowing the camera to
be used correctly by the library. On the other hand, the integrated scanner works correctly.