# How to use in your application

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
