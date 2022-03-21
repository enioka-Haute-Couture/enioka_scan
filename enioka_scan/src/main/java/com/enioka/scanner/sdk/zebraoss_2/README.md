This sdk is a rewrite of the `zebraoss` sdk, implementing breaking changes over newly observed data after more testing of the scanner.
It also includes support for SSI over BLE.
The main breaking changes are mainly around scan events and multi-packet headers.
Original sdk somehow works for single packets so it is kept for now, but it should be removed eventually.