# Enioka Haute Couture Android Barcode Scanning Library

This library makes the integration of all barcode scanners easy in any Android application,
avoiding vendor lock-in and lowering the cost of advanced scanner integration.

It is compatible with:
* Zebra EMDK devices (which comprise most of their integrated systems like the TC25, TC75, WT6000...)
* Zebra Bluetooth scanners
* Honeywell AIDC integrated devices (including CN* devices)
* Athesi SPA43
* GeneralScan Bluetooth rings
* And a few others, check full compatibility table below.

When there are no compatible hardware devices available, the library provides a camera reader based on ZBar (default) or ZXing.

Through a common abstraction, it provides access to the following methods (provided the hardware supports them):
* press/release the scanner's trigger
* pause/resume scanning abilities
* disconnect/reconnect scanners
* enable/disable illumination from the scanner
* enable/disable colored LEDs
* set scanner enabled symbologies

Finally, it provides a ready to use Service that handles scanner lifecycles, as well as a template Activity, and a sample demo
application, allowing to use scanners in a matter of minutes.

# Compatibility matrix

The different plugins do not all have the same capabilities. The following table sums up what is possible to do with this library on common device families.

* "?" means the library does not support the function, but the underlying SDK or device may support it and the capability may be added later.
* "N/A" means this would make no sense on this device.
* "All 1D" symbologies means code 128, 39, I25, D25, EAN13. (some devices also provide QR code and other 2D symbologies)
* "Plugin AAR needed" means an additional aar must be used for this device. This is likely because it contains non-OSS code and cannot be distributed freely.
* "SDK needed" means the library will need the manufacturer SDK to work. This SDK likely could not be included for licence reasons, and must be provided by valid SDK license holder.
* "Provider name" is a name internal to the library which can be used to enable or disable a provider (by default all providers present are used which can slow initialisation).

Manufacturer | Device family          | Plugin AAR needed | SDK needed | Provider name                               | Connection | Notes                          | Basic scanning | Symbologies recognized      | Symbologies detection | Symbology configuration | Illumination control | Disable trigger | LED control | Beep control | Trigger control
------------ | ---------------------- | ----------------- | ---------- | ------------------------------------------- | ---------- | ------------------------------ | -------------- | --------------------------- | --------------------- | ----------------------- | -------------------- | --------------- | ----------- | ------------ | ---------------
Zebra        | Integrated: TC25...    | Yes               | No         | EmdkProvider                                | Integrated | EMDK devices                   | Yes            | All 1D                      | Yes                   | On startup              | No                   | Yes             | N/A         | No?          | ?
Zebra        | BT ring RS6000,5100    | Yes               | Yes        | BtZebraProvider                             | BT SPP     |                                | Yes            | All 1D                      | Yes                   | On startup              | No                   | Yes             | Yes         | Yes          | ?
Zebra        | BT ring RS6000,5100    | No                | No         | ZebraOssSppScannerProvider                  | BT SPP     | Alternate pure OSS provider    | Yes            | All 1D                      | Yes                   | On startup              | No                   | Yes             | Yes         | Yes          | Yes
Bluebird     | Integrated: EF500...   | No                | No         | BluebirdProvider                            | Integrated |                                | Yes            | Most, save D25              | Yes                   | ?                       | No                   | Yes             | N/A         | ?            | ?
Athesi       | Integrated: SPA43LTE   | No                | No         | AthesiHHTProvider                           | Integrated | Device must be named SPA43LTE  | Yes            | All 1D                      | Yes                   | On startup              | No                   | Yes             | N/A         | No?          | Yes
Athesi       | Integrated: SPA43LTE   | No                | No         | AthesiE5LProvider                           | Integrated | Device must be named RD50TE    | Yes            | All 1D                      | No                    | No?                     | No                   | No              | N/A         | No?          | Yes
Honeywell    | Integrated: EDA50...   | Yes               | Yes        | AIDCProvider                                | Integrated | AIDC devices (Intermec)        | Yes            | All 1D                      | Yes                   | On startup              | No                   | ?               | N/A         | No?          | ?
Honeywell    | Integrated: EDA52      | No                | No         | HoneywellOssIntegratedScannerProvider       | Integrated | Device must be named EDA52     | Yes            | All 1D                      | Yes                   | No                      | No                   | ?               | N/A         | No?          | Yes
Honeywell    | BT Voyager 1602g       | No                | No         | HoneywellOssSppScannerProvider              | BT SPP     |                                | Yes            | All 1D                      | Yes                   | No                      | No                   | ?               | N/A         | No?          | Yes
GeneralScan  | BT ring R5000BT        | No                | No         | GsSppScannerProvider                        | BT SPP     |                                | Yes            | All 1D                      | No                    | ?                       | No                   | No              | No          | No           | ?
ProGlove     | BT glove Mark II       | No                | No         | ProgloveProvider                            | BT BLE     | ProGlove app needed            | Yes            | All 1D                      | Yes                   | ?                       | No                   | No              | Yes         | No?          | ?
Koamtac      | BT KDC (180...)        | Yes               | No         | Koamtac                                     | BT BLE     | Device must be named KDC*      | Yes            | All 1D                      | Yes                   | ?                       | No                   | Yes             | Yes         | Yes          | ?
M3           | RingScanners           | Yes               | No         | M3RingScannerProvider                       | BT SPP     | M3 app needed                  | Yes            | All 1D                      | No                    | No                      | No                   | Yes             | No          | No           | ?
Camera       | Devices with camera    | No                | No         |                                             | Integrated | Capabilities depend on device  | Yes            | All 1D                      | Yes                   | On startup              | Yes (flash light)    | Yes             | N/A         | Yes          | N/A

There also are traces of Postech BT ring scanner compatibility - their communication protocol is like the GeneralScan one, but the authors of the library lacked a test device to finish it, so it was disabled.
Note that many scanners not mentioned here are interpreted by the OS as a keyboard (HID devices), and as such are not supported in a specific way by the library. As for the camera, it is initialized in a different way than other scanners, we recommend taking a look at the template activity to understand its usage.

# Adding the library to an Android application

For all devices which are not marked as "plugin AAR needed" in the above table, a single dependency is needed. It is available either here on Github or (better) or Maven Central: just use coordinates `com.enioka.scanner:scanner:x.y.z:aar` (do remember to specify `mavenCentral()` inside the Gradle build file `repositories` section), and update the version to latets version).

That's all if you do not need an AAR plugin. If you also need a plugin AAR (provided by us) and a proprietary SDK (provided by the scanner manufacturer, at the root of your app: (all files *are inside the source tree or created by the project*)
* copy barcode_scanner_library_*.aar (the Zebra Bluetooth SDK) to aar_zebra
* copy DataCollection.jar (Honeywell SDK) to barcodelibs

Inside aar_zebra:

```groovy
configurations.maybeCreate("default")
artifacts.add("default", file('barcode_scanner_library_v2.0.8.0.aar'))
```

Inside settings.gradle, add 'aar_zebra" to the list of includes.

Inside the application build.gradle, add:

```groovy
dependencies {
    compile project(':aar_zebra')

    compile 'me.dm7.barcodescanner:zbar:1.8.3'

    compile 'com.android.support:appcompat-v7:25.3.1'
    compile 'com.android.support.constraint:constraint-layout:1.0.2'
}
```

Inside the manifest of your application, nothing to do.

# Using the library

There are different ways to use the library, depending on where it has to be used. And in any ways, the library does not hide its low-level objects which are an always available fallback.
Most scanner capabilities are exposed through the [`Scanner`][scanner-api] API, though the default behavior of the [`ScannerServiceApi`][scanner-service-api] API ought to be enough for simply receiving scanned data.

## In an Activity

This is the most usual use of for the library. The library provides the class [`ScannerCompatActivity`][scanner-compat-activity] from which to inherit. The most simple activity ever possible to use a scanner is this:

```java
import com.enioka.scanner.activities.ScannerCompatActivity;

public class MyScanningActivity extends ScannerCompatActivity {
}
```

This creates an activity with a very simple layout which display status messages from the scanners as well as scanning results and a button to toggle illumination. Now, most activities of course want to use their own layouts. This is allowed by setting two fields (one for the camera layout, one for the laser layout) in the `onCreate()` method or the constructor:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    layoutIdLaser = R.layout.activity_parcel_scan_laser;
    layoutIdCamera = R.layout.activity_parcel_scan_camera;

    super.onCreate(savedInstanceState);
}
```

There are a few other fields to allow you to customize further the activity, which are all documented inside the class. These include options to rename the toggle illumination button ID, to disable camera or laser or both, or enable a fallback dialog with manual input and auto-completion.

If the provided template does not suit your needs, but you still want to take advantage of the [`ScannerServiceApi`][scanner-service-api], simply make sure that your activity follows the steps described in the section below.

## Outside an activity

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

It is then possible to use the [`ScannerServiceApi`][scanner-service-api] object to access the different endpoints of the service. The most interesting one is `registerClient()` which hooks scanning callbacks and scanner/provider discovery notifications to a class implementing the [`ScannerClient`][scanner-client] interface such as a custom activity.

Please remember to unbind the service when it is not needed anymore, as for any other service. This will often be in "onDestroy" hooks. Also, as this is a bound service, it is destroyed whenever it has no bound clients left. Many applications actually bind the service on startup onto the application context to be sure it is never destroyed and therefore is very quick to bind from anywhere, but this depends on the use-case and is not compulsory at all.

Finally, there are a few Intent extra properties which can be set to control the behaviour of the service such as filters used in the scanner search.
These can be found as static strings inside the [`ScannerServiceApi`][scanner-service-api] interface, and methods in the [`ScannerSearchOptions`][scanner-search-options] class help converting search parameters to and from those intent extras.

## Using the camera

When there is no laser scanner available, or when a button is clicked, the `ScannerCompatActivity` activity will fallback to using the device camera (if any) to scan barcodes. This leverages two different barcode scanning libraries, ZBar and ZXing in recent versions. It is compatible both with very old Camera APIs as well as more recent Camera 2 APIs. It tries to set the best camera parameters for barcode scanning, including dynamically setting the camera resolution according to the processing speed of the device. As a result, it may take a few dozen seconds to reach the most suitable settings on first startup - the resolution is then stored and reused on subsequent initialisations.

The camera scanner can actually be used easily inside your own activities without any links with the rest of the library (no need to use the scanner service, etc) by just adding the `CameraBarcodeScanView` to your layouts, and then registering a callback on this view using `CameraBarcodeScanView.setResultHandler`. Other APIs are available to enable the torch or pause scanning.

# Developer quick start (modifying this library)

## Developing for Android

In order to start developing and testing the library:
* Have Android Studio installed and open the project with it
* Connect a compatible android device via USB, it should then show up in Android Studio's device manager
* Run the app with the android device selected

In case the android device is not detected by Android Studio:
* Make sure the device is in developer mode and has USB Debugging enabled
* Make sure the USB cable supports data transfer (some cables only support charging)

## Adding another SDK to the library

A scanner SDK contains at least a [`Scanner`][scanner-api] implementation (interfacing between the code and the device) and a [`ScannerProvider`][scanner-provider-api] implementation (handling scanner creation and whether the SDK is compatible with the search).

In order for a new scanner SDK to be found by the library, the [`ScannerProvider`][scanner-provider-api] implementation needs to be declared as a service in its `AndroidManifest.xml` with an intent-filter containing the action `com.enioka.scan.PROVIDE_SCANNER`.
The associated Java class does not need to extend Android's `Service` class (the `tools:ignore="Instantiatable"` attribute may be added to the service in the manifest), but it must provide a public default constructor as it will be instantiated using `Class.getName()`.

See the [Mock SDK][mock-sdk] for an example of addon SDK.

# Release process

To publish the library to Maven Central and GitHub releases, a tag must be created and attached to the appropriate commit.
The tag will trigger a workflow that will automatically create a github release containing the AAR file, and publish the library to Maven Central.
If the release is created manually, the workflow will not run and the library will not be published correctly. Only the tag needs to be manually created.

[mock-sdk]: ./enioka_scan_mock
[scanner-api]: ./enioka_scan/src/main/java/com/enioka/scanner/api/Scanner.java
[scanner-client]: ./enioka_scan/src/main/java/com/enioka/scanner/service/ScannerClient.java
[scanner-compat-activity]: ./enioka_scan/src/main/java/com/enioka/scanner/activities/ScannerCompatActivity.java
[scanner-provider-api]: ./enioka_scan/src/main/java/com/enioka/scanner/api/ScannerProvider.java
[scanner-search-options]: ./enioka_scan/src/main/java/com/enioka/scanner/api/ScannerSearchOptions.java
[scanner-service-api]: ./enioka_scan/src/main/java/com/enioka/scanner/service/ScannerServiceApi.java