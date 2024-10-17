# Compatibility layer for various android application frameworks

enioka-scan provides npm packages that aim at facilitating its integration within already existing apps.

## For all android projects
You need to add the matching dependencing, depending on what physical device you wish to use.

in ``android/app/build.gradle``
```gradle
dependencies {
    //core library
    implementation group: 'com.enioka.scanner', name: 'enioka-scan-core', version: '3.0.1'
    //use camera
    implementation group: 'com.enioka.scanner', name: 'provider-os-camera', version: '3.0.1'
    
    //provider dependant, here is for honeywell scanner.

    implementation group: 'com.enioka.scanner', name: 'provider-os-honeywell-integrated', version: '3.0.1'
}
```
*(using  the ``mavenCentral()`` repository )*


You can then implement your own logic and Activities by creating a class extending ``ScannerCompatActivity``.

```java
import com.enioka.scanner.activities.ScannerCompatActivity;

public class MyScanActivity extends ScannerCompatActivity {

    @Override
    public void initCamera(){
        super.initCamera();
        
        //initCamera logic...
    }

    @Override
    public void onData(List<Barcode> data) 
    {
    //onData Logic
    }

    //rest of the Activity logic      
}

```

## Cordova
Cordova has its own plugin system
You can add the compat library using 

```bash
$ cordova plugin add enioka-scan-compat
```

in any .js file :

```js

function foo() 
{
    window.startActivityNow(
        {className:"com.example.myapp.MyScanActivity"},
        successHandler,
        errorHandler
    )
}
```


## Capacitor
Capcitor supports cordova modules installed with npm

``` bash
$ npm install enioka-scan-compat-
```



```html
<script src="js/index.js" type="module"></script>
```

```js
function foo() {
    window.startActivityNow(
        {className:"com.example.myapp.MyScanActivity"},
        successHandler,
        errorHandler
    )
}
```

## React Native

``` bash
$ npm install enioka-scan-compat-
```

```tsx
import { startActivity } from 'enioka-scan-compat-'

<Button title = "startActivity" onPress={() => startActivity("com.example.myapp.MyScanActivity")}>
</Button>
```

## Troubleshooting

- in gradle.properties : 
``android.enableJetifier=true``
- in AndroidManifest.xml
``android.allowBackup=true``
