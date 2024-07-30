# The camera scanner

**enioka Scan** can still be used to manage barcodes scanned using the Android camera, through
`com.enioka.scanner.sdk.camera`, (check
[library dependencies and compatibility matrix](dependencies.md)). For that, it exposes the
`CameraBarcodeScanView` and a series of helper classes to handle compatiblity with both Camera 1
and Camera 2 hardware APIs.

This page regroups all information needed to control this special scanner at a deeper level.

## The `CameraBarcodeScanView` class

This is the base Android `FrameLayout` used by the activity to display the camera. This view wraps
the API-specific `CameraBarcodeScanViewBase` implementation and handles the API selection based on
the device's properties.

For most users, this is the class they will interact with if they need to interact with the Camera
hardware and not just the [`Scanner` instance](scanner.md#the-scanner-interface).

:::{method} setPreviewRatioMode(AspectRatioMode mode) -> void

Change the preview aspect ratio mode of the camera. This change will not take effect until the next
view refresh. You can force a refresh by pausing and resuming the camera.

:param AspectRatioMode mode: The mode to use for filling the camera preview. Can be one of the
following values:
- `0`: `fillWithCrop`: The preview will be scaled to fit the picture, cropping the sides if
    needed. The aspect ratio of the preview will be kept.
- `1`: `fillWithBlackBars`: The preview will be scaled to fit the picture, with black bars on
    the sides if needed. The aspect ratio of the preview will be kept.
- `2`: `fillWithStretch`: The preview will fill the available space, by squashing or stretching
    the preview if needed. The aspect ratio of the preview can be altered.
:::

Example of a preview with the `fillWithCrop` mode:

:::{image} /pictures/crop.png
:width: 400
:alt: Example of a preview with the `fillWithCrop` mode.
:::

Example of a preview with the `fillWithBlackBars` mode:

:::{image} /pictures/black_bars.png
:width: 400
:alt: Example of a preview with the `fillWithBlackBars` mode.
:::

Example of a preview with the `fillWithStretch` mode:

:::{image} /pictures/stretch.png
:width: 400
:alt: Example of a preview with the `fillWithStretch` mode.
:::

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

:::{method} resetTargetPosition() -> void

Reset the vertical position of the target. Also resets the associated preferences.
:::

:::{method} orientationChanged() -> void

Notifies the view that the orientation of the device has changed by calling
`setDisplayOrientation()` with the correct clockwise rotation of the camera preview, depending on
the device's orientation.

:::{method} setTargetPosition(float y) -> void

Set the top target's vertical position on the preview (y coordinate).
:::

:::{method} setTargetDimension(float width, float height) -> void

Set the target's dimensions on the preview.
:::

:::{method} getLatestSuccessfulScanJpeg() -> byte[]

:returns: The JPEG data of the image used in the latest successful scan, or null if there is no
    previous scan data.
:::

## Use `CameraBarcodeScanView` in your custom layout

To use this view in your own layout, you can add the following block to its XML definition and adapt
the attributes to your needs:

```xml
<com.enioka.scanner.sdk.camera.CameraBarcodeScanView
    android:id="@+id/cameraScanView"
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
    app:previewRatioMode="fillWithCrop"
    app:readerMode="Auto"
    app:storePreferredResolution="false"
    app:targetColorActive="@color/colorRed"
    app:targetColorPaused="@color/defaultItemColor"
    app:targetIsFixed="false"
    app:targetStrokeWidth="5"
    app:useAdaptiveResolution="true" />
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

:::{method} app:previewRatioMode

The mode to use for filling the camera preview. Can be one of the following values:
- `0`: `fillWithCrop`: The preview will be scaled to fit the picture, cropping the sides if
  needed. The aspect ratio of the preview will be kept.
- `1`: `fillWithBlackBars`: The preview will be scaled to fit the picture, with black bars on
  the sides if needed. The aspect ratio of the preview will be kept.
- `2`: `fillWithStretch`: The preview will fill the available space, by squashing or stretching
  the preview if needed. The aspect ratio of the preview can be altered.

If the value of this attribute is not recognized or defined, the preview ratio mode will be set
by default to `fillWithCrop`.
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

## The CameraScannerProvider API

The `CameraScannerProvider` is the entry point to the camera scanner API. It is used to create and
control the camera scanner service, and to access the camera scanner view. As the camera scanner is
part of the `com.enioka.scanner.sdk.camera` package, it is not directly accessible from the main
library.

### The `CameraScannerProvider` interface

:::{enum} AspectRatioMode

The mode to use for filling the camera preview. Can be one of the following values:
- `FILL_WITH_CROP`: The preview will be scaled to fit the picture, cropping the sides if needed. The
  aspect ratio of the preview will be kept.
- `FILL_WITH_BLACK_BARS`: The preview will be scaled to fit the picture, with black bars on the
  sides if needed. The aspect ratio of the preview will be kept.
- `FILL_WITH_STRETCH`: The preview will fill the available space, by squashing or stretching the
  preview if needed. The aspect ratio of the preview can be altered.

:::

:::{method} getIdResource() -> HashMap<String, Integer>

:returns: A map containing the IDs of the views used by the camera scanner. May be replaced with
your own.

It contains the following values:
- `layout_id_camera`: The ID of the layout containing the camera view.
  Set to `R.layout.activity_main_alt`.
- `camera_view_id`: The ID of the
  [`CameraBarcodeScanView`](camera.md#the-camerabarcodescanview-class) inside the `layout_id_camera`
  layout. Set to `R.id.cameraScanView`.
- `scanner_toggle_view_id`: The ID of the optional ImageButton on which to press to toggle the
  zxing/zbar camera scan library. Set to `R.id.scannerSwitchZxing`.
- `scanner_toggle_pause_id`: The ID of the optional toggle button on which to press to pause/unpause
  the scanner. Set to `R.id.scannerSwitchPause`.
- `card_last_scan_id`: The ID of the card view that displays the last scan.
  Set to `R.id.cardCameraLastScan`.
- `constraint_layout_id`: The ID of the constraint layout inside the camera layout.
  Set to `R.id.constraintLayoutMainActivity`.
- `scanner_flashlight_id`: The ID of the optional ImageButton on which to press to toggle the
  flashlight/illumination. Set to `R.id.scannerFlashlight`.
- `scanner_bt_provider_logs`: The ID of the optional ImageButton on which to press to manually access
  available providers logs. Set to `R.id.scannerBtProviderLogs`.
:::

:::{method} getCameraScanner(Context ctx, ScannerConnectionHandler handler, ScannerSearchOptions options) -> void

Called to initialize the camera scanner with the given view and callbacks.
:::

:::{method} isCameraScannerInitialized() -> boolean

:returns: true if the camera scanner is initialized, false otherwise.
:::

:::{method} reset() -> void

Resets the camera scanner.
:::

:::{method} toggleIllumination() -> void

Toggles the camera's illumination.
:::

:::{method} disconnect() -> void

Disconnects the camera scanner.
:::

:::{method} pauseCamera() -> void

Pauses the camera scanner.
:::

:::{method} resumeCamera() -> void

Resumes the camera scanner.
:::

:::{method} isIlluminationOn() -> boolean

:returns: true if the camera's illumination is on, false otherwise.
:::

:::{method} setPreviewRatioMode(View cameraView, int previewRatioMode) -> void

See [setPreviewRatioMode](camera.md#the-camerabarcodescanview-class) for more information.
:::

:::{method} setReaderMode(View cameraView, boolean readerMode) -> void

:param View cameraView: The camera view to change the reader mode of.
:param boolean readerMode: false for ZBar, true for ZXing.

Change the library used to read barcodes from the camera feed.
:::

:::{method} orientationChanged(View cameraView) -> void

:param View cameraView: The camera view to notify of the orientation change.

Notifies the view that the orientation of the device has changed by calling `setDisplayOrientation()`
with the correct clockwise rotation of the camera preview, depending on the device's orientation.
:::

:::{method} setTargetPosition(View cameraView, float y) -> void

:param View cameraView: The camera view to change the target position of.
:param float y: The top target's vertical position on the preview (y coordinate).

Set the top target's vertical position on the preview.
:::

:::{method} setTargetDimension(View cameraView, float width, float height) -> void

:param View cameraView: The camera view to change the target dimensions of.
:param float width: The target's width.
:param float height: The target's height.

Set the target's dimensions on the preview.
:::
