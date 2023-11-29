# enioka Haute Couture Android Barcode Scanning Library

:::{admonition} WIP
:class: attention

This documentation is a work in progress.
:::

This library makes the integration of all barcode scanners easy in any Android application,
avoiding vendor lock-in and lowering the cost of advanced scanner integration.

It is compatible with:
- Zebra EMDK devices (which comprise most of their integrated systems like the TC25, TC75, WT6000...)
- Zebra Bluetooth scanners
- Honeywell AIDC integrated devices (including CN* devices)
- Athesi SPA43
- GeneralScan Bluetooth rings
- And a few others, check full compatibility table below.

When there are no compatible hardware devices available, the library provides a camera reader based 
on ZBar (default) or ZXing.

Through a common abstraction, it provides access to the following methods (provided the hardware supports them):
- press/release the scanner's trigger
- pause/resume scanning abilities
- disconnect/reconnect scanners
- enable/disable illumination from the scanner
- enable/disable colored LEDs
- set scanner enabled symbologies

Finally, it provides a ready to use Service that handles scanner lifecycles, as well as a template 
Activity, and a sample demo application, allowing to use scanners in a matter of minutes.

:::{toctree}
:numbered:
:maxdepth: 3
:titlesonly:

quickstart
dependencies
api/index
dev/index
:::