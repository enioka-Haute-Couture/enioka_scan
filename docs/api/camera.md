# The camera scanner

**enioka Scan** can still be used to manage barcodes scanned using the Android camera. For that, it
exposes the `CameraBarcodeScanView` and a series of helper classes to handle compatibility with both
Camera 1 and Camera 2 hardware APIs.

This page regroups all information needed to control this special scanner at a deeper level.

## The `CameraBarcodeScanView` class

This is the base Android `FrameLayout` used by the activity to display the camera. This view wraps
the API-specific `CameraBarcodeScanViewBase` implementation and handles the API selection based on
the device's properties.

For most users, this is the class they will interact with if they need to interact with the Camera
hardware and not just the [`Scanner` instance](scanner.md#the-scanner-interface).

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
    [`ResultHandler` implementation](scanner_callbacks.md#the-camerabarcodescanviewresulthandler-interface)
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

## Use `CameraBarcodeScanView` in your custom layout

To use this view in your own layout, you can add the following block to its XML definition and adapt
the attributes to your needs:

```xml
<com.enioka.scanner.camera.CameraBarcodeScanView
    android:id="@+id/camera_scan_view"
    android:layout_width="0dp"
    android:layout_height="0dp"
    app:forceCameraApiVersion="Auto"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintLeft_toLeftOf="parent"
    app:layout_constraintRight_toRightOf="parent"
    app:layout_constraintTop_toTopOf="parent"
    app:maxDistortionRatio="0.3"
    app:minResolutionY="720"
    app:maxResolutionY="1080"
    app:previewRatioMode="fillAvailableSpace"
    app:readerMode="Auto"
    app:storePreferredResolution="false"
    app:targetColorActive="@color/colorRed"
    app:targetColorPaused="@color/defaultItemColor"
    app:targetIsFixed="false"
    app:targetStrokeWidth="5"
    app:useAdaptiveResolution="true" 
/>
```

The main XML attributes are as follow:

:::{method} app:forceCameraApiVersion

"1" for the old camera API, "2" for the Camera2 API, any other value to let the library detect the
appropriate camera API for this device.
:::

:::{method} app:useAdaptiveResolution

If true, the resolution of the camera preview may be decreased if performances are deemed too low
by the frame analyzer.
:::

:::{method} app:minResolutionY

The minimum vertical resolution of the camera preview, after which resolution can no longer be
decreased by adaptive resolution when trying to improve performance.
:::

:::{method} app:maxResolutionY

The maximum vertical resolution of the camera preview, useful to limit performance costs.
:::

:::{method} app:readerMode

"1" for ZXING, any other value for ZBAR.
:::

:::{method} app:storePreferredResolution

If true, the app will persists the most used preview resolution to the application's preference.
:::

:::{method} app:targetColorActive

The default color of the "target" indicating where barcodes are expected to be.
:::

:::{method} app:targetColorPaused

The color of the "target" indicating where barcodes are expected to be, used whenever the scanning
is paused.
:::

:::{method} app:targetIsFixed

If true, the target cannot be moved by the user. If false, the user can drag the target up and down
on the preview.
:::

:::{method} app:targetStrokeWidth

The thickness of the target's lines.
:::