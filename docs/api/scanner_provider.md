# The ScannerProvider API

:::{admonition} WIP
:class: attention

This documentation is a work in progress.
:::

The Scanner Provider is the entrypoint of an SDK. Its purpose is to check if the SDK is able to 
interact with a device, and handle the [`Scanner`](scanner.md) creation. 

In most cases, users will not need to interact with a provider directly, but adding compatibility to
more devices will require adding a new provider.

## The `ScannerProvider` interface

The core methods of a provider common between, all types of devices.

:::{method} getScanner(Context ctx, ProviderCallback cb, ScannerSearchOptions options) -> void

This method will make the provider check whether it is compatible with any scanning device while
matching requirements imposed by the search options. If it is able to connect to a device, the
provider will use the callback to notify its availability and return a [`Scanner`](scanner.md)
instance.

:param Context ctx: The application context.
:param ProviderCallback cb: The [`ProviderCallback`](scanner_callbacks.md#providerCallback) used
    to notify the Scanner Service whether this SDK is available (as in, is able to connect to any 
    scanning device), and the current status of [`Scanner`](scanner.md) creation.
:param ScannerSearchOptions options: The search options the provider may use to further refine its
    compatibility checks.
:::

:::{method} getKey() -> String

:returns: The unique key which identifies this provider.
:::

:::{seealso}

Check the documentation of the [`ProviderCallback`](scanner_callbacks.md#providerCallback) for 
details on how the provider will communicate its status with the Scanner Service.
:::

## The `IntentScannerProvider` abstract class

In order to facilitate discovery for integrated devices, the `IntentScannerProvider` abstract
class provides an implementation of the `ScannerProvider.getScanner()` to check various Android
mechanisms and figure out if the current device is compatible with the SDK.

A provider for an integrated scanner should extend this class and override the following methods:

:::{method} createNewScanner(Context ctx, ScannerSearchOptions options) -> Scanner

This method is called by `ScannerProvider.getScanner()` to create the [`Scanner`](scanner.md) 
instance.

:param Context ctx: The application context.
:param ScannerSearchOptions options: The search options the provider used to confirm compatibility.
:returns: The [`Scanner`](scanner.md) instance of the implementation used by this SDK.
:::

:::{method} configureProvider() -> void

This method is called by `ScannerProvider.getScanner()` to set what should be tested to know if the
device is compatible with an SDK. There are four different mechanisms that can be tested, if 
multiple of them are set, ***all*** must be valid for the SDK to be considered compatible.
:::

The following variables may be set in `configureProvider()` to influence the compatibility check:

:::{cpp:var} String intentToTest = null

If set, the provider will be compatible only if the given Android Intent has a matching listener
on the device.
:::

:::{cpp:var} List<String> specificDevices = new ArrayList<>(0)

If set, the provider will be compatible only if the value of `android.os.Build.MODEL` exactly
matches one of the values contained in the list.
:::

:::{cpp:var} String appPackageToTest = null

If set, the provider will be compatible only if the given application package name is found and
enabled on the device.
:::

:::{cpp:var} String serviceToTest = null

If set, the provider will be compatible only if the given service package name can be started by the
library.
:::

## The case of Bluetooth, and the `BtSppScannerProvider` interface

Because bluetooth devices require extra steps to detect, the provider discovery for such devices is
a bit different. A single `SerialBtScannerProvider` class implements the `ScannerProvider`
interface to handle system-level bluetooth configuration, and the actual providers for SDKs 
targetting bluetooth devices need to implement the `BtSppScannerProvider` interface.

A bluetooth provider must implement the following methods:

:::{method} getKey() -> String
:no-index:

:returns: The unique key which identifies this provider.
:::

:::{method} canManageDevice(BluetoothScanner device, ManagementCallback callback) -> void

Tests whether a scanner is compatible with the provider. Must complete in under 50ms.

:param BluetoothScanner device: The "raw" bluetooth device for which to test SDK compatibility
:param ManagementCallback callback: The callback used to notify whether the compatibility check
    was successful or not.
:::

:::{method} getInputHandler() -> ScannerDataParser

:returns: The [`ScannerDataParser`](others.md#scannerDataParser) which should be used to parse
    bluetooth messages from compatible devices.
:::

:::{seealso}

Check the documentation of the [`ManagementCallback`](scanner_callbacks.md#managementCallback) 
for details on how the bluetooth provider will communicate its status with the Scanner Service.

Check the documentation of the [`ScannerDataParser`](others.md#scannerDataParser) for
details bluetooth message parsers.
:::