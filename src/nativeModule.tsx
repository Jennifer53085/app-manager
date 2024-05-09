import { NativeModules } from 'react-native';
const { ControllerModule } = NativeModules;

export const handleSearchApps = (callback:Callback) => ControllerModule.searchApps(callback);

export const handleDeleteApp = (packageName:string,callback:Callback) => ControllerModule.deleteApp(packageName,callback);

