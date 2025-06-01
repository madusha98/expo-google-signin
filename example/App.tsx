import { useEvent } from "expo";
import ExpoGoogleSignin, { SignInResult } from "expo-google-signin";
import { Button, SafeAreaView, ScrollView, Text, Image, View } from "react-native";
import { useState } from "react";

export default function App() {
  const [result, setResult] = useState<SignInResult | undefined>(undefined);

  const handleSignIn = async () => {
    try {
      const signInResult = await ExpoGoogleSignin.signInAsync(process.env.EXPO_PUBLIC_WEB_CLIENT_ID, ['profile', 'email', 'https://www.googleapis.com/auth/calendar', 'https://www.googleapis.com/auth/contacts.readonly']);
      setResult(signInResult);
      console.log(signInResult, "signInResult");
    } catch (error) {
      console.error('Sign-in error:', error);
    }
  };

  return (
    <SafeAreaView style={{flex: 1}}>  
      <View style={{flex: 1, justifyContent: "center", alignItems: "center", backgroundColor: "#eee"}}>
        <Button
          title="Sign In"
          onPress={handleSignIn}
        />
        {result && (
          <View style={{marginTop: 20}}>
            <Text>Display Name: {result.displayName}</Text>
            <Text>Family Name: {result.familyName}</Text>
            <Text>Given Name: {result.givenName}</Text>
            <Image source={{uri: result.profilePictureUri}} style={{width: 100, height: 100}} />
            <Text>Phone Number: {result.phoneNumber}</Text>
            <Text>Server Auth Code: {result.serverAuthCode}</Text>
            <Text>ID Token: {result.idToken}</Text>
          </View>
        )}
      </View>
    </SafeAreaView>
  );
}


