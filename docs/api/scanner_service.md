# The ScannerService API

The `ScannerService` is responsible for handling the search of an available scanner device and SDK 
for the end user. Once any is found, it can be made available to the user in the form of a 
[`Scanner`](scanner) instance. 

This page regroups all service-related interfaces and helper classes.

## The `ScannerServiceApi` interface

The `ScannerServiceApi` interface exposes methods that give the user control on the scanner search
and on the handling of found scanners.

:::{method} registerClient(ScannerClient client) -> void

Registers a [`ScannerClient`](#the-scannerclient-interface) implementation to the service. The 
service will call this client's callbacks whenever applicable.

:param ScannerClient client: The client to register.
:::

:::{method} unregisterClient(ScannerClient client) -> void

Unregisters a [`ScannerClient`](#the-scannerclient-interface) implementation from the service. The 
service will no no longer call this client's callbacks.

:param ScannerClient client: The client to unregister.
:::

:::{method} restartScannerDiscovery() -> void

Disconnects all currently-connected [scanners](scanner.md#the-scanner-interface) then starts the
initialization process all over again.
:::

:::{method} restartProviderDiscovery() -> void

Clears the cache of discovered [scanner providers](scanner_provider.md#the-scannerprovider-api) and 
re-starts their discovery. This is a costly operation.
:::

:::{method} getAvailableProviders() -> List<String>

:returns: The list of keys of currently-available scanner providers, useful to tune scanner search 
options at runtime.
:::

:::{method} getConnectedScanners() -> List<Scanner>

:returns: The list of keys of currently-connected scanners, allowing their direct manipulation 
through the [`Scanner` API](scanner.md#the-scanner-interface).
:::

:::{method} updateScannerSearchOptions(ScannerSearchOptions newOptions) -> void

Updates the service's scanner search options.

:param ScannerSearchOptions newOptions: The new options to use during future searches.
:::

:::{method} pause() -> void
:no-index:

Calls the [`Scanner.pause()`](scanner.md#the-scanner-interface) method for all connected scanners.
:::

:::{method} resume() -> void
:no-index:

Calls the [`Scanner.resume()`](scanner.md#the-scanner-interface) method for all connected scanners.
:::

:::{method} disconnect() -> void
:no-index:

Calls the [`Scanner.disconnect()`](scanner.md#the-scanner-interface) method for all connected 
scanners.

Should scanners be needed again, `restartScannerDiscovery()` will need to be
called first.
:::

## The `ScannerClient` interface

The Scanner Service, like any service, works as a standalone unit in the background. While it can
be manipulated through the `ScannerServiceApi` interface, it needs to register a series of callbacks
to send information to your application. These callbacks are grouped in the `ScannerClient` 
interface, which should be implemented by the part of your code responsible for handling the service
lifecycle and barcode reads.

:::{seealso}
This interface extends `ScannerStatusCallback` and needs to implement its method, see more in the 
[callbacks documentation](scanner_callbacks.md#the-scannerstatuscallback-interface-and-scannerstatuscallbackproxy-class).
:::

:::{method} onScannerInitEnded(int count) -> void

This callback is used once the initialization of scanners is over. It will be called 
retro-actively on new clients if at least one scanner initialization happened.

:param int count: The amount of initialized scanners.
:::

:::{method} onProviderDiscoveryEnded() -> void

This callback is used once the discovery of scanner providers is over and scanners are ready
to be used. It will be called retro-actively on new clients if at least one provider discovery 
happened.
:::

:::{method} onData(List<Barcode> data) -> void

This callback is used whenever a connected scanner successfully reads data.

:param List<Barcode> data: The barcodes read by the scanner.
:::

## The `ScannerSearchOptions` class

The `ScannerSearchOptions` provides a clear list of options used to refine the scanner search. Each
attribute from `ScannerSearchOptions` corresponds to an intent extra used by `ScannerServiceApi`.

:::{method} defaultOptions() -> ScannerSearchOptions

:returns: an instance of `ScannerSearchOptions` using the default values described below. Equivalent
    to a default constructor call.
:::

:::{cpp:var} boolean startSearchOnServiceBind = true

If true, the service will start searching for scanners immediately upon binding.

If false, the service will only discover available providers without requesting scanners 
immediately.

Corresponds to the `ScannerServiceApi.EXTRA_START_SEARCH_ON_SERVICE_BIND` extra.
:::

:::{cpp:var} boolean waitDisconnected = true

If a scanner is known but not currently available, wait for it. If false, consider the scanner
unavailable immediately.

Corresponds to the `ScannerServiceApi.EXTRA_SEARCH_WAIT_DISCONNECTED_BOOLEAN` extra.
:::

:::{cpp:var} boolean returnOnlyFirst = true

If true, will only connect to the first scanner available (or reporting it may become available if 
`waitDisconnected` is true).

If false, all available scanners will be used.

Corresponds to the `ScannerServiceApi.EXTRA_SEARCH_RETURN_ONLY_FIRST_BOOLEAN` extra.
:::

:::{cpp:var} boolean useBlueTooth = true

If true, bluetooth devices will be searched for compatible scanners.

Corresponds to the `ScannerServiceApi.EXTRA_SEARCH_ALLOW_BT_BOOLEAN` extra.
:::

:::{cpp:var} boolean allowIntentDevices = false

If true, providers using another application for controlling scanners will be allowed.
This is important as most of the time, it is impossible to say if these apps are actually installed,
and therefore it is impossible to detect the presence of an actual scanner or not.

Corresponds to the `ScannerServiceApi.EXTRA_SEARCH_ALLOW_INTENT_BOOLEAN` extra.

::::{seealso}

Check [`IntentScannerProvider`](scanner_provider.md#the-intentscannerprovider-abstract-class) for 
more details on how this type of device is checked for compatibility.
::::

:::

:::{cpp:var} boolean allowLaterConnections = true

If true, some providers may retrieve scanners after initial search is over.

Mostly used to let bluetooth providers keep listening for incoming devices after search.

Corresponds to the `ScannerServiceApi.EXTRA_SEARCH_KEEP_SEARCHING_BOOLEAN` extra.
:::

:::{cpp:var} boolean allowInitialSearch = true

If true, some providers may retrieve scanners while the initial search is taking place.

Mostly used to let bluetooth providers contact known devices during search.

Corresponds to the `ScannerServiceApi.EXTRA_SEARCH_ALLOW_INITIAL_SEARCH_BOOLEAN` extra.
:::

:::{cpp:var} boolean allowPairingFlow = false

If true, the providers which needs a pairing done by their own SDK (for example, BLE on-the-fly 
pairing) will be allowed to do so.

Corresponds to the `ScannerServiceApi.EXTRA_SEARCH_ALLOW_PAIRING_FLOW_BOOLEAN` extra.
:::

:::{cpp:var} Set<String> allowedProviderKeys = null

Restricts the search to this list of providers. Ignored if null or empty.

Mainly used to whitelist known expected devices, or to refine the scanner search using the results
of [`ScannerServiceApi.getAvailableProviders()`](#the-scannerserviceapi-interface).

Corresponds to the `ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY` extra.
:::

:::{cpp:var} Set<String> excludedProviderKeys = null

The providers inside this list will never be used. Ignored if null or empty.

Mainly used to blacklist known unwanted devices, or to refine the scanner search using the results
of [`ScannerServiceApi.getAvailableProviders()`](#the-scannerserviceapi-interface).

Corresponds to the `ScannerServiceApi.EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY` extra.
:::

:::{cpp:var} Set<String> symbologySelection = null

An array of [`BarcodeType`](others.md#the-barcodetype-enum) to activate. Ignored if null or empty.

Corresponds to the `ScannerServiceApi.EXTRA_SYMBOLOGY_SELECTION` extra.
:::

It also exposes methods to simplify the process of converting options into Intent extras, and vice 
versa.

:::{method} fromIntentExtras(Intent intent) -> ScannerSearchOptions

Sets the option's attributes based on a given intent's extras. If an extra is missing, the current
value of the attribute is preserved.

:param Intent intent: The Intent to read.
:returns: this
:::

:::{method} toIntentExtras(Intent intent) -> void

Updates an intent's extras based on the current option's attributes.

:param Intent intent: The Intent to update.
:::


## The `ScannerServiceBinderHelper` class

To make the process of binding your application to the Scanner Service easier, the
`ScannerServiceBinderHelper` class exposes a series of methods you can use.

:::{method} defaultServiceConfiguration() -> Bundle

Static method.

:returns: The default service search options used everywhere else in the library. A new bundle is
returned on each call.
:::

:::{method} bind(Application a, Bundle extra) -> ScannerServiceBinderHelper

Static method. Binds to the Scanner Service using the `defaultServiceConfiguration()` extras.

:param Application a: The application to which the service will be bound.
:param Bundle extra: Intent extras passed to the service on bind. See 
    [`ScannerSearchOptions.toIntentExtras()`](#the-scannersearchoptions-class).
:returns: The `ScannerServiceBinderHelper` instance that bound to the service.
:::

:::{method} bind(Application a) -> ScannerServiceBinderHelper
:no-index:

Static method. Binds to the Scanner Service using the `defaultServiceConfiguration()` extras.

:param Application a: The application to which the service will be bound.
:returns: The `ScannerServiceBinderHelper` instance that bound to the service.
:::

:::{method} getScannerService() -> ScannerServiceApi

:returns: The `ScannerServiceApi` instance bound to this `ScannerServiceBinderHelper` instance.
:throws IllegalStateException: If no service is bound when called.
:::

:::{method} disconnect() -> void
:no-index:

Disconnects the currently-connected service from this `ScannerServiceBinderHelper` instance.
:::