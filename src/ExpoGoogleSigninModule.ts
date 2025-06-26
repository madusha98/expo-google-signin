import { NativeModule, requireNativeModule } from 'expo';

import {  ExpoGoogleSigninModuleEvents, SignInResult } from './ExpoGoogleSignin.types';

declare class ExpoGoogleSigninModule extends NativeModule<ExpoGoogleSigninModuleEvents> {
  signInAsync(webClientId: string, scopes?: string[], forceCodeForRefreshToken?: boolean): Promise<SignInResult>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoGoogleSigninModule>('ExpoGoogleSignin');
