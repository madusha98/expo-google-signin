// Reexport the native module. On web, it will be resolved to ExpoGoogleSigninModule.web.ts
// and on native platforms to ExpoGoogleSigninModule.ts
export { default } from './ExpoGoogleSigninModule';
export * from  './ExpoGoogleSignin.types';
