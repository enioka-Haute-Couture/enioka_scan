# Other useful classes

:::{admonition} WIP
:class: attention

This documentation is a work in progress.
:::

The library contains a lot of other smaller classes you may need to interact with. This page will
regroup most of them.

## The `BarcodeType` enum

This enum contains every barcode symbology officially supported by **enioka Scan**. Whenever a
scanner reads data, it will try to match its symbology with one of these. This list is not final and
will likely be extended in the future to include other symbologies. This is also the enum to use
when configuring a scanner.

:::{cpp:enum} BarcodeType

:CODE128: The Code 128 symbology
:CODE39: The Code 39 symbology
:DIS25: The Discrete-2-of-5 symbology
:INT25: The Interleaved-2-Of-5 symbology
:EAN13: The EAN 13 symbology
:QRCODE: The regular QR Code symbology
:AZTEC: The Aztec 2D symbology
:UNKNOWN: Any other symbology not currently part of the enum. When used during symbology 
    configuration, means the scanner should allow all symbologies.
:::
