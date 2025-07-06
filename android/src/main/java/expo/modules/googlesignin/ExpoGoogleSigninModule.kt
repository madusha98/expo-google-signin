package expo.modules.googlesignin

import android.app.Activity
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import expo.modules.kotlin.Promise
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.activity.result.IntentSenderRequest

class ExpoGoogleSigninModule : Module() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var signInPromise: Promise? = null
    private var authLauncher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>? = null

    override fun definition() = ModuleDefinition {
        Name("ExpoGoogleSignin")

        OnCreate {
            val activity = appContext.currentActivity
            if (activity != null) {
                authLauncher = androidx.activity.result.ActivityResultRegistryOwner::class.java
                    .cast(activity)
                    ?.activityResultRegistry
                    ?.register(
                        "googleSignIn",
                        ActivityResultContracts.StartIntentSenderForResult()
                    ) { result ->
                        coroutineScope.launch {
                            if (result.resultCode == Activity.RESULT_OK) {
                                val data = result.data
                                if (data != null) {
                                    try {
                                        val googleSignInManager = GoogleSignInManager(activity)
                                        val signInResult = googleSignInManager.handleActivityResult(data)
                                        Log.d("GoogleSignIn", "Sign-in result: ${signInResult.displayName}")
                                        signInPromise?.resolve(mapOf(
                                            "status" to "success",
                                            "idToken" to signInResult.idToken,
                                            "displayName" to signInResult.displayName,
                                            "familyName" to signInResult.familyName,
                                            "givenName" to signInResult.givenName,
                                            "profilePictureUri" to signInResult.profilePictureUri,
                                            "phoneNumber" to signInResult.phoneNumber,
                                            "serverAuthCode" to signInResult.serverAuthCode
                                        ))
                                    } catch (e: Exception) {
                                        Log.e("GoogleSignIn", "Error processing result: ${e.message}")
                                        signInPromise?.reject("ERR_RESULT", "Failed to process sign-in result: ${e.message}", e)
                                    }
                                } else {
                                    Log.e("GoogleSignIn", "Intent data is null")
                                    signInPromise?.reject("ERR_NO_DATA", "No data returned from sign-in", null)
                                }
                            } else {
                                Log.e("GoogleSignIn", "Sign-in canceled or failed, resultCode: ${result.resultCode}")
                                signInPromise?.reject("ERR_CANCELED", "Sign-in canceled or failed", null)
                            }
                            signInPromise = null // Clear the promise after resolving/rejecting
                        }
                    }
            } else {
                Log.w("GoogleSignIn", "No activity available to register auth launcher")
            }
        }

        AsyncFunction("signInAsync") { webClientId: String, scopes: List<String>?, forceCodeForRefreshToken: Boolean?, promise: Promise ->
            val activity = appContext.currentActivity
            if (activity == null) {
                promise.reject("ERR_NO_ACTIVITY", "No current activity", null)
                return@AsyncFunction
            }

            if (authLauncher == null) {
                promise.reject("ERR_NO_LAUNCHER", "Activity result launcher not initialized", null)
                return@AsyncFunction
            }

            val googleSignInManager = GoogleSignInManager(activity)
            signInPromise = promise

            coroutineScope.launch {
                googleSignInManager.signIn(webClientId).onSuccess { result: GoogleSignInResult ->
                    Log.d("GoogleSignIn", "Sign-in successful: ${result.displayName}")

                    googleSignInManager.handleAuthorization(
                        webClientId,
                        scopes,
                        forceCodeForRefreshToken ?: false,
                        onPendingIntent = { pendingIntent ->
                            try {
                                // Wrap the IntentSender in an IntentSenderRequest
                                val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                                authLauncher?.launch(intentSenderRequest)
                                // Do not resolve the promise here; wait for activity result
                            } catch (e: Exception) {
                                Log.e("GoogleSignIn", "Error launching pending intent: ${e.message}")
                                signInPromise?.reject("ERR_INTENT", "Failed to launch authorization: ${e.message}", e)
                                signInPromise = null
                            }
                        },
                        onAlreadyAuthorized = { serverAuthCode ->
                            Log.d("GoogleSignIn", "Already authorized, serverAuthCode: $serverAuthCode")
                            signInPromise?.resolve(mapOf(
                                "status" to "success",
                                "idToken" to result.idToken,
                                "displayName" to result.displayName,
                                "familyName" to result.familyName,
                                "givenName" to result.givenName,
                                "profilePictureUri" to result.profilePictureUri,
                                "phoneNumber" to result.phoneNumber,
                                "serverAuthCode" to serverAuthCode
                            ))
                            signInPromise = null
                        },
                        onError = { e ->
                            Log.e("GoogleSignIn", "Authorization error", e)
                            signInPromise?.reject("ERR_AUTH", "Authorization failed: ${e.message}", e)
                            signInPromise = null
                        }
                    )
                }.onFailure { e ->
                    Log.e("GoogleSignIn", "Sign-in failed", e)
                    signInPromise?.reject("ERR_SIGNIN", "Sign-in failed: ${e.message}", e)
                    signInPromise = null
                }
            }
        }

        OnDestroy {
            signInPromise = null
            authLauncher?.unregister()
            authLauncher = null
        }
    }
}