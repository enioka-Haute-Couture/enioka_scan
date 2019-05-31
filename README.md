# Enioka Haute Couture Android Barcode Scanning Library

This library makes the integration of all barcode scanners easy in any Android application,
avoiding vendor lock-in and lowering the cost of advanced scanner integration.

It is compatible with:
* Zebra EMDK devices (which comprise most of their integrated systems like the TC25, TC75, WT6000...)
* Honeywell AIDC integrated devices (including CN* devices)
* Athesi SPA43
* All hardware scanners acting as keyboards (HID), like most BlueTooth cheap handled scanners.

When there are no compatible hardware devices available, the library provides a camera reader based on ZBar or ZXing.

Through a common abstraction, it provides access to the following methods (provided the hardware supports them):
* pause/resume scanning
* disconnect/reconnect scanners
* enable/disable illumination from the scanner
* set scanner enabled symbologies

Also of note, even if the OS actually provides direct HID integration, they were integrated alongside the other input systems in this library to allow a single API for all scanning devices including them.

Finally, it provides a ready to use Service as well as an Activity, as well as a sample demo
application, allowing to use scanners in a matter of minutes.

# Adding the library to an Android application

TODO: upload to Maven Central and rewrite this.

At the root of your app: (all files *are inside the source tree or created by the project*)
* copy barcode_scanner_library_*.aar (the Zebra Bluetooth SDK) to aar_zebra
* copy eniokascan.aar to aar_enioka
* copy DataCollection.jar (Honeywell SDK) to barcodelibs

Inside aar_enioka, create a build.gradle file:
```
configurations.maybeCreate("default")
artifacts.add("default", file('eniokascan.aar'))
```

Inside aar_zebra:
```
configurations.maybeCreate("default")
artifacts.add("default", file('barcode_scanner_library_v2.0.8.0.aar'))
```

Inside settings.gradle, add "aar_enioka" and 'aar_zebra" to the list of includes.

Inside the application build.gradle, add:

```
dependencies {
    compile project(':aar_enioka')
    compile project(':aar_zebra')

    compile 'me.dm7.barcodescanner:zbar:1.8.3'

    compile 'com.android.support:appcompat-v7:25.3.1'
    compile 'com.android.support.constraint:constraint-layout:1.0.2'
}
```

Inside the manifest of your application add: nothing to do.

# Using the library

There are different ways to use the library, depending on where it has to be used. And in any ways, the library does not hide its low-level objects which are an always available fallback.

Note that depending on where the scanner is used, not all scanners will be available. For example, it is impossible to use the camera scanner from a background service as it has no UI to display the camera feed on, but it is entirely possible to use an integrated laser scanner from it. We therefore differentiate "background scanners", which only need an application context to run, from "foreground scanners", which need an Activity to be used. Most scanners are background except:
* The camera scanner, for the reason specified above
* HID scanners. They are controlled by the OS which only sends their input events to the active activity.

## In an Activity

This is the most usual use of for the library. The library provides the class `ScannerCompatActivity` from which to inherit. The most simple activity ever possible to use a scanner is this:
```
package com.enioka.scanner.demo;

import com.enioka.scanner.activities.ScannerCompatActivity;

public class MyScanningActivity extends ScannerCompatActivity {
}
```

This creates an activity with a very simple layout which display status messages from the scanners as well as scanning results and a button to toggle illumination. Now, most activities of course want to use their own layouts. This is allowed by setting a two fields (one for the camera layout, one for the laser layout) in onCreate or constructor:

```
@Override
    protected void onCreate(Bundle savedInstanceState) {
        layoutIdLaser = R.layout.activity_parcel_scan_laser;
        layoutIdCamera = R.layout.activity_parcel_scan_camera;

        super.onCreate(savedInstanceState);
    }
```

There are a few other fields to allow you to customize further the activity, which are all documented inside the class. These include options to rename the toggle illumination button ID, to disable camera or laser or both, or enable a fallback dialog with manual input and auto-completion.

Most importantly, the `ScannerCompatActivity.onData(List<Barcode> data)` method will usually be overloaded by child classes.

## Outside an activity

The library exposes a service which can be bound as any other bound service:
```
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
Intent intent = new Intent(this, ScannerService.class);
bindService(intent, connection, Context.BIND_AUTO_CREATE);
```

It is then possible to use the `ScannerServiceApi` object to access the different APIs of the service. The most interesting one is `registerClient` which hooks scanning callbacks, including a "data was received from scanner" callback.

Note that foreground scanners are not available using this method.

Please remember to unbind the service when it is not needed anymore, as for any other service. This will often be in "onDestroy" hooks. Also, as this is a bound service, it is destroyed whenever it has no bound clients left. Many applications actually bind the service on startup to be sure it is never destroyed and therefore is very quick to bind from anywhere, but this depends on the use-case and is not compulsory at all.


## With an activity object

This often happens when dealing with UI frameworks which have their own lifecycle handling such as Cordova. The programmer does not directly write activities deriving from Activity, but has access to the underlying Activity object used by the framework. In this case, it is just a matter of binding to the service like above, and then call `ScannerServiceApi.takeForegroundControl(Activity, ForegroundScannerClient)` (this internally calls `registerClient` too). This will register the activity as having foreground control, and full foreground scanner access will be available.

This method can also be used inside an Activity, when the use of an external base class for an Activity is not possible (for example when there already is a base class, or when using `AppCompatActivity` is not desired). This is exactly what `ScannerCompatActivity` does - it simply binds to the service.
