package expo.modules.googlesignin

data class GoogleSignInResult(
    val idToken: String?,
    val displayName: String?,
    val familyName: String?,
    val givenName: String?,
    val profilePictureUri: String?,
    val phoneNumber: String?,
    val serverAuthCode: String?
)