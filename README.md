# @madusha98/expo-google-signin

A Google Sign-In module for Expo applications that uses the new Credential Manager and Google Identity Service for authentication and authorization on Android.

> **Note:** This module currently supports **Android only**.

## Features

- Uses the modern Credential Manager and Google Identity Service APIs
- Supports authentication with Google accounts
- Retrieves server auth code for token exchange with your backend
- Configurable OAuth scopes
- Typescript support

## Installation

```bash
# Using npm
npm install @madusha98/expo-google-signin

# Using yarn
yarn add @madusha98/expo-google-signin

# Using Expo
npx expo install @madusha98/expo-google-signin
```

## Setup

### Android Setup

1. Configure your Google API project:
   - Go to the [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select an existing one
   - Configure the OAuth consent screen with the necessary information
   - Create OAuth 2.0 credentials (Web client ID)
   - Add your SHA-1 signing certificate to your project



## Usage

```typescript
import { signInAsync } from '@madusha98/expo-google-signin';

// Basic sign-in with default scopes (profile and email)
async function signInWithGoogle() {
  try {
    const result = await signInAsync('YOUR_WEB_CLIENT_ID');
    console.log('Sign-in successful:', result);
    
    // Send serverAuthCode to your backend to exchange for tokens
    if (result.serverAuthCode) {
      // Call your backend API to exchange code for tokens
      exchangeCodeForTokens(result.serverAuthCode);
    }
  } catch (error) {
    console.error('Google Sign-In Error:', error);
  }
}

// Sign-in with custom scopes
async function signInWithCustomScopes() {
  try {
    const scopes = [
      'https://www.googleapis.com/auth/userinfo.profile',
      'https://www.googleapis.com/auth/userinfo.email',
      'https://www.googleapis.com/auth/calendar'
    ];
    
    const result = await signInAsync('YOUR_WEB_CLIENT_ID', scopes);
    console.log('Sign-in with custom scopes successful:', result);
  } catch (error) {
    console.error('Google Sign-In Error:', error);
  }
}
```

## API Reference

### signInAsync(webClientId: string, scopes?: string[]): Promise<SignInResult>

Initiates the Google Sign-In flow.

#### Parameters:

- `webClientId` (string): Your OAuth 2.0 web client ID from Google Cloud Console
- `scopes` (string[] | optional): Array of OAuth scopes to request. If not provided, defaults to profile and email scopes.

#### Returns:

A Promise that resolves to a `SignInResult` object:

```typescript
interface SignInResult {
  idToken?: string;         // The ID token from Google (if available)
  displayName?: string;     // User's display name
  familyName?: string;      // User's family name
  givenName?: string;       // User's given name
  profilePictureUri?: string; // URL to user's profile picture
  phoneNumber?: string;     // User's phone number (if available)
  serverAuthCode?: string;  // Auth code to exchange for access/refresh tokens
}
```

## Commonly Used Scopes

- `https://www.googleapis.com/auth/userinfo.profile`: User's basic profile information
- `https://www.googleapis.com/auth/userinfo.email`: User's email address
- `https://www.googleapis.com/auth/calendar`: Access to user's calendar
- `https://www.googleapis.com/auth/drive`: Access to user's Google Drive
- `https://www.googleapis.com/auth/youtube`: Access to user's YouTube account

## Token Exchange Flow

The `serverAuthCode` returned by this module should be sent to your backend server. Your server can then exchange it for access and refresh tokens using the OAuth 2.0 token endpoint:

```
POST https://oauth2.googleapis.com/token
```

Request parameters:
- `code`: The serverAuthCode received from the sign-in process
- `client_id`: Your web client ID
- `client_secret`: Your client secret (keep this secure on your server)
- `redirect_uri`: Your redirect URI
- `grant_type`: "authorization_code"

## Troubleshooting

- **Sign-in fails with "No current activity"**: Ensure you're calling the sign-in method when an activity is available.
- **No accounts available**: Make sure the user has a Google account set up on their device.
- **Authorization failed**: Check that your client ID is correct and that you've properly set up your OAuth consent screen.

## License

MIT
