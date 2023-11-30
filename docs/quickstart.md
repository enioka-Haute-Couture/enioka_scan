# Quick Start

:::{admonition} WIP
:class: attention

This documentation is a work in progress.
:::

:::{admonition} TODO
:class: attention

* Edit source links to redirect to the API reference or guides.
:::

This guide will show you the minimum required steps to include **enioka Scan** in your application.
It assumes that you already have a configured Android Studio project.

This guide will only go over the minimal code. For a more in-depth guide for each use case, check
the [API reference](/api/index).

## Add the required dependencies

In order to use **enioka Scan**, you need to add the corresponding dependency to your `build.gradle`.

```groovy
repositories {
    mavenCentral()
}
dependencies {
    implementation 'com.enioka.scanner:scanner:2.4.1:aar'
}
```

Depending on your scanning devices, this single dependency may be enough. For some devices, other
dependencies are required, you can check [Dependencies](/dependencies) for a detailed overview.

## Using the library

There are different ways to use the library, depending on where it has to be used. And in any ways,
the library does not hide its low-level objects which are an always available fallback. Most scanner
capabilities are exposed through the [`Scanner`][scanner-api] API, though the default behavior of
the [`ScannerServiceApi`][scanner-service-api] API ought to be enough for simply receiving scanned
data.

### Using enioka Scan in an activity

The simplest way to use **enioka Scan** is to extend the provided 
[`ScannerCompatActivity`][scanner-compat-activity]:

```java
import com.enioka.scanner.activities.ScannerCompatActivity;

public class MyScanningActivity extends ScannerCompatActivity {
}
```

This creates an activity with a very simple layout, which displays status messages from the scanners
as well as scanning results and some utility buttons: triggers for the scanner, a toggle for 
illumination, and a beep trigger.

Now, most activities of course want to use their own layouts. This is allowed by setting two fields
(one for the camera layout, one for the laser layout) in the constructor of the activity:

```java
@Override
protected void MyScanningActivity() {
    super.onCreate();
    layoutIdLaser = R.layout.activity_parcel_scan_laser;
    layoutIdCamera = R.layout.activity_parcel_scan_camera;
}
```

There are a few other fields to allow you to customize further the activity, which are all 
documented inside the class. These include options to rename the toggle illumination button ID, to 
disable camera or laser or both, or enable a fallback dialog with manual input and auto-completion.

If the provided template does not suit your needs, but you still want to take advantage of the 
[`ScannerServiceApi`][scanner-service-api], simply make sure that your activity follows the steps 
described in the section below.

if you use a custom layout, you need to use our Camera scanning view. The ID of this view should by 
default be `camera_scan_view` (which can be customized inside the constructor as for the other 
views). This view accepts a few parameters, which are displayed here with their default values:

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

Finally, note that inside the activity code there are a few hooks that can be overloaded - these are
public or protected methods, with full javadoc. Of notice are:

* `onData(List<Barcode> data)`
* `onStatusChanged(Scanner, ScannerStatusCallback.Status)`

### Using enioka Scan outside of an activity

The library exposes a service which can be bound as any other bound service:

```java
ScannerServiceApi scannerService;
boolean serviceBound = false;

ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to ScannerService, cast the IBinder and get the ScannerServiceApi instance
            ScannerService.LocalBinder binder = (ScannerService.LocalBinder) service;
            scannerService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
            scannerService = null;
        }
    };

// Bind to ScannerService service
// Make sure to bind the ScannerService class (or any implementation of the API), not the ScannerServiceApi interface
Intent intent = new Intent(this, ScannerService.class);
bindService(intent, connection, Context.BIND_AUTO_CREATE);
```

It is then possible to use the [`ScannerServiceApi`][scanner-service-api] object to access the
different endpoints of the service. The most interesting one is `registerClient()` which hooks
scanning callbacks and scanner/provider discovery notifications to a class implementing the
[`ScannerClient`][scanner-client] interface such as a custom activity.

Please remember to unbind the service when it is not needed anymore, as for any other service. This
will often be in "onDestroy" hooks. Also, as this is a bound service, it is destroyed whenever it
has no bound clients left. Many applications actually bind the service on startup onto the
application context to be sure it is never destroyed and therefore is very quick to bind from
anywhere, but this depends on the use-case and is not compulsory at all.

If you want to bind the service inside your application class, a helper is provided to avoid
boilerplate code that can be used as such:

```java
public class App extends Application {
    ScannerServiceBinderHelper serviceBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        serviceBinder = ScannerServiceBinderHelper.bind(this); // a second overload exists with a configuration Bundle.
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        serviceBinder.disconnect();
    }
}
```

Finally, there are a few Intent extra properties which can be set to control the behaviour of the
service such as filters used in the scanner search.
These can be found as static strings inside the [`ScannerServiceApi`][scanner-service-api] interface
, and methods in the [`ScannerSearchOptions`][scanner-search-options] class help converting search
parameters to and from those intent extras.

## Using the camera

When there is no laser scanner available, or when a button is clicked, the `ScannerCompatActivity` 
activity will fallback to using the device camera (if any) to scan barcodes. This leverages two 
different barcode scanning libraries, ZBar and ZXing in recent versions. It is compatible both with 
very old Camera APIs as well as more recent Camera 2 APIs. It tries to set the best camera 
parameters for barcode scanning, including dynamically setting the camera resolution according to 
the processing speed of the device. As a result, it may take a few dozen seconds to reach the most 
suitable settings on first startup - the resolution is then stored and reused on subsequent 
initialisations.

The camera scanner can actually be used easily inside your own activities without any links with the
rest of the library (no need to use the scanner service, etc) by just adding the
`CameraBarcodeScanView` to your layouts, and then registering a callback on this view using
`CameraBarcodeScanView.setResultHandler`. Other APIs are available to enable the torch or pause
scanning.

The provided Camera activity will display a target rectangle, which can be moved or tapped to change
or refresh the autofocus area.

[scanner-api]: https://github.com/enioka-Haute-Couture/enioka_scan/blob/master/enioka_scan/src/main/java/com/enioka/scanner/api/Scanner.java
[scanner-client]: https://github.com/enioka-Haute-Couture/enioka_scan/blob/master/enioka_scan/src/main/java/com/enioka/scanner/service/ScannerClient.java
[scanner-compat-activity]: https://github.com/enioka-Haute-Couture/enioka_scan/blob/master/enioka_scan/src/main/java/com/enioka/scanner/activities/ScannerCompatActivity.java
[scanner-search-options]: https://github.com/enioka-Haute-Couture/enioka_scan/blob/master/enioka_scan/src/main/java/com/enioka/scanner/api/ScannerSearchOptions.java
[scanner-service-api]: https://github.com/enioka-Haute-Couture/enioka_scan/blob/master/enioka_scan/src/main/java/com/enioka/scanner/service/ScannerServiceApi.java
