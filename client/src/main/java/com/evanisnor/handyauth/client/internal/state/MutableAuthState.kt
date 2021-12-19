package com.evanisnor.handyauth.client.internal.state

import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class MutableAuthState(
    var isAuthorized: Boolean = false,
    var refreshToken: String? = null,
    @EpochMilli var tokenExpiry: Instant? = null,
    var accessToken: String? = null,
    var accessTokenType: String? = null
) {

    fun isExpired(now: Instant) = tokenExpiry != null && now.isAfter(tokenExpiry)

    fun clear() {
        isAuthorized = false
        refreshToken = null
        tokenExpiry = null
        accessToken = null
        accessTokenType = null
    }
}