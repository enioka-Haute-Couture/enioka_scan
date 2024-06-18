# The Scanner API

The `Scanner` interface is the central part of the library. It is the piece of code that gives the
user control on the physical scanner devices. All devices have common methods relating to basic
functionalities (reading a barcode) or lifecycle control. Some devices have access to extra features
which are represented here with optional interfaces.

All methods accepting a `ScannerCallback` type as a parameter have a default implementation to 
wrap these callbacks in a Proxy Callbacks to ensure proper jumps to the UI thread. You may also 
directly call the overloaded method with a proxy callback.

:::{seealso}

* The [Callbacks](scanner_callbacks.md) documentation.
:::

## The `Scanner` interface

These methods are guaranteed to be supported by all scanning devices.

:::{cpp:enum} Mode

This enum represents the scanning mode the device will be set to.

:SINGLE_SCAN: The scanner stops after one successful read. It must be rearmed.
:CONTINUOUS_SCAN: The scanner waits for the result post-treatment and automatically rearms.
:BATCH: The scanner is always ready to scan, not waiting for any result analysis. Results may be 
        sent in batches.
:::

:::{method} initialize(...) -> void

The scanner initializer, called once per application launch. It handles the creation of links
between the library and the physical devices and registers callbacks.

:param Context applicationContext: The android context of the application
:param ScannerInitCallback initCallback: The callback called after the initialization is complete
:param ScannerDataCallback dataCallback: The callback called after data is read
:param ScannerStatusCallback statusCallback: The callback called after a status change
:param Mode mode: The scanning mode
:param Set<BarcodeType> symbologySelection: Which barcode symbologies the scanning device should be 
        set to recognize.
:::

:::{method} getProviderKey() -> String

For logging and sorting purpose, this is the key of the SDK behind this scanner (same as 
[`ScannerProvider.getKey()`](scanner_provider.md#the-scannerprovider-interface))
:::

:::{method} setDataCallBack(ScannerDataCallbackProxy cb) -> void

Changes the scanner's data callback. There is no "non-proxy" overload of this method.

:param ScannerDataCallbackProxy cb: The new callback, called when data is read
:::

:::{method} disconnect(@Nullable ScannerCommandCallback cb) -> void

Disconnect scanner from the App (the app does not need the scanner anymore).

:param ScannerCommandCallback cb: The callback to call with the result of the operation. May be null
        or absent.
:::

:::{method} pause(@Nullable ScannerCommandCallback cb) -> void

The app keeps the scanner for itself but does not need it immediately. It may free whatever 
resources it has, or ignore this call.

:param ScannerCommandCallback cb: The callback to call with the result of the operation. May be null
or absent.
:::

:::{method} resume(@Nullable ScannerCommandCallback cb) -> void

Reverse the effects of `pause()`. The scanner is once again ready to scan after 
this call. The scanner's status callback should be called if needed. Idempotent.

:param ScannerCommandCallback cb: The callback to call with the result of the operation. May be null
or absent.
:::

## Extra functionality support

Some scanners have support for extra features, but it may not be supported by all devices. As such,
these "extra" functionalities are considered supported on a per-SDK level and represented by 
interfaces a Scanner may or may not implement (on top of `Scanner`).

To check if a scanner has access to such functionalities, simply call the `get<feature>Support()`
method. If the feature is supported by the device, the function returns the scanner instance casted
to the corresponding interface. If the feature is not supported, the method returns null. 

For example, if you wanted to programmatically activate the trigger of every scanning device 
connected to the `ScannerService`, you would write the following code:

```java
for (final Scanner s : scannerService.getConnectedScanners()) {
    final WithTriggerSupport scannerTriggers = s.getTriggerSupport();
    if (scannerTriggers != null) {
        // This scanner has support for trigger activation, you may continue
        scannerTriggers.pressScanTrigger();
    } else {
        // This scanner does not support this functionality
        continue;
    }
}
```

### The `WithBarcodePairingSupport` interface

See the [BarcodePairing](barcode_pairing.md) documentation for more information.

:::{method} Scanner.getBarcodePairingSupport() -> WithBarcodePairingSupport
:::

### The `WithBeepSupport` interface

:::{method} Scanner.getBeepSupport() -> WithBeepSupport
:::

Extra interface implemented by scanners that support beeps. 

It exposes the following methods:

:::{method} beepScanSuccessful(@Nullable ScannerCommandCallback cb) -> void

Short high beep to indicate successful scan

:param ScannerCommandCallback cb: The callback to call with the result of the operation. May be null
or absent.
:::

:::{method} beepScanFailure(@Nullable ScannerCommandCallback cb) -> void

Long low beep to indicate unsuccessful scan

:param ScannerCommandCallback cb: The callback to call with the result of the operation. May be null
or absent.
:::

:::{method} beepPairingCompleted(@Nullable ScannerCommandCallback cb) -> void

Different beep to indicate a completed barcode pairing

:param ScannerCommandCallback cb: The callback to call with the result of the operation. May be null
or absent.
:::

### The `WithTriggerSupport` interface

:::{method} Scanner.getTriggerSupport() -> WithTriggerSupport
:::

Extra interface implemented by scanners that support software triggers. 

It exposes the following methods:

:::{method} pressScanTrigger(@Nullable ScannerCommandCallback cb) -> void

Simulates a press on a hardware-trigger, firing the beam that will read barcodes.

:param ScannerCommandCallback cb: The callback to call with the result of the operation. May be null
or absent.
:::

:::{method} releaseScanTrigger(@Nullable ScannerCommandCallback cb) -> void

Ends the effect of `pressScanTrigger()`.

:param ScannerCommandCallback cb: The callback to call with the result of the operation. May be null
or absent.
:::

### The `WithIlluminationSupport` interface

:::{method} Scanner.getIlluminationSupport() -> WithIlluminationSupport
:::

Extra interface implemented by scanners that support illumination. 

It exposes the following methods:

:::{method} enableIllumination(@Nullable ScannerCommandCallback cb) -> void

If the device used has a way to illuminate the target, enable it. Idempotent.

:param ScannerCommandCallback cb: The callback to call with the result of the operation. May be null
or absent.
:::

:::{method} disableIllumination(@Nullable ScannerCommandCallback cb) -> void

Opposite of `enableIllumination()`.

:param ScannerCommandCallback cb: The callback to call with the result of the operation. May be null
or absent.
:::

:::{method} toggleIllumination(@Nullable ScannerCommandCallback cb) -> void

See `enableIllumination()` and `disableIllumination()`.

:param ScannerCommandCallback cb: The callback to call with the result of the operation. May be null
or absent.
:::

:::{method} isIlluminationOn() -> boolean

True if the illumination method is active.

:returns: True if the illumination method is active.
:::

### The `WithLedSupport` interface

:::{method} Scanner.getLedSupport() -> WithLedSupport
:::

Extra interface implemented by scanners that support LED customization. 

It exposes the following methods:

:::{method} ledColorOn(ScannerLedColor color, @Nullable ScannerCommandCallback cb) -> void

Turns a LED color on.

:param ScannerCommandCallback cb: The callback to call with the result of the operation. May be null
or absent.
:param ScannerLedColor color: The color to turn on.
:::

:::{method} ledColorOff(ScannerLedColor color, @Nullable ScannerCommandCallback cb) -> void

Turns a LED color off.

:param ScannerCommandCallback cb: The callback to call with the result of the operation. May be null
or absent.
:param ScannerLedColor color: The color to turn off.
:::

### The `WithInventorySupport` interface

:::{method} Scanner.getInventorySupport() -> WithInventorySupport
:::

Extra interface implemented by scanners that offer inventory information.

It exposes the following methods:

:::{method} getStatus(String key) -> String

Get an inventory/status value. For example battery serial number, device MAC, etc. Keys are usually 
constants exported by drivers. Data returned may come from a local cache.

:param String key: The requested inventory key.
:returns: The corresponding value, or null if the key is not supported by this scanner.
:::

:::{method} getStatus(String key, boolean allowCache) -> String
:no-index:

Get an inventory/status value. For example battery serial number, device MAC, etc. Keys are usually 
constants exported by drivers.

:param String key: The requested inventory key.
:param boolean allowCache: If false the driver is not allowed to use a cache and MUST fetch fresh 
data from the device.
:returns: The corresponding value, or null if the key is not supported by this scanner.
:::

:::{method} getStatus() -> Map<String, String>
:no-index:

:returns: all inventory/status data known by the scanner. May be empty but not null.
:::

The following inventory keys are expected. It is up to each `Scanner` implementation to interpret
them:

:::{cpp:var} String SCANNER_STATUS_SCANNER_SN = "SCANNER_STATUS_SCANNER_SN"
:::
:::{cpp:var} String SCANNER_STATUS_SCANNER_MODEL = "SCANNER_STATUS_SCANNER_MODEL"
:::
:::{cpp:var} String SCANNER_STATUS_BATTERY_SN = "SCANNER_STATUS_BATTERY_SN"
:::
:::{cpp:var} String SCANNER_STATUS_BATTERY_MODEL = "SCANNER_STATUS_BATTERY_MODEL"
:::
:::{cpp:var} String SCANNER_STATUS_BATTERY_WEAR = "SCANNER_STATUS_BATTERY_WEAR"
:::
:::{cpp:var} String SCANNER_STATUS_BATTERY_CHARGE = "SCANNER_STATUS_BATTERY_CHARGE"
:::
:::{cpp:var} String SCANNER_STATUS_FIRMWARE = "SCANNER_STATUS_FIRMWARE"
:::
:::{cpp:var} String SCANNER_STATUS_BT_MAC = "SCANNER_STATUS_BT_MAC"
:::