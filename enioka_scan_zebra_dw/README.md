# Zebra DataWedge provider

The goal of this plugin is to allow usage of Zebra devices that have the DataWedge management
service installed, which happens mostly on integrated Zebra devices (TC26, TC27, TC55...).

> Warning: there are multiple different providers that can manage those devices! Take care only to
> have the one you want enabled, using the include or exclude provider search parameters.

To use this provider:

* it needs to be in the class path (just add it to the Maven dependencies like
  this: `implementation 'com.enioka.scanner:provider-cs-zebra-dw:x.y.z:aar'`)
* At least one enabled DataWedge profile that:
    * Has scanning enabled
    * Has Intent output enabled, with:
        * Intent name `com.enioka.scanners.zebra.dw.intent.callback.name` (this can be modified by
          changing the string resource named `com.enioka.scanners.zebra.dw.intent.callback.name`)
        * No Intent Category
        * Intent Delivery set to `Broadcast Intent`

Note that in this version, the provider does not modify or create DataWedge profiles. This is being
considered.