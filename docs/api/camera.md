# The camera scanner

:::{admonition} WIP
:class: attention

This documentation is a work in progress.
:::

**enioka Scan** can still be used to manage barcodes scanned using the Android camera. For that, it
exposes the `CameraBarcodeScanView` and a series of helper classes to handle compatibility with both
Camera 1 and Camera 2 hardware APIs.

This page regroups all information needed to control this special scanner at a deeper level.

## The `CameraBarcodeScanView` class

This is the base Android `FrameLayout` used by the activity to display the camera. This view wraps
the API-specific `CameraBarcodeScanViewBase` implementation and handles the API selection based on
the device's properties.

For most users, this is the class they will interact with if they need to interact with the Camera
hardware and not just the [`Scanner`](scanner) instance.

:::{method} setReaderMode(CameraReader readerMode) -> void

Change the library used to read barcodes from the camera feed.

:param CameraReader readerMode: The library to use, between `CameraReader.ZBAR` and 
    `CameraReader.ZXing`
:::

:::{method} addSymbology(BarcodeType barcodeType) -> void

Add a symbology to detect. By default, only `CODE_128` is used.

:param BarcodeType barcodeType: The symbology to add.
:::

:::{method} setResultHandler(ResultHandler handler) -> void

Change the callback used when a barcode is read. 

Used by `CameraBarcodeScanViewScanner` to register itself with the view and correctly propagate
barcodes read by the camera as any regular scanner reads.

:param ResultHandler handler: the 
    [`ResultHandler`](scanner_callbacks.md#the-camerabarcodescanviewresulthandler-interface) 
    implementation
:::

:::{method} setTorch(boolean value) -> void

Switch the camera's torch on or off.

:param boolean value: Indicate if the torch should be turned on (true) or off (false)
:::

:::{method} getSupportTorch() -> boolean

:returns: true if the camera supports torch activation. false otherwise.
:::

:::{method} getTorchOn() -> boolean

:returns: true if the camera's torch is on, false otherwise.
:::

:::{method} cleanUp() -> void

Unhooks all camera callbacks and closes it. After this method is called, the camera becomes unusable
and the view needs to be reinitialized.
:::

:::{method} pauseCamera() -> void

Pauses the camera's capture.
:::

:::{method} resumeCamera() -> void

Resumes the camera's capture.
:::

:::{method} getLatestSuccessfulScanJpeg() -> byte[]

:returns: The JPEG data of the image used in the latest successful scan, or null if there is no
    previous scan data.
:::