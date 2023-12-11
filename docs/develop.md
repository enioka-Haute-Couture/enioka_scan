# Development

This page will summarize how to modify **enioka Scan**, whether it is to contribute to the project 
or for your own needs.

## Developing for Android

In order to start developing and testing the library:
- Have [Android Studio](https://developer.android.com/studio) installed and open the project with it
- Connect a compatible android device via USB, it should then show up in Android Studio's device
  manager
- Run the demo app with the android device selected. The app includes the library and all available
  providers

In case the android device is not detected by Android Studio:
- Make sure the device is in developer mode and has USB Debugging enabled
- Make sure the USB cable supports data transfer (some cables only support charging)

## Adding to the ReadTheDocs documentation

To test documentation changes locally, you need to install Sphinx and other dependencies. Using 
python 3, you can run `pip install -r ./docs/requirements.txt` in your virtual environment.

In order to build the documentation, run `sphinx-build -a ./docs <build-dir>`.

To view the generated documentation, use the url `file://<build-dir>/index.html` in your web
browser.

All documentation is written in markdown. Due to the lack of proper java support, the API reference
was "hacked" using default `method` directives for functions, and `cpp:var` or `cpp:enum` directives
for attributes and enums respectively. 

Note that anchor links to methods and members are not well supported, so in general it is better to
link to the parent class or interface rather than the method or attribute itself.

:::{seealso}

You may check the 
[Sphinx](https://www.sphinx-doc.org/en/master/index.html) and 
[MyST](https://myst-parser.readthedocs.io/en/latest/index.html) documentations for more details on 
how to format documentation content. 
:::

## Adding another SDK / Scanner provider

A scanner SDK contains at least a [`Scanner` implementation](api/scanner.md#the-scanner-api) 
(interfacing between the code and the device) and a 
[`ScannerProvider` implementation](api/scanner_provider.md#the-scannerprovider-api) (handling 
scanner creation and device compatibility checks).

In order for a new scanner SDK to be found by the library, the
[`ScannerProvider` implementation](api/scanner_provider.md#the-scannerprovider-api) implementation 
needs to be declared as a service in its `AndroidManifest.xml`, with an intent-filter containing the
action `com.enioka.scan.PROVIDE_SCANNER` for "regular" devices, and 
`com.enioka.scan.PROVIDE_SPP_SCANNER` for bluetooth devices.

The associated Java class does not need to extend Android's `Service` class(the 
`tools:ignore="Instantiatable"` attribute may be added to the service in the manifest), 
but it must provide a public default constructor as it will be instantiated using `Class.getName()`.

:::{seealso}

* The [`Scanner` interface](api/scanner.md) documentation
* The [`ScannerProvider` interface](api/scanner_provider.md) documentation
* The sources of various scanner providers on the 
  [GitHub repository](https://github.com/enioka-Haute-Couture/enioka_scan), mainly the 
  [Mock SDK](https://github.com/enioka-Haute-Couture/enioka_scan/tree/master/enioka_scan_mock)
:::

## Library tests

While not every aspect of the code may be easily tested, some parts can and should be. For example,
parsers and translation methods used to convert enioka Scan API methods to device-specific commands
should be unit-tested to ensure payloads are properly handled.

The repository contains GitHub Action pipelines to build the library and launch both JUnit and
Android Instrumented tests for both Android API levels 19 (the minimum supported version) and 28
(the target version). 

## Release process

Once enough features or fixes are merged into `master`, a new version tag will be pushed by a 
repository owner/maintainer. This will trigger a GitHub Action workflow to create a release with an
artefact of the core library, and will upload all artefacts to Maven Central.

:::{warning}

The release tag ***must*** be pushed from the CLI, as a tag or release created from the
GitHub web UI will fail to trigger the workflow.
:::
