package expo.modules.googlesignin

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

class GoogleSignInManager(private val context: Context) {
    companion object {
        private const val TAG = "GoogleSignInManager"
    }

    private val credentialManager by lazy { CredentialManager.create(context) }

    private fun buildGoogleSignInRequest(clientId: String): GetCredentialRequest {
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(clientId)
            .setNonce(generateNonce())
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()
    }

    private fun generateNonce(): String {
        return java.util.UUID.randomUUID().toString()
    }

    suspend fun signIn(clientId: String): Result<GoogleSignInResult> {
        return try {
            val request = buildGoogleSignInRequest(clientId)
            val response = credentialManager.getCredential(context, request)
            handleSignInResult(response)
        } catch (e: GetCredentialException) {
            when (e) {
                is NoCredentialException -> {
                    Log.e(TAG, "No Google accounts available", e)
                    Result.failure(Exception("No Google accounts available on this device"))
                }
                is GetCredentialCancellationException -> {
                    Log.e(TAG, "Sign-in cancelled", e)
                    Result.failure(Exception("Sign-in was cancelled"))
                }
                else -> {
                    Log.e(TAG, "Sign-in failed", e)
                    Result.failure(e)
                }
            }
        }
    }

    fun handleAuthorization(
        clientId: String,
        scopes: List<String>?,
        forceCodeForRefreshToken: Boolean,
        onPendingIntent: (PendingIntent) -> Unit,
        onAlreadyAuthorized: (String?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val requestedScopes = (scopes ?: listOf(
            Scopes.PROFILE,
            Scopes.EMAIL
        )).map { Scope(it) }

        val authorizationRequest = AuthorizationRequest.Builder()
            .requestOfflineAccess(clientId, forceCodeForRefreshToken)
            .setRequestedScopes(requestedScopes.toMutableList())
            .build()

        Log.d("ServerClientId", authorizationRequest.serverClientId.toString())

        val activity = context as? Activity
            ?: return onError(IllegalStateException("Context is not an Activity"))

        Identity.getAuthorizationClient(activity)
            .authorize(authorizationRequest)
            .addOnSuccessListener { result ->
                val pendingIntent = result.pendingIntent
                if (result.hasResolution() && pendingIntent != null) {
                    onPendingIntent(pendingIntent)
                } else {
                    Log.d("Result", result.serverAuthCode.toString())
                    onAlreadyAuthorized(result.serverAuthCode)
                }
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun handleActivityResult(data: Intent?): GoogleSignInResult {
        try {
            val authClient = Identity.getAuthorizationClient(context)
            val authorizationResult = authClient.getAuthorizationResultFromIntent(data)

            Log.d("GoogleSignIn", "Authorization code: $authorizationResult.serverAuthCode")
            return GoogleSignInResult(
                idToken =authorizationResult.toGoogleSignInAccount()?.idToken,
                displayName =authorizationResult.toGoogleSignInAccount()?.displayName,
                familyName = authorizationResult.toGoogleSignInAccount()?.familyName,
                givenName = authorizationResult.toGoogleSignInAccount()?.givenName,
                profilePictureUri = authorizationResult.toGoogleSignInAccount()?.photoUrl.toString(),
                phoneNumber = null, // Adjust if available
                serverAuthCode = authorizationResult.serverAuthCode
            )
        } catch (e: ApiException) {
            throw Exception("Failed to get authorization result: ${e.statusCode} - ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error processing sign-in result: ${e.message}")
        }
    }

    private fun handleSignInResult(response: GetCredentialResponse): Result<GoogleSignInResult> {
        return when (val credential = response.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Result.success(
                            GoogleSignInResult(
                                idToken = googleIdTokenCredential.idToken,
                                displayName = googleIdTokenCredential.displayName,
                                familyName = googleIdTokenCredential.familyName,
                                givenName = googleIdTokenCredential.givenName,
                                profilePictureUri = googleIdTokenCredential.profilePictureUri?.toString(),
                                phoneNumber = googleIdTokenCredential.phoneNumber,
                                serverAuthCode = null
                            )
                        )
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Invalid Google ID token", e)
                        Result.failure(e)
                    }
                } else {
                    Log.e(TAG, "Unexpected credential type")
                    Result.failure(IllegalStateException("Unexpected credential type"))
                }
            }
            else -> {
                Log.e(TAG, "Unexpected credential type")
                Result.failure(IllegalStateException("Unexpected credential type"))
            }
        }
    }
}