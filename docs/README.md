# @madusha98/expo-google-signin

[![npm version](https://badge.fury.io/js/@madusha98%2Fexpo-google-signin.svg)](https://badge.fury.io/js/@madusha98%2Fexpo-google-signin)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A modern Google Sign-In module for Expo applications that leverages the latest Credential Manager and Google Identity Service APIs for seamless authentication and authorization on Android devices.

> **⚠️ Platform Support:** This module currently supports **Android only**. iOS support is planned for future releases.

## 🚀 Features

- ✅ **Modern APIs**: Built with the latest Credential Manager and Google Identity Service
- 🔐 **Secure Authentication**: Implements Google's recommended authentication flow
- 🎯 **Server Integration**: Provides server auth codes for backend token exchange
- 🔧 **Configurable Scopes**: Support for custom OAuth scopes
- 📱 **Expo Compatible**: Designed specifically for Expo managed workflow
- 🔷 **TypeScript Support**: Full TypeScript definitions included
- 🎨 **Easy Integration**: Simple API with comprehensive error handling

## 📦 Installation

```bash
# Using npm
npm install @madusha98/expo-google-signin

# Using yarn
yarn add @madusha98/expo-google-signin

# Using Expo CLI (recommended)
npx expo install @madusha98/expo-google-signin
```

## ⚙️ Setup

### Prerequisites

- Expo SDK 49 or higher
- Android API level 23 or higher
- Google Cloud Console project with OAuth 2.0 configured

### 1. Google Cloud Console Configuration

#### Step 1: Create/Select a Project
1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the **Google Identity Service**

#### Step 2: Configure OAuth Consent Screen
1. Navigate to **APIs & Services** → **OAuth consent screen**
2. Choose **External** user type (unless you have a Google Workspace)
3. Fill in the required information:
   - App name
   - User support email
   - Developer contact information
4. Add your app domain (if applicable)
5. Save and continue through the scopes and test users sections

#### Step 3: Create OAuth 2.0 Credentials
1. Go to **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **OAuth 2.0 Client ID**
3. Select **Web application** as the application type
4. Add authorized redirect URIs (for your backend)
5. Note down the **Client ID** - you'll need this in your app

#### Step 4: Add SHA-1 Certificate
1. Get your app's SHA-1 fingerprint:
   ```bash
   # For debug builds
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   
   # For release builds
   keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
   ```
2. In Google Cloud Console, go to **Credentials**
3. Edit your OAuth 2.0 Client ID
4. Add the SHA-1 fingerprint under **Authorized Android applications**

### 2. Expo Configuration

Add the following to your `app.json` or `app.config.js`:

```json
{
  "expo": {
    "plugins": [
      "@madusha98/expo-google-signin"
    ]
  }
}
```

### 3. Environment Variables

Create a `.env` file in your project root:

```env
EXPO_PUBLIC_WEB_CLIENT_ID=your-web-client-id-here.apps.googleusercontent.com
```

## 🔧 Usage

### Basic Implementation

```typescript
import React, { useState } from 'react';
import { View, Button, Text, Image, Alert } from 'react-native';
import ExpoGoogleSignin, { SignInResult } from '@madusha98/expo-google-signin';

export default function App() {
  const [user, setUser] = useState<SignInResult | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSignIn = async () => {
    setLoading(true);
    try {
      const result = await ExpoGoogleSignin.signInAsync(
        process.env.EXPO_PUBLIC_WEB_CLIENT_ID!
      );
      
      setUser(result);
      console.log('Sign-in successful:', result);
      
      // Send serverAuthCode to your backend
      if (result.serverAuthCode) {
        await exchangeCodeForTokens(result.serverAuthCode);
      }
    } catch (error) {
      console.error('Google Sign-In Error:', error);
      Alert.alert('Sign-In Error', 'Failed to sign in with Google');
    } finally {
      setLoading(false);
    }
  };

  const exchangeCodeForTokens = async (serverAuthCode: string) => {
    // Send to your backend server
    try {
      const response = await fetch('https://your-backend.com/auth/google', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ code: serverAuthCode }),
      });
      
      const tokens = await response.json();
      console.log('Tokens received:', tokens);
    } catch (error) {
      console.error('Token exchange error:', error);
    }
  };

  return (
    <View style={{ flex: 1, justifyContent: 'center', padding: 20 }}>
      {!user ? (
        <Button
          title={loading ? 'Signing in...' : 'Sign in with Google'}
          onPress={handleSignIn}
          disabled={loading}
        />
      ) : (
        <View style={{ alignItems: 'center' }}>
          <Text style={{ fontSize: 18, marginBottom: 10 }}>
            Welcome, {user.displayName}!
          </Text>
          {user.profilePictureUri && (
            <Image
              source={{ uri: user.profilePictureUri }}
              style={{ width: 100, height: 100, borderRadius: 50 }}
            />
          )}
          <Text>Email: {user.givenName} {user.familyName}</Text>
        </View>
      )}
    </View>
  );
}
```

### Advanced Usage with Custom Scopes

```typescript
import ExpoGoogleSignin from '@madusha98/expo-google-signin';

const signInWithCustomScopes = async () => {
  try {
    const customScopes = [
      'https://www.googleapis.com/auth/userinfo.profile',
      'https://www.googleapis.com/auth/userinfo.email',
      'https://www.googleapis.com/auth/calendar.readonly',
      'https://www.googleapis.com/auth/contacts.readonly'
    ];
    
    const result = await ExpoGoogleSignin.signInAsync(
      process.env.EXPO_PUBLIC_WEB_CLIENT_ID!,
      customScopes
    );
    
    console.log('Sign-in with custom scopes successful:', result);
    
    // Now you can access user's calendar and contacts through your backend
    if (result.serverAuthCode) {
      await fetchUserCalendarEvents(result.serverAuthCode);
    }
  } catch (error) {
    console.error('Custom scopes sign-in error:', error);
  }
};
```

## 📚 API Reference

### `signInAsync(webClientId: string, scopes?: string[]): Promise<SignInResult>`

Initiates the Google Sign-In flow using the Credential Manager.

#### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `webClientId` | `string` | ✅ | Your OAuth 2.0 web client ID from Google Cloud Console |
| `scopes` | `string[]` | ❌ | Array of OAuth scopes to request. Defaults to `['profile', 'email']` |

#### Returns

A Promise that resolves to a `SignInResult` object:

```typescript
interface SignInResult {
  idToken?: string;           // JWT ID token from Google
  displayName?: string;       // User's full display name
  familyName?: string;        // User's last name
  givenName?: string;         // User's first name
  profilePictureUri?: string; // URL to user's profile picture
  phoneNumber?: string;       // User's phone number (if available)
  serverAuthCode?: string;    // Authorization code for server-side token exchange
}
```

#### Example Response

```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2NzAyN...",
  "displayName": "John Doe",
  "familyName": "Doe",
  "givenName": "John",
  "profilePictureUri": "https://lh3.googleusercontent.com/a/default-user",
  "phoneNumber": "+1234567890",
  "serverAuthCode": "4/0AX4XfWjE5R7..."
}
```

## 🔐 OAuth Scopes Reference

### Basic Scopes
| Scope | Description | Use Case |
|-------|-------------|----------|
| `profile` | Basic profile information | Get user's name and profile picture |
| `email` | Email address | User identification and communication |

### Google Services Scopes
| Scope | Description | Use Case |
|-------|-------------|----------|
| `https://www.googleapis.com/auth/calendar` | Full calendar access | Create, read, update calendar events |
| `https://www.googleapis.com/auth/calendar.readonly` | Read-only calendar access | View calendar events |
| `https://www.googleapis.com/auth/drive` | Full Google Drive access | File management |
| `https://www.googleapis.com/auth/drive.readonly` | Read-only Drive access | View files |
| `https://www.googleapis.com/auth/contacts.readonly` | Read contacts | Access contact list |
| `https://www.googleapis.com/auth/youtube` | YouTube account access | Manage YouTube content |
| `https://www.googleapis.com/auth/gmail.readonly` | Read Gmail | Access email content |

### Custom Scope Example

```typescript
const scopes = [
  'https://www.googleapis.com/auth/userinfo.profile',
  'https://www.googleapis.com/auth/userinfo.email',
  'https://www.googleapis.com/auth/calendar.readonly',
  'https://www.googleapis.com/auth/drive.metadata.readonly'
];

const result = await ExpoGoogleSignin.signInAsync(clientId, scopes);
```

## 🔄 Server-Side Token Exchange

The `serverAuthCode` returned by this module should be exchanged for access and refresh tokens on your backend server for security.

### Backend Implementation Example (Node.js)

```javascript
const { OAuth2Client } = require('google-auth-library');

const client = new OAuth2Client(
  process.env.GOOGLE_CLIENT_ID,
  process.env.GOOGLE_CLIENT_SECRET,
  'postmessage' // redirect URI for mobile apps
);

app.post('/auth/google', async (req, res) => {
  try {
    const { code } = req.body;
    
    // Exchange authorization code for tokens
    const { tokens } = await client.getToken(code);
    
    // Verify the ID token
    const ticket = await client.verifyIdToken({
      idToken: tokens.id_token,
      audience: process.env.GOOGLE_CLIENT_ID,
    });
    
    const payload = ticket.getPayload();
    const userId = payload['sub'];
    
    // Store tokens securely and create user session
    // ...
    
    res.json({ 
      success: true, 
      user: {
        id: userId,
        email: payload.email,
        name: payload.name,
        picture: payload.picture
      }
    });
  } catch (error) {
    console.error('Token exchange error:', error);
    res.status(400).json({ error: 'Invalid authorization code' });
  }
});
```

### Manual Token Exchange (Alternative)

```bash
curl -X POST https://oauth2.googleapis.com/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "code=YOUR_SERVER_AUTH_CODE" \
  -d "client_id=YOUR_CLIENT_ID" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "redirect_uri=postmessage" \
  -d "grant_type=authorization_code"
```

## 🛠️ Troubleshooting

### Common Issues

#### 1. "Sign-in fails with 'No current activity'"
**Cause**: The sign-in method is called when no Android activity is available.

**Solution**: Ensure you're calling the sign-in method from a component that's currently mounted and visible.

```typescript
// ❌ Don't call immediately on app start
useEffect(() => {
  signInWithGoogle(); // This might fail
}, []);

// ✅ Call in response to user interaction
const handlePress = () => {
  signInWithGoogle(); // This will work
};
```

#### 2. "No accounts available"
**Cause**: No Google accounts are configured on the device.

**Solution**: 
- Ensure the user has added a Google account in device settings
- Test on a device with Google Play Services installed
- Check if Google Play Services is up to date

#### 3. "Authorization failed" or "Invalid client ID"
**Cause**: Incorrect client ID or missing SHA-1 certificate.

**Solution**:
1. Verify your client ID is correct
2. Ensure you're using the **Web client ID**, not Android client ID
3. Add your app's SHA-1 fingerprint to Google Cloud Console
4. Make sure OAuth consent screen is properly configured

#### 4. "API not enabled"
**Cause**: Required Google APIs are not enabled.

**Solution**:
1. Go to Google Cloud Console
2. Navigate to **APIs & Services** → **Library**
3. Enable these APIs:
   - Google Identity Service
   - Any additional APIs for requested scopes

#### 5. Development vs Production Issues

**Debug Build Issues**:
```bash
# Get debug SHA-1
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Release Build Issues**:
```bash
# Get release SHA-1
keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
```

Make sure both debug and release SHA-1 fingerprints are added to your Google Cloud Console project.

### Debug Mode

Enable detailed logging to troubleshoot issues:

```typescript
try {
  const result = await ExpoGoogleSignin.signInAsync(clientId, scopes);
  console.log('Full sign-in result:', JSON.stringify(result, null, 2));
} catch (error) {
  console.error('Detailed error:', {
    message: error.message,
    code: error.code,
    stack: error.stack
  });
}
```

## 📱 Testing

### Testing Checklist

- [ ] Test on physical Android device (emulator may have limitations)
- [ ] Verify Google Play Services is installed and updated
- [ ] Test with different Google accounts
- [ ] Test with and without custom scopes
- [ ] Verify server auth code exchange works
- [ ] Test error handling scenarios

### Example Test Cases

```typescript
// Test basic sign-in
test('Basic Google Sign-In', async () => {
  const result = await ExpoGoogleSignin.signInAsync(TEST_CLIENT_ID);
  expect(result.displayName).toBeDefined();
  expect(result.serverAuthCode).toBeDefined();
});

// Test custom scopes
test('Sign-In with Custom Scopes', async () => {
  const scopes = ['profile', 'email', 'https://www.googleapis.com/auth/calendar.readonly'];
  const result = await ExpoGoogleSignin.signInAsync(TEST_CLIENT_ID, scopes);
  expect(result.serverAuthCode).toBeDefined();
});
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/madusha98/expo-google-signin.git
   cd expo-google-signin
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Run the example app:
   ```bash
   cd example
   npm install
   npx expo run:android
   ```

### Reporting Issues

When reporting issues, please include:
- Expo SDK version
- Android version and device model
- Complete error messages
- Steps to reproduce
- Your Google Cloud Console configuration (without sensitive data)

## 📋 Roadmap

- [ ] iOS support using ASAuthorizationController
- [ ] Sign-out functionality
- [ ] Silent sign-in for returning users
- [ ] Biometric authentication integration
- [ ] Web support for Expo Web
- [ ] Additional Google services integration

## 📄 License

MIT License - see the [LICENSE](LICENSE.txt) file for details.

## 👨‍💻 Author

**Madusha Lakruwan**
- Email: madushalakruwan2nd@gmail.com
- GitHub: [@madusha98](https://github.com/madusha98)

## 🙏 Acknowledgments

- Google Identity Team for the excellent documentation
- Expo team for the amazing development platform
- React Native community for continuous support

---

**⭐ If this package helped you, please consider giving it a star on [GitHub](https://github.com/madusha98/expo-google-signin)!**
