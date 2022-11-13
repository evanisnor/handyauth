package com.evanisnor.handyauth.client

/**
 * Represents an OAuth2 access token.
 */
data class HandyAccessToken(
  val token: String = "",
  val tokenType: String = "",
) {

  /**
   * Convert this [HandyAccessToken] to a String for use in Authorization HTTP headers.
   * example: "Bearer abcABC123"
   */
  fun asHeaderValue(): String = "$tokenType $token"
}
