package com.evanisnor.handyauth.client.internal.model

import android.net.Uri
import android.os.Parcelable
import com.evanisnor.handyauth.client.HandyAuthConfig
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthRequest(
    val config: HandyAuthConfig,
    val responseType: ResponseType,
    val scopes: List<String>,
    val state: String,
    val codeChallenge: String,
    val codeChallengeMethod: ChallengeMethod
) : Parcelable {

    enum class ResponseType(val queryParameterName: String) {
        Code("code")
    }

    enum class ChallengeMethod {
        S256
    }

    @IgnoredOnParcel
    val authorizationUrl: Uri =
        Uri.parse(
            "${config.authorizationUrl}?" +
                    "client_id=${config.clientId}&" +
                    "redirect_uri=${config.encodedRedirectUrl}&" +
                    "response_type=${responseType.queryParameterName}&" +
                    "scope=${scopes.joinToString("+")}&" +
                    "state=${state}&" +
                    "code_challenge=${codeChallenge}&" +
                    "code_challenge_method=${codeChallengeMethod.name}"
        )

}