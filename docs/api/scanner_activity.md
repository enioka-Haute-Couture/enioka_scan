# The Scanner activity 

The simplest way to start using **enioka Scan** in an activity is simply to inherit from the
`ScannerCompatActivity` class. This class implements the 
[`ScannerClient` interface](scanner_service.md#the-scannerclient-interface) and handles the scanner
service bindings and life cycle. It also exposes a default UI to print scan results, status messages
and some buttons to manually control the scanner device from the app.

Most of its methods are protected and can be overridden if needed. Mainly, methods from the
[`ScannerClient` interface](scanner_service.md#the-scannerclient-interface) should be replaced with
your own to adapt the way scan results are handled by your application.

By default, the activity will use the 
[default search options](scanner_service.md#the-scannersearchoptions-class) to initiate scanner
search. There are two ways to change these options: override the 
[`getServiceInitExtras()` method](#scannercompatactivity-methods), and passing the search options as
extras to the intent starting the activity. In case both are used, the method is applied first, then
intent extras.

This page will describe attributes and methods specific to this activity, disregarding overrides. 
For most users, it is recommended to only change the value of attributes or interface methods, as 
overriding class methods may cause unexpected side-effects. For example, using a custom layout only
requires changing the corresponding layout ID attributes.

## `ScannerCompatActivity` attributes

:::{cpp:var} boolean laserModeOnly = false

If true, the activity will not switch to using the camera, even if no other scanners are available.
:::

:::{cpp:var} boolean enableScan = true

If false, the activity will behave like a standard `AppCompatActivity`.
:::

:::{cpp:var} boolean useBluetooth = true

If true, the activity will check that the application has bluetooth permissions before starting the
service. **This does not affect scanner search options**.
:::

:::{cpp:var} int layoutIdLaser = R.layout.activity_main

The layout used by the activity when using regular scanner devices. May be replaced with your own.
:::

:::{cpp:var} int cameraToggleId = R.id.scanner_bt_camera

The ID of the optional ImageButton on which to press to manually switch to camera mode.
:::

:::{cpp:var} int flashlightViewId = R.id.scanner_flashlight

The ID of the optional ImageButton on which to press to toggle the flashlight/illumination.
:::

:::{cpp:var} int keyboardOpenViewId = R.id.scanner_bt_keyboard

The ID of the optional toggle button on which to display the manual input fragment.
:::

:::{cpp:var} ManualInputFragment manualInputFragment;

An optional fragment allowing to input a value with the soft keyboard (for cases when scanners do 
not work).
:::

:::{cpp:var} List<ManualInputItem> autocompletionItems = new ArrayList<>()

Auto completion items for manual input (with manualInputFragment).
:::

:::{cpp:var} int threshold = 5

How many characters should be entered before auto-completion starts.
:::

:::{cpp:var} ScannerServiceApi scannerService;

The instance of the bound scanner service, can be used to access service methods but should not be
replaced.

Initialized by the `onStart()` and `onResume()` methods.
:::

## `ScannerCompatActivity` methods

:::{method} getServiceInitExtras() -> Bundle

:returns: The [search options](scanner_service.md#the-scannersearchoptions-class) that will be used 
    when starting the service. These options may be overriden by intent extras when launching the
    activity.
:::

:::{method} setAutocompletion(List<String> autocompletion, int threshold) -> void

**Inserts** the given autocompletion strings into the `autocompletionItems` and updates the 
autocompletion threshold.

:param List<String> autocompletion: The autocompletion items to add.
:param int threshold: The new threshold.
:::

:::{method} setAutocompletionItems(List<ManualInputItem> items, int threshold) -> void

**Replaces** `autocompletionItems` with the given list and updates the autocompletion threshold.

:param List<String> autocompletion: The autocompletion items to use.
:param int threshold: The new threshold.
:::

:::{method} anyScannerSupportsIllumination() -> boolean

Checks whether any available scanner supports Illumination.
:::

:::{method} anyScannerHasIlluminationOn() -> boolean

Checks whether any available scanner has Illumination toggled on.
:::

:::{seealso}

* The [`ScannerClient` interface](scanner_service.md#the-scannerclient-interface) documentation.
:::