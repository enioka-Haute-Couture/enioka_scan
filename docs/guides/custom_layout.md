# Using a custom layout with the scanning activity

In order to use a custom layout with the provided `ScannerCompatActivity`, two fields need to be
changed to point it. You can change these attributes in your activity's `onCreate()` lifecycle
step.

```java
import com.enioka.scanner.activities.ScannerCompatActivity;

public class MyScanningActivity extends ScannerCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate();
        layoutIdLaser = R.layout.activity_parcel_scan_laser;
        layoutIdCamera = R.layout.activity_parcel_scan_camera;
    }
}
```

Here, `layoutIdLaser` is the layout used when handling laser scanner devices, and `layoutIdCamera`
is the layout used when using the device's camera as a scanner.

To ensure activity is able to properly switch to the camera when needed, you should include the
following view block in the `<layout>.xml` file used by `layoutIdCamera`:

```xml
<com.enioka.scanner.camera.CameraBarcodeScanView
    android:id="@+id/camera_scan_view"
    app:forceCameraApiVersion="Auto"
    app:maxDistortionRatio="0.3"
    app:maxResolutionY="1080"
    app:previewRatioMode="fillAvailableSpace"
    app:readerMode="Auto"
    app:storePreferredResolution="false"
    app:targetColor="@color/colorRed"
    app:targetIsFixed="false"
    app:targetStrokeWidth="5"
    app:useAdaptiveResolution="true"  />
```

Should you choose a different ID for this view, remember to also change the value of `cameraViewId`
in the activity.

:::{seealso}

* The [scanner activity](../api/scanner_activity.md) documentation
* The [camera](../api/camera.md) documentation
:::