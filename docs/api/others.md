# Other useful classes

The library contains a lot of other smaller classes you may need to interact with. This page will
regroup most of them.

## The `BarcodeType` enum

This enum contains every barcode symbology officially supported by **enioka Scan**. Whenever a
scanner reads data, it will try to match its symbology with one of these. This list is not final and
will likely be extended in the future to include other symbologies. This is also the enum to use
when configuring a scanner.

:::{cpp:enum} BarcodeType

:CODE39: The Code 39 symbology
:CODE39_FULL_ASCII: The Code 39 full ASCII symbology
:CODE11: The Code 11 symbology
:CODE93: The Code 93 symbology
:CODE128: The Code 128 symbology
:CODABAR: The COADABAR symbology
:INT25: The Interleaved-2-Of-5 symbology
:DIS25: The Discrete-2-of-5 symbology
:QRCODE: The regular QR Code symbology
:UPCA: The UPC-A Code symbology
:UPCE: The UPC-E Code symbology
:EAN8: The EAN 8 symbology
:EAN13: The EAN 13 symbology
:DATAMATRIX: The DATAMATRIX symbology
:GRIDMATRIX: The Grid Matrix symbology
:MAXICODE: The MaxiCode symbology
:GS1_DATABAR: The GS1-DataBar (RSS) symbology
:GS1_DATABAR_LIMITED: The GS1-DataBar-Limited (RSS limited) symbology
:GS1_DATABAR_EXPANDED: The GS1-DataBar-Expanded (RSS expanded) symbology
:ISBN10: The ISBN-10 symbology
:GS1_128: The GS1-128 (EAN-128) symbology
:MSI: The MSI symbology
:PDF417: The PDF417 symbology
:AZTEC: The Aztec 2D symbology
:AZTEC_RUNE: The Aztec Runner symbology
:JAPAN_POST: The Japan Postal symbology
:DUTCH_POST: The Dutch  Postal (or KIX-Code) symbology
:BRITISH_POST: The Royal Mail Postal symbology
:AUS_POST: The Australia Post Postal symbology
:KOREA_POST: The Korea Post Postal symbology
:CHINA_POST: The China Post Postal symbology
:CANADIAN_POST: The Canada Post Postal symbology
:HAN_XIN: The Han Xin symbology
:UNKNOWN: Any other symbology not currently part of the enum. When used during symbology 
    configuration, means the scanner should allow all symbologies.
:::

## Supported barcode types matrix

| Barcode type         | provider-os-athesi-spa43 | provider-os-bluebird | provider-os-honeywell-integrated | provider-cs-proglove | provider-cs-honeywell | provider-cs-koamtac | provider-cs-zebra | provider-os-zebra-dw | camera Zxing  | camera Zbar |
|----------------------|--------------------------|----------------------|----------------------------------|----------------------|-----------------------|---------------------|-------------------|----------------------|---------------|-------------|
| CODE 39              | ✓                        | ✓                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| CODE 39 FULL ASCII   | x                        | x                    | x                                | x                    | ✓                     | x                   | ✓                 | x                    | x             | x           |
| CODE 11              | ✓                        | x                    | ✓                                | x                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| CODE 93              | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| CODE 128             | ✓                        | ✓                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| CODABAR              | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| INT25                | ✓                        | ✓                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| DIS25                | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| QRCODE               | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| UPCA                 | x                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| UPCE                 | x                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| EAN8                 | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| EAN13                | ✓                        | ✓                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| DATAMATRIX           | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | x           |
| GRIDMATRIX           | ✓                        | x                    | ✓                                | x                    | x                     | x                   | x                 | ✓                    | x             | x           |
| MAXICODE             | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | x           |
| GS1 DATABAR EXPANDED | x                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| GS1 DATABAR          | x                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| GS1 DATABAR LIMITED  | x                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| ISBN 10              | x                        | x                    | x                                | x                    | x                     | x                   | x                 | x                    | x             | ✓           |
| GS1 128              | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| MSI                  | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| PDF417               | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | ✓           |
| AZTEC                | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | ✓             | x           |
| AZTEC RUNNER         | x                        | x                    | x                                | ✓                    | x                     | ✓                   | ✓                 | x                    | x             | x           |
| JAPAN POSTAL         | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| DUTCH POSTAL         | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| UK POSTAL            | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| AUSTRALIA POSTAL     | ✓                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| KOREAN POSTAL        | ✓                        | x                    | ✓                                | x                    | ✓                     | ✓                   | x                 | ✓                    | x             | x           |
| CHINA POSTAL         | ✓                        | x                    | ✓                                | x                    | ✓                     | ✓                   | ✓                 | x                    | x             | x           |
| CANADIAN POSTAL      | x                        | x                    | ✓                                | ✓                    | ✓                     | ✓                   | ✓                 | ✓                    | x             | x           |
| HAN XIN              | ✓                        | x                    | ✓                                | x                    | ✓                     | ✓                   | x                 | ✓                    | x             | x           |
