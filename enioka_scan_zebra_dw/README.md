# Zebra DataWedge provider

The goal of this plugin is to allow usage of Zebra devices that have the DataWedge management
service installed, which happens mostly on integrated Zebra devices (TC26, TC27, TC55...).

> Warning: there are multiple different providers that can manage those devices! Take care only to
> have the one you want enabled, using the include or exclude provider search parameters.

To use this provider:

* it needs to be in the class path (just add it to the Maven dependencies like
  this: `implementation 'com.enioka.scanner:provider-cs-zebra-dw:x.y.z:aar'`)
* The DataWedge service must be installed on the device and running (enabled or disabled).

The plugin will on startup create or update a DataWedge profile that:

    * is named after your application package name. The profile name can also be specified with a string resource named `enioka_scan_zebra_dw_profile_name`.
    * is enabled. (note that the plugin will also enabled DataWedge itself if the service is running disabled)
    * has scanning enabled
    * has Intent output enabled, with:
        * Intent name `com.enioka.scanners.zebra.dw.intent.callback.name` (this can be modified by
          changing the string resource named `enioka_scan_zebra_dw_intent_name`)
        * No Intent Category
        * Intent Delivery set to `Broadcast Intent`

You are allowed to modify any other configuration option in the profile except those above, which
are reset on each initialization. This ensure you can tweak your scanning configuration as you want,
but are still guaranteed scanning will work on plugin startup.

This includes decoder configuration. The plugin will at runtime (and only at runtime, this is not
persisted in the profile) enable or disable the decoders selected inside your app, but all the other
numerous decoder parameters are free to configure inside the profile.
