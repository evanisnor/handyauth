package com.evanisnor.handyauth.client.internal.network

import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.internal.model.AuthRequest
import com.evanisnor.handyauth.client.internal.model.ExchangeResponse
import com.evanisnor.handyauth.client.internal.model.ExchangeResponseJsonAdapter
import com.evanisnor.handyauth.client.internal.model.RefreshResponse
import com.evanisnor.handyauth.client.internal.model.RefreshResponseJsonAdapter
import com.evanisnor.handyauth.client.internal.secure.CodeGenerator
import com.squareup.moshi.JsonAdapter
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class TokenNetworkClient(
  private val config: HandyAuthConfig,
  private val codeGenerator: CodeGenerator,
  private val client: OkHttpClient,
  private val exchangeResponseJsonAdapter: ExchangeResponseJsonAdapter,
  private val refreshResponseJsonAdapter: RefreshResponseJsonAdapter,
) {

  class TokenRefreshFailure(message: String, cause: Throwable?) : Error(message, cause)

  fun createCodeVerifier(): String {
    return codeGenerator.generate(128)
  }

  fun buildAuthorizationRequest(codeVerifier: String): AuthRequest = AuthRequest(
    config = config,
    responseType = AuthRequest.ResponseType.Code,
    scopes = config.scopes,
    state = codeGenerator.generate(16),
    codeChallenge = codeGenerator.codeChallenge(codeVerifier),
    codeChallengeMethod = AuthRequest.ChallengeMethod.S256,
  )

  suspend fun exchangeCodeForTokens(
    authorizationCode: String,
    codeVerifier: String,
  ): ExchangeResponse {
    val exchangeRequest = Request.Builder()
      .url(config.tokenUrl.toString())
      .post(
        FormBody.Builder()
          .add("grant_type", "authorization_code")
          .add("client_id", config.clientId)
          .add("code_verifier", codeVerifier)
          .add("code", authorizationCode)
          .add("redirect_uri", config.redirectUrl)
          .build(),
      )
      .build()

    return when (val response = client.newCall(exchangeRequest).send()) {
      is CallResult.Response -> response.parseBody(adapter = exchangeResponseJsonAdapter)!!
      is CallResult.Error -> throw response.error
    }
  }

  suspend fun refresh(refreshToken: String): RefreshResponse {
    val refreshRequest = Request.Builder()
      .url(config.tokenUrl.toString())
      .post(
        FormBody.Builder()
          .add("grant_type", "refresh_token")
          .add("client_id", config.clientId)
          .add("refresh_token", refreshToken)
          .build(),
      )
      .build()

    return when (val response = client.newCall(refreshRequest).send()) {
      is CallResult.Response -> response.parseBody(adapter = refreshResponseJsonAdapter)!!
      is CallResult.Error -> throw TokenRefreshFailure(
        response.body ?: "Token refresh failed without a response from the server",
        response.error,
      )
    }
  }

  /**
   * Parse the response body with the specified JsonAdapter
   */
  private fun <T> CallResult.Response.parseBody(adapter: JsonAdapter<T>) =
    response.body?.let { body ->
      adapter.fromJson(body.source())
    }
}
