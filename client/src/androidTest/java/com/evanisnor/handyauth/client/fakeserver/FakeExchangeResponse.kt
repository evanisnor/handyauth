package com.evanisnor.handyauth.client.fakeserver

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FakeExchangeResponse(
    @get:Json(name = "access_token") val accessToken: String,
    @get:Json(name = "refresh_token") val refreshToken: String,
    @get:Json(name = "token_type") val tokenType: String,
    @get:Json(name = "expires_in") val expiresIn: Long,
    @get:Json(name = "scope") val scope: String
)
