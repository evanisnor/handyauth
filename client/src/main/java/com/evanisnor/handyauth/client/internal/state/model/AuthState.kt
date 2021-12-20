package com.evanisnor.handyauth.client.internal.state.model

import com.evanisnor.handyauth.client.HandyAccessToken
import com.evanisnor.handyauth.client.internal.state.EpochMilli
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class AuthState(
    val isAuthorized: Boolean = false,
    val refreshToken: String? = null,
    @EpochMilli val tokenExpiry: Instant? = null,
    val accessToken: String? = null,
    val accessTokenType: String? = null
) {

    fun isExpired(now: Instant) = tokenExpiry != null && now.isAfter(tokenExpiry)

    fun asMutableAuthState(): MutableAuthState = MutableAuthState(
        isAuthorized, refreshToken, tokenExpiry, accessToken, accessTokenType
    )

    fun asAccessToken(): HandyAccessToken = HandyAccessToken(
        token = accessToken ?: "",
        tokenType = accessTokenType ?: ""
    )

}