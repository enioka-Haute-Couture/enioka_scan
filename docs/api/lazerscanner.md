# The LazerScanner class

`LazerScanner` is the main scanner factory and the core of the library. It is in charge of finding
scanner providers and scanner devices. Most users will let the 
[`ScannerService`](scanner_service.md#the-scannerserviceapi-interface)
handle its own instance of `LazerScanner`, but should you need it, you can use its static methods
directly.

:::{method} discoverProviders(Context ctx, ProviderDiscoveredCallback cb) -> void

Discovers scanner providers through service intent and retrieves them through reflection.
The providers are cached and do not need to be discovered again unless new entries are expected, it
is a costly operation.

:param Context ctx: a context used to retrieve a `PackageManager`
:param ProviderDiscoveredCallback cb: The callback used whenever a provider is found.
    Automatically wrapped by a `ProviderDiscoveredCallbackProxy`, for which a public overload exists
:::

:::{method} getProviderCache() -> List<String>

:returns: the list of provider keys from the current provider cache, including bluetooth ones if 
    available (the cache needs to have been initialized first).
:::

:::{method} getLaserScanner(Context ctx, ScannerConnectionHandler handler, ScannerSearchOptions options) -> void

Search for new laser scanners with available providers. The scanner is provided through a callback.
There is a specific callback when no scanner is available.

:param Context ctx: a context used to retrieve a `PackageManager`
:param ScannerConnectionHandler handler: The callback used whenever a scanner is found.
    Automatically wrapped by a `ScannerConnectionHandlerProxy`, for which a public overload exists.
:param ScannerSearchOptions options: The options used to refine the search.
:::

:::{seealso}

* The [Callbacks](scanner_callbacks.md) documentation
:::