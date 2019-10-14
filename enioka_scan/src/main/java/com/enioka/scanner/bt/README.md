# Bluetooth SPP connector SDK

## Goal

The library provides a small SDK to help creating connectors to most Bluetooth (BT) scanners (handheld matchbox scanners, ring scanners...)

It is centered on devices which use the SPP (Serial Port Profile) BT profile - the case for most if not all evolved BT scanners (while simple/cheap BT scanners are most often dumb HID/keyboards without SPP).
Note that most devices that are MFi-certified (Made For iOS) are also SPP compatible.

All the connection details are handled by the SDK, and the connector simply has to deal with the specific protocol needed by a device category, using a set of provided interfaces.
This protocol is usually described inside the integration documentation or the programming guide of their respective devices.

## Why this SDK

Most of the time, the device manufacturer provides a dedicated SDK for their devices.
So instead of using this generic SDK, it is simpler to just create a connector using the vendor SDK (and some are actually available from the authors of this library).

However:
* Most vendor SDK (perhaps even all?) are rather OSS unfriendly and just cannot be redistributed or even linked to.
* Vendor SDK tend to consider they are alone in the world, notably when using the Bluetooth manager, while a point of eniokascan is to allow the use of multiple devices from multiple vendors without thinking twice about it.
* Many mid-priced devices actually have incomplete/limiting SDKs, even for the rather small needs of this library.

Therefore, a generic BT SDK was created. Note however that the connectors made using this SDK will likely be compatible with less devices than those created from vendor SDK.

## Pairing process

Coming soon(TM).

For now, the SDK only deals with BT slave devices which are already paired with the Android device, and master devices.

## Device discovery process

On startup, the library lists all BT provider services. A provider implements BtSppScannerProvider, extends Service and references itself in the manifest, allowing it to be used as a bound service. 

### Slave devices (Android opens the connection to the scanners)

It then lists all BT slave devices which are already paired and feature the SPP BT service.

It connects to each one. Devices which fail to connect are ignored.

For each device, each BT provider service is called in turn and offered the device. The provider is responsible for identifying if it can handle the device or not.
The first provider answering yes wins and search stops for this BT device.

### Master devices (the scanner opens the connection)

On startup, the library keeps an SPP listener open for one minute. Devices which connect during that time are then handled exactly as for slave devices.

Listen can be reopened for one minute using an API. This should not be abused, as this is battery consuming.

### How to identify a device

In both cases, the connector is responsible for identifying if it can handle a BT device. There are many usual methods:

* Metadata
  * MAC address: all BT interface vendors have a dedicated MAC prefix, which can be found [here](https://www.adminsub.net/mac-address-finder/). But this is often not reliable at all, for the scanner vendors all use the same BT interface providers. For example, the RS-6000 from Zebra uses a Texas Instrument interface, which can also be found in some Honeywell devices... Also, different generations of the same device may not use the same BT interface.
  * BT services. All BT services are also prefixed! Prefixes can be found [here](https://www.bluetooth.com/specifications/assigned-numbers/16-bit-uuids-for-members/). The issue here is that most of the time the devices only advertise the SPP service. But if there are vendor-specific services available, this is quite reliable.
  * Naming conventions on the BT device name, like a name prefix. In that case, the connector must provide a way to parameter the convention. As this has impacts on daily usability, this is a last resort.
* Commands
  * All devices have specific commands (and giving a simple API to them is the very goal of this library!). So trying to run a command, like "give me your model and version", available on most devices, is often the best way to discriminate between devices. That being said:
    * remember to always set a low timeout for detection commands - on a wrong device, the detection commands will likely do not return anything at all, and certainly not the expected end of answer delimitor.
	* different SDKs do expect different protocols, including different ways to acknowledge a command. This means a command from one SDK may put the scanner in a weird state when trying commands for another scanner type! Therefore, a connector must always begins its detection sequence with an ACK or any equivalent way to clear state.
	* This method also has the advantage of allowing to take device version into account.

The library remembers (successful) associations between device and connector, so the detection is only run once for the same device, even across reboots.

## Commands

The main goal of a connector is to translate API verbs into something which can be understood by the scanner and vice-versa.
Most interactions take place in what we call a "command".

A command is a way to run operations on the scanner: make it beep, enable a LED, etc.

Each command should be a dedicated class implementing Command<ExpectedResultClass>.

Commands are asynchronous, as bluetooth operations should always be asynchronous (and like most IO operations, BT or not, should be).


Only a single command is run at a time. This is a simplification, as some devices *may* allow multiple commands at the same time. This is however rare - most protocols are half-duplex anyway. Therefore, for sanity's sake, the SDK will always wait for an ACK of some form for the current command before sending another. Commands are queued. Callbacks are used to signal end of commands.

Note: there is no need to implement every possible command! Just think extensible and implement what is needed for the Scanner interface.

Also, each command is associated to a specific timeout.


### Parsing data from the scanner

The scanner sends data, either as the result of a command (such as: give me the value of configuration XXX) or as the result of an operation on the scanner itself (such as: new data scanned).

Parsing this data is highly specific to the device familly and is the one of the two main roles of a SPP connector. This is done by implementing the ScannerDataParser interface.
How the connector actually implements the interface is free. Please note that:

* the data given to the parser may be chunked (multiple buffers, and therefore multiple calls to the parser, for a single "message") (layer 4)
* the scanner protocol may itself feature chunking (layer 7)

A normalization of errors exists in MessageRejectionReason.

Most parsers will use two steps:
* parse the enveloppe/tokenize the stream. For example, identify the fixed header defined by the protocol and the data/payload part of the message.
* parse payload itself. For example, parse the "parameter value" payload (isolated in step one).

At the end of the parsing, the actual payload (if any) is returned by the parser in a BtParsingResult structure. The payload can be an instance of any Java class - this is highly device specific too. Some shared payloads can of course be reused, such as the Barcode class.

The SDK will then use this to call any callbacks **registered on this data type**.


## Conventions

Leave a small description of the protocol in md format inside the connector package.
