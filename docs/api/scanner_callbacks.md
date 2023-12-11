# API callbacks

Most **enioka Scan** actions use callbacks to communicate their result to your code. In the case of
scanner-related callbacks, proxy classes exist to wrap callback implementations and ensure their
methods are called on the UI thread.

This page regroups all callback interfaces from **enioka Scan**.

## The `ScannerCommandCallback` interface and `ScannerCommandCallbackProxy` class

Callback used to inform of the result of scanner commands, usually optional.

:::{method} onSuccess() -> void

Called when the command was completed, and if it received a positive answer (when applicable).
:::

:::{method} onFailure() -> void

Called when the command failed, either through or SDK error or negative answer (when applicable).
:::

:::{method} onTimeout() -> void

Called when the command's expected answer did not arrive in time (usually only with BT scanners).
:::

## The `ScannerConnectionHandler` interface and `ScannerConnectionHandlerProxy` class

Callback used during the scanner search to retrieve created 
[`Scanner` instances](scanner.md#the-scanner-interface).

:::{method} scannerConnectionProgress(String providerKey, String scannerKey, String message) -> void

A SDK-specific localized message signaling a change during the search for a scanner.

:param String providerKey: The identifier of the provider/SDK
:param String scannerKey: A unique identifier for the scanner being connected
:param String message: An SDK-specific message (localized)
:::

:::{method} scannerCreated(String providerKey, String scannerKey, Scanner s) -> void

Called when a scanner was found and created. Depending on
[`ScannerSearchOptions.returnOnlyFirst`](scanner_service.md#the-scannersearchoptions-class) may be 
called multiple times.

:param String providerKey: The identifier of the provider/SDK
:param String scannerKey: A unique identifier for the scanner being connected
:param Scanner s: The scanner instance
:::

:::{method} noScannerAvailable() -> void

Called when there is no scanner available on the device. `endOfScannerSearch()` is always called
after this.
:::

:::{method} endOfScannerSearch() -> void

Called when the search for scanners in the different providers is over.
:::

## The `ScannerDataCallback` interface and `ScannerDataCallbackProxy` class

Callback used to handle the data read by the scanner.

:::{method} onData(Scanner s, List<Barcode> data) -> void
:no-index:

Called whenever data is read by the scanner.

:param Scanner s: The scanner from which data was read
:param List<Barcode> data: The data read by the scanner
:::

## The `ScannerInitCallback` interface and `ScannerInitCallbackProxy` class

Callback handling scanner init events.

:::{method} onConnectionSuccessful(Scanner s) -> void

Called whenever a scanner successfully connected.

:param Scanner s: The connected scanner.
:::

:::{method} onConnectionFailure(Scanner s) -> void

Called whenever a scanner could not connect.

:param Scanner s: The disconnected scanner.
:::

## The `ScannerStatusCallback` interface and `ScannerStatusCallbackProxy` class

:::{cpp:enum} Status

This enum represents the scanner's lifecycle, as well as the current status of the scanner, scanner
provider and scanner service's search. Some elements may not be used depending on which scanner SDK
is used.

:WAITING: The scanner provider is waiting for a connection.
:CONNECTING: The scanner is in the process of connecting.
:RECONNECTING: The scanner disconnected but is trying to reconnect.
:CONNECTED: The scanner has finished connecting.
:INITIALIZING: The scanner is in the process of initializing.
:INITIALIZED: The scanner has finished initializing.
:READY: The scanner is ready to scan and waiting to be used.
:SCANNING: The scanner is in the process of scanning.
:PAUSED: The scanner is connected, initialized and enabled but not ready to scan.
:DISABLED: The scanner is connected and initialized but has been disabled and cannot be used.
:FAILURE: The scanner is no longer available after a critical error occurred, usually during 
    connection or initialization.
:DISCONNECTED: The scanner disconnected and can no longer be used.
:UNKNOWN: The scanner is in an unknown status.
:SERVICE_PROVIDER_SEARCH_OVER: ScannerService's provider search is over.
:SERVICE_SDK_SEARCH_OVER: ScannerService's SDK search is over.
:SERVICE_SDK_SEARCH_NOCOMPATIBLE: ScannerService's SDK search found no scanner.

:::

:::{method} onStatusChanged(Scanner scanner, Status newStatus) -> void

Called whenever the scanner has changed status.
:param Scanner scanner: The updated scanner. May be null if the scanner has not yet been created.
:param Status newStatus: The new scanner status.
:::

## The `ProviderDiscoveredCallback` interface and `ProviderDiscoveredCallbackProxy` class

Callback used during provider discovery (before scanner search).

:::{method} onDiscoveryDone() -> void

Called once all providers are discovered.
:::

## The `ScannerProvider.ProviderCallback` interface

Callback used by a scanner provider to inform the search process whether it is available and
able to connect to a scanner.

:::{method} onScannerCreated(String providerKey, String scannerKey, Scanner s) -> void

Called when the provider has finished creating a scanner.

:param String providerKey: The identifier of the provider/SDK
:param String scannerKey: A unique identifier for the scanner being connected
:param Scanner s: The scanner instance
:::

:::{method} connectionProgress(String providerKey, String scannerKey, String message) -> void

Send a localized status message to the end user.

:param String providerKey: The identifier of the provider/SDK
:param String scannerKey: A unique identifier for the scanner being connected
:param String message: The message (localized)
:::

:::{method} onProviderUnavailable(String providerKey) -> void

Called when the provider has determined it cannot run (is not available) on this device and should 
not be revived.

:param String providerKey: The identifier of the provider/SDK
:::

:::{method} onAllScannersCreated(String providerKey) -> void

Called if the provider can run, and all scanners have already been created.

:param String providerKey: The identifier of the provider/SDK
:::

:::{method} isAlreadyConnected(BluetoothDevice device) -> void

Called if a bluetooth device was already taken by a previous provider.

:param BluetoothDevice device: The busy bluetooth device
:::

## The `BtSppScannerProvider.ManagementCallback` interface

Callback used by bluetooth scanner providers to inform the master bluetooth provider that it cannot
manage the device it was tested for.

:::{method} canManage(Scanner libraryScanner) -> void

Called if the provider can manage the device.

:param Scanner libraryScanner: The corresponding 
[`Scanner` instance](scanner.md#the-scanner-interface).
:::

:::{method} cannotManage() -> void

Called if the provider can not manage the device.
:::

## The `CameraBarcodeScanView.ResultHandler` interface

Callback used by the camera reader when a barcode is read from the camera feed.

:::{method} handleScanResult(String result, BarcodeType type) -> void

:param String result: The barcode data
:param BarcodeType type: The barcode symbology
:::