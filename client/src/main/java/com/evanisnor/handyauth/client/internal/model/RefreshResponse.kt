package com.evanisnor.handyauth.client.internal.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RefreshResponse(
  @Json(name = "access_token") val accessToken: String,
  @Json(name = "refresh_token") val refreshToken: String?,
  @Json(name = "token_type") val tokenType: String,
  @Json(name = "expires_in") val expiresIn: Long,
  @Json(name = "scope") val scope: String,
)
