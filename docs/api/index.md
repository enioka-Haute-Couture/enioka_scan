# API reference

% Due to the limitations of Sphinx and MyST, documentation formatting was 'hacked' a bit by using
% python and cpp directives as Java is not natively supported and has no proper third-party option.

:::{admonition} WIP
:class: attention

This documentation is a work in progress.
:::

This section contains more in-depth info about important classes and interfaces of **enioka Scan**.

:::{toctree}
:caption: For most users
:maxdepth: 2

scanner
scanner_service
scanner_callbacks
scanner_activity
camera
others
:::

:::{toctree}
:caption: For advanced users and enioka Scan developpers
:maxdepth: 2

lazerscanner
scanner_provider
:::

% API guides for the BluetoothScanner API may be helpful for provider developpers but is not a 
% priority. A full bluetooth guide may be needed.