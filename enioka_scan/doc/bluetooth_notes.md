# Bluetooth SPP connector SDK

## Goal

The library provides a small SDK to help creating connectors to most Bluetooth (BT) scanners (handheld matchbox scanners, ring scanners...)

It is centered on devices which use the SPP (Serial Port Profile) BT profile - the case for most if not all evolved BT scanners (with simple/cheap BT scanners being dumb HID/keyboards without SPP).
Note that most devices that are MFi-certified (Made For iOS) are also SPP compatible.

All the connection details are handled by the SDK, and the connector simply has to deal with the specific protocol needed by a device category, using a set of provided interfaces.
This protocol is usually described inside the integration documentation or the programming guide of their respective devices.

## Why this SDK

Most of the time, the device manufacturer provides a dedicated SDK for their devices.
So instead of using this generic SDK, it is simpler to just create a connector using the vendor SDK (and some are actually available from us).

However:
* Most vendor SDK (perhaps even all?) are rather OSS unfriendly and just cannot be redistributed or even linked to.
* Vendor SDK tend to consider they are alone in the world, notably when using the Bluetooth manager, while a point of eniokascan is to allow the use of multiple devices from multiple vendors without thinking twice about it.
* Many mid-priced devices actually have incomplete/limiting SDKs, even for the rather small needs of this library.

Therefore, a generic BT SDK was created. Note however that the connectors made using this SDK will likely be compatible with less devices than those created from vendor SDK.

## Pairing process

Coming soon(TM).

For now, the SDK only deals with BT devices which are already paired with the Android device.

## Device discovery process

On startup, the library lists all BT provider services.

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
	* different SDKs do expect different protocols, including different ways to acknowledge a command. This means a command from one SDK may put the scanner in a weird state when trying commands for another scanner type! Therefore, the library will always reset the Bluetooth socket (disconnect/reconnect) before proposign it to the connector. This makes that kind of detection rather costly, and visible to the user as devices tend to beep on connection. DOES NOT WORK FOR MASTER DEVICES ARGH!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	* different SDKs do expect different protocols, including different ways to acknowledge a command. This means a command from one SDK may put the scanner in a weird state when trying commands for another scanner type! Therefore, a connector must always begins its detection sequence with an ACK or any equivalent way to clear state.
	* This method also has the advantage of allowing to take device version into account

The library remembers associations between device and connector, so the detection is only run once for the same device, even across reboots.

## Commands

(TODO - notes)

BtDevice.runCommand => asynchronous.

A single command is run at a time. Commands are queued. End of command: a callback?
* issue: command not responding/weird device state. => timeout + always ACK after command.

Results: they are given on the BtDevice.inputHandler (buffer!)


Some commands do not receive any ACK from the device. (GeneralScan...)
Most just wait for ACK.
Some commands have multiple answers (Honeywell) or answer a different command (GS) !

De façon générale, le parseur est assez spécifique à chaque SDK... On verra après comment factoriser si besoin.

Retour d'information : spécifique selon commande. Par exemple : détection nuémro de version.

Reste la gestion du timeout.

Besoin d'une commande SDK qu fait ACK pour tout simplifier.


But:
* identifier le device
* permettre de faire la conf initiale (dont symbologies)
* beep
* illumination
* mode de scan
* LED

Côté provider : rendre synchrone ou pas ?
* en tout cas âs le pb du SDK BT - pb côté provider.

React to unsollcitied data! (get parameter barcode read for example...)

**When data comes from the stream, the goal is to parse the data correctly and call the right callback with said data.**
data from stream => response parser, specific to each language.
Parser 
* tokenizes the stream. (cut ! This highly depends on the structure of the scanner protcol)
* selects the GUT sub message parser according to the opcode of the tokens
* gives data to the sub parser.
* sub parser calls callback COMING FROM ?????
* sub parser sends ACK if needed.

Partage de clef pour le callback... comment le rendre regardable ?  TYPE: btDevice.runCommand(new LedOn(RED), new Callback {....}).
=> faut que la commande donne sa clef... et donc lien entre la clef du subparser et commande... BOF.
en même temps : dans l'enum, on peut préciser une assocaition non ?

Callback : enregistrer par op^code ??????? de tte façon pas sur que réponse = celle attendue par cette instanc de commande.

Comment passer des paramètres de façon générique ????

Passer les paramètres :
* constructeur de la commande. => une instance par commande, bouh mais pas trop.
* un truc générique dans une méthode de la commande de base... (pas typé : bouh !)
* des options sur instance unique et donc pb potentiel d'état réutilisé...

Aussi, une commande doit pouvoir lister les valeurs autorisées de façon dynamique.


*Syndrome du callback.*
Un parseur doit apeller le "bon" callback. I.e. celui avec la bonne classe de données. (? extends basedata, Null possible)


No need for every possible command! Just think extensible.

## Conventions

Leave a small description of the protocol in md format inside the connector package.
