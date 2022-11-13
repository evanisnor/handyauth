package com.evanisnor.handyauth.client.internal.state.model

import java.time.Instant

data class MutableAuthState(
  var isAuthorized: Boolean = false,
  var refreshToken: String? = null,
  var tokenExpiry: Instant? = null,
  var accessToken: String? = null,
  var accessTokenType: String? = null,
) {

  fun asAuthState() = AuthState(
    isAuthorized,
    refreshToken,
    tokenExpiry,
    accessToken,
    accessTokenType,
  )
}
