import {NativeModules} from 'react-native';

const EniokaScanModule = NativeModules.EniokaScanModule

export function startActivityByName(targetActivity: string, intentExtras: Record<string, boolean | string[]>): void {
    EniokaScanModule.startActivityByName(targetActivity, intentExtras);
    return;
}

export function startScannerCompatActivity(intentExtras: Record<string, boolean | string[]>): void {
    EniokaScanModule.startActivityByName("com.enioka.scanner.activities.ScannerCompatActivity", intentExtras);
    return;
}

export function getDefaultExtras(): Record<string, boolean | string[]> {
    return {
        "startSearchOnServiceBind": true,
        "useBlueTooth": true,
        "allowIntentDevices": true,
        "allowLaterConnections": true,
        "allowInitialSearch": true,
        "allowPairingFlow": true,
        "allowedProviderKeys": [],
        "excludedProviderKeys": [],
        "symbologySelection": [],
    };
}