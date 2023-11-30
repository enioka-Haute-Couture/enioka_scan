# enioka Haute Couture Android Barcode Scanning Library

:::{admonition} WIP
:class: attention

This documentation is a work in progress.
:::

This library makes the integration of all barcode scanners easy in any Android application,
avoiding vendor lock-in and lowering the cost of advanced scanner integration.

It is compatible with a wide variety of scanning devices, integrated or external, from different
vendors such as Zebra, Honeywell, Athesi and more. 

When there are no compatible hardware devices available, the library provides a camera reader based 
on [ZBar](https://mvnrepository.com/artifact/me.dm7.barcodescanner/zbar) (default) or [ZXing](https://mvnrepository.com/artifact/com.google.zxing/core).

Through a common abstraction, it provides access to the following methods (provided the hardware supports them):
- press/release the scanner's trigger
- pause/resume scanning abilities
- disconnect/reconnect scanners
- enable/disable illumination from the scanner
- enable/disable colored LEDs
- set scanner enabled symbologies

Finally, it provides a ready to use Service that handles scanner lifecycles, as well as a template 
Activity, and a demo application, allowing you to use scanners in a matter of minutes.

:::{toctree}
:numbered:
:maxdepth: 3
:titlesonly:

quickstart
dependencies
guides/index
api/index
develop
:::