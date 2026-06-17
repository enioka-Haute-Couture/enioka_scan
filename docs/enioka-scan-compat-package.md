# Compatibility plugins for various frameworks

enioka-scan provides npm packages that facilitate its integration with non-java applications by
allowing you to use the provided activities.

Currently, plugins exist for the following Frameworks:
- React Native
- Cordova / Capacitor

## For all frameworks

The npm plugins only facilitate the integration of the provided UI, however you still need to add
the Maven Central dependencies in the `android/app/build.gradle` of your project:

```groovy
dependencies {
    // core library - mandatory
    implementation group: 'com.enioka.scanner', name: 'enioka-scan-core', version: 'X.Y.Z'
    // wanted device-specific providers (e.g. Camera and Honeywell integrated)
    implementation group: 'com.enioka.scanner', name: 'provider-os-camera', version: 'X.Y.Z'
    implementation group: 'com.enioka.scanner', name: 'provider-os-honeywell-integrated', version: 'X.Y.Z'
}
```

The plugin exposes 3 functions:

:::{method} startActivityByName(string name, Record<string, boolean | string[]> intentExtras) -> void

Starts the requested activity with the given intent extras.

:param string name: The full classpath name of the activity, i.e. "com.enioka.scanner.activities.ScannerCompatActivity"
:::

:::{method} startScannerCompatActivity(Record<string, boolean | string[]> intentExtras) -> void

Starts the ScannerCompatActivity with the given intent extras.
:::

:::{method} getDefaultExtras() -> Record<string, boolean | string[]>

:returns: The default value of the intent extras expected by the Scanner Service.
:::

In the Cordova/Capacitor version of the plugin, the "startActivityByName" and
"startScannerCompatActivity" functions take two extra parameters: a successCallback and
errorCallback.

## React Native

The React Native integration dependency can be installed with the following command:

```bash
$ npm install @enioka/enioka-scan-react-native-integration
```

You then need to activate the provided package in your android sources:

```kotlin
import com.enioka.scanner.integration.EniokaScanPackage

class MainApplication : Application(), ReactApplication {

  override val reactHost: ReactHost by lazy {
    getDefaultReactHost(
      context = applicationContext,
      packageList =
        PackageList(this).packages.apply { 
          // Add the package here
          add(EniokaScanPackage())
        },
    )
  }
  // ...
}
```

After that, the provided functions can be used in your React components:

```tsx
import { startScannerCompatActivity, getDefaultExtras } from '@enioka/enioka-scan-react-native-integration'

<Button title="startActivity"
        onPress={() => startScannerCompatActivity(getDefaultExtras()}>
</Button>
```

## Cordova and Capacitor

Cordova has its own plugin system. You can add the integration dependency using:

```bash
$ cordova plugin add @enioka/enioka-scan-cordova-capacitor-integration
```

Capacitor supports Cordova modules installed with npm, it therefore uses the same dependency:

```bash
$ npm install @enioka/enioka-scan-cordova-capacitor-integration
```

You can then instantiate the scanner activity in any .js file after your app was initialized:

```js
function foo() 
{
    EniokaScanPlugin.startScannerCompatActivity(
        EniokaScanPlugin.getDefaultExtras(),
        function(res){}, // success callback
        function(res){}, // error callback
    );
}
```

## Troubleshooting

Because your application's manifest will have to be merged with the enioka Scan dependencies, make
sure the following properties match the ones used in enioka Scan (which are not the default values):

- in gradle.properties:
  `android.enableJetifier=true`
- in AndroidManifest.xml:
  `android.allowBackup=true`

## Improvements

As of enioka scan 3.1.0, these plugins (both in version 1.0.0 on NPM) only let you launch the
default implementation of the ScannerCompatActivity.

In upcoming updates, a passthrough to the underlying ScannerService will be made available, allowing
you to subscribe to scan updates and manipulate the scanner.

However, there is no plan to add ways to extend or manipulate the default UI provided alongside the
ScannerCompatActivity. For such use cases, you will need to write your own Java/Kotlin Activity.
