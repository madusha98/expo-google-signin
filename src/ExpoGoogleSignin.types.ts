import type { StyleProp, ViewStyle } from 'react-native';

export type OnLoadEventPayload = {
  url: string;
};

export type ExpoGoogleSigninModuleEvents = {
  onChange: (params: ChangeEventPayload) => void;
};

export type ChangeEventPayload = {
  value: string;
};

export type ExpoGoogleSigninViewProps = {
  url: string;
  onLoad: (event: { nativeEvent: OnLoadEventPayload }) => void;
  style?: StyleProp<ViewStyle>;
};

export interface SignInResult {
  idToken?: string;
  displayName?: string;
  familyName?: string;
  givenName?: string;
  profilePictureUri?: string;
  phoneNumber?: string;
  serverAuthCode?: string;
}