import { NativeModules } from 'react-native';

const ActivityStarterModule = NativeModules.ActivityStarterModule

export function startActivityByName(targetActivity: String): void {
  ActivityStarterModule.startActivityByName(targetActivity);
  return;
}
