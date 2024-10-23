
# Compatibility layer for various android application frameworks

enioka-scan provides npm packages that facilitate its integration with non-java applications by allowing you to use the provided Scanner and Camera activities.

## For all android projects

The npm plugins only facilitate the integration of the provided UI, however you still need to add the usual Maven Central dependencies in ``android/app/build.gradle``:

```gradle
dependencies {
    // core library
    implementation group: 'com.enioka.scanner', name: 'enioka-scan-core', version: 'X.Y.Z'
    // wanted device-specific providers (e.g. Camera and Honeywell integrated)
    implementation group: 'com.enioka.scanner', name: 'provider-os-camera', version: 'X.Y.Z'
    implementation group: 'com.enioka.scanner', name: 'provider-os-honeywell-integrated', version: 'X.Y.Z'
}
```

## Cordova and Capacitor

Cordova has its own plugin system. You can add the integration dependency using:

```bash
$ cordova plugin add @enioka/enioka-scan-cordova-capacitor-integration
```

Capcitor supports Cordova modules installed with npm, it therefore uses the same dependency:

```bash
$ npm install @enioka/enioka-scan-cordova-capacitor-integration
```

You can then easily instantiate the scanner activity in any .js file :

```js
function foo() 
{
    window.startActivityNow(
        { className:"com.enioka.scanner.activities.ScannerCompatActivity" },
        successHandler,
        errorHandler
    )
}
```

## React Native

The React Native integration dependency can be installed with the following command:

```bash
$ npm install @enioka/enioka-scan-react-native-integration
```

You can then easily instantiate the scanner activity in any .js file used in your HTML page:

```tsx
import { startActivity } from '@enioka/enioka-scan-react-native-integration'

<Button title = "startActivity" onPress={() => startActivity("com.enioka.scanner.activities.ScannerCompatActivity")}>
</Button>
```

## Troubleshooting

Because your application's manifest will have to be merged with the enioka Scan dependencies, make sure the following properties match the ones used in enioka Scan (which are not the default values):

- in gradle.properties:
  ``android.enableJetifier=true``
- in AndroidManifest.xml:
  ``android.allowBackup=true``

## Improvements

So far this setup only allows the launch of an activity without much control on their intent extras, and without much thought put into the Service aspect, as these frameworks are primarily UI-oriented and not designed for deep Android integration.

This means:
- We do not provide any means to easily bind to the Scanner service independently from the provided ScannerCompatActivity. The service should still be available after installing the `core` dependency, so your franework may offer a way to bind to it.
- We do not provide any means to easily extend the UI beyond simply launching the default one, you may be able to extend it in a Java class and use this class in the `startActivity()` call instead, but Java will still be needed.
- We do not provide any way to customize the `startActivity()` intent for now though that will be a feature down the line.
