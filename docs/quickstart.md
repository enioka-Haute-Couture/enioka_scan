# Quick Start

This guide will show you the minimum required steps to include **enioka Scan** in your application.
It assumes that you already have a configured Android Studio project.

This guide will only go over the minimal code. For a more in-depth guide for each use case, check
the [API reference](api/index.md).

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

Additional device-specific dependencies are required to enable support for actual scanners.
You can check the [library dependencies and compatibility matrix](dependencies.md)
for a detailed overview.

## Using the library

There are different ways to use the library, depending on where it has to be used. And in any ways,
the library does not hide its low-level objects which are an always available fallback. Most scanner
capabilities are exposed through the [`Scanner` API](api/scanner.md#the-scanner-interface), though
the default behavior of the 
[`ScannerServiceApi` API](api/scanner_service.md#the-scannerserviceapi-interface) ought to be enough
for simply receiving scanned data.

### Using enioka Scan in an activity

The simplest way to use **enioka Scan** is to extend the provided 
[`ScannerCompatActivity` activity](api/scanner_activity.md):

```java
import com.enioka.scanner.activities.ScannerCompatActivity;

public class MyScanningActivity extends ScannerCompatActivity {
}
```

This creates an activity with a very simple layout, which displays status messages from the scanners
as well as scanning results and some utility buttons: triggers for the scanner, a toggle for 
illumination, a beep trigger, a trigger to the camera scanner and a trigger to show scanner logs.

If the camera scanner SDK is included (`com.enioka.scanner.sdk.camera`), the default camera layout
and view will also be available inside the activity.

To use your own layouts with this activity, you can follow [this guide](guides/custom_layout.md).

Finally, note that inside the activity code there are a few hooks that can be overloaded - these are
public or protected methods, with full javadoc. Of notice are:

* `onData(List<Barcode> data)`
* `onStatusChanged(Scanner, ScannerStatusCallback.Status)`

:::{seealso}

* The [`ScannerCompatActivity` activity](api/scanner_activity.md) documentation
* The [`ScannerClient` interface](api/scanner_service.md#the-scannerclient-interface) documentation
:::

### Using enioka Scan outside of an activity

The library exposes a service which can be bound as any other bound service. `ScannerCompatActivity`
actually uses this service to handle scanner connexions, and you are free to use it directly in your
application.

**enioka Scan** provides a helper class that reduces the boilerplate code required to bind the
service to your application class:

```java
public class App extends Application {
    ScannerServiceBinderHelper serviceBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        serviceBinder = ScannerServiceBinderHelper.bind(this); // a second overload exists with a configuration Bundle.
    }
    
    // You may then call service methods directly
    public void myMethod() {
        serviceBinder.getScannerService().registerClient(/* ... */);
    }
}
```

It is then possible to use the 
[`ScannerServiceApi` instance](api/scanner_service.md#the-scannerserviceapi-interface) to access the
different endpoints of the service. The most interesting one is `registerClient()` which hooks
scanning callbacks and scanner/provider discovery notifications to a class implementing the
[`ScannerClient` interface](api/scanner_service.md#the-scannerclient-interface) such as a custom 
activity.

Please remember to unbind the service when it is not needed anymore, as for any other service. This
will often be in "onDestroy" hooks. Also, as this is a bound service, it is destroyed whenever it
has no bound clients left. Many applications actually bind the service on startup onto the
application context to be sure it is never destroyed and therefore is very quick to bind from
anywhere, but this depends on the use-case and is not compulsory at all.

Finally, there are a few Intent Extra properties which can be set to control the behaviour of the
service such as filters used in the scanner search. These can be found as static strings inside 
the [`ScannerServiceApi` interface](api/scanner_service.md#the-scannerserviceapi-interface), and 
methods in the [`ScannerSearchOptions` class](api/scanner_service.md#the-scannersearchoptions-class)
help converting search parameters to and from those intent extras.

:::{seealso}

* The [Scanner Service API](api/scanner_service.md) documentation
:::

## Using the camera

:::{warning}

* `com.enioka.scanner.sdk.camera` dependencies are required to use the camera scanner, check
  [library dependencies and compatibility matrix](dependencies.md).
  :::

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
scanning. You can also use the `CameraProvider` class to get a camera instance and control it,
including the main functionalities of the camera scanner.

The provided Camera activity will display a target rectangle, which can be moved or tapped to change
or refresh the autofocus area.

:::{seealso}

* The [camera](api/camera.md) documentation
:::