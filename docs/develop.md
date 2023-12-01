# Development

:::{admonition} WIP
:class: attention

This documentation is a work in progress.
:::

:::{admonition} TODO
:class: attention

* Edit source links to redirect to the API reference or guides.
:::

This page will summarize how to modify enioka Scan, whether it is to contribute to the project or
for personal use.

## Developing for Android

In order to start developing and testing the library:
- Have Android Studio installed and open the project with it
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

All documentation is written in markdown. 

:::{seealso}

You may check the 
[Sphinx](https://www.sphinx-doc.org/en/master/index.html) and 
[MyST](https://myst-parser.readthedocs.io/en/latest/index.html) documentations for more details on 
how to format documentation content. 
:::

## Adding another SDK / Scanner provider

A scanner SDK contains at least a [`Scanner`][scanner-api] implementation (interfacing between the 
code and the device) and a [`ScannerProvider`][scanner-provider-api] implementation (handling 
scanner creation and device compatibility checks).

In order for a new scanner SDK to be found by the library, the 
[`ScannerProvider`][scanner-provider-api] implementation needs to be declared as a service in its 
`AndroidManifest.xml` with an intent-filter containing the action `com.enioka.scan.PROVIDE_SCANNER`
for "regular" devices, and `com.enioka.scan.PROVIDE_SPP_SCANNER` for bluetooth devices.

The associated Java class does not need to extend Android's `Service` class(the 
`tools:ignore="Instantiatable"` attribute may be added to the service in the manifest), 
but it must provide a public default constructor as it will be instantiated using `Class.getName()`.

:::{seealso}

See the [Mock SDK][mock-sdk] for an example of addon SDK. 
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

[mock-sdk]: https://github.com/enioka-Haute-Couture/enioka_scan/blob/master/enioka_scan_mock
[scanner-api]: https://github.com/enioka-Haute-Couture/enioka_scan/blob/master/enioka_scan/src/main/java/com/enioka/scanner/api/Scanner.java
[scanner-provider-api]: https://github.com/enioka-Haute-Couture/enioka_scan/blob/master/enioka_scan/src/main/java/com/enioka/scanner/api/ScannerProvider.java
