# Enioka Haute Couture Android Barcode Scanning Library

This library makes the integration of all barcode scanners easy in any Android application,
avoiding vendor lock-in and lowering the cost of advanced scanner integration.

It is compatible with a wide variety of scanning devices, integrated or external, from different
vendors such as Zebra, Honeywell, Athesi and more.

When there are no compatible hardware devices available, the library provides a camera reader based
on [ZBar](https://mvnrepository.com/artifact/me.dm7.barcodescanner/zbar) (default) or
[ZXing](https://mvnrepository.com/artifact/com.google.zxing/core).

Through a common abstraction, it provides access to the following methods (provided the hardware
supports them):
- press/release the scanner's trigger
- pause/resume scanning abilities
- disconnect/reconnect scanners
- enable/disable illumination from the scanner
- enable/disable colored LEDs
- set scanner enabled symbologies

Finally, it provides a ready to use Service that handles scanner lifecycles, as well as a template
Activity, and a demo application, allowing you to use scanners in a matter of minutes.

In order to use **enioka Scan**, you need to add the corresponding dependency to your `build.gradle`.

```groovy
repositories {
    mavenCentral()
}
dependencies {
    implementation 'com.enioka.scanner:enioka-scan-core:3.0.0:aar'
}
```

More artefacts are be required to add compatibility to your devices, the full list and compatibility
matrix are available in the documentation.

## Documentation

You can learn more about **enioka Scan** by reading the [official documentation](https://enioka-scan.readthedocs.io/en/latest/).

Most notably:
- [The quick-start guide](https://enioka-scan.readthedocs.io/en/latest/quickstart.html)
- [Dependencies and compatible devices](https://enioka-scan.readthedocs.io/en/latest/dependencies.html)
- [The API reference](https://enioka-scan.readthedocs.io/en/latest/api/index.html)
- [How to develop for enioka Scan](https://enioka-scan.readthedocs.io/en/latest/develop.html)