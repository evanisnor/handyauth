package com.evanisnor.handyauth.client.internal.network

import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.internal.model.*
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
    private val refreshResponseJsonAdapter: RefreshResponseJsonAdapter
) {

    fun createCodeVerifier(): String {
        return codeGenerator.generate(128)
    }

    fun buildAuthorizationRequest(codeVerifier: String): AuthRequest = AuthRequest(
        config = config,
        responseType = AuthRequest.ResponseType.Code,
        scopes = config.scopes,
        state = codeGenerator.generate(16),
        codeChallenge = codeGenerator.codeChallenge(codeVerifier),
        codeChallengeMethod = AuthRequest.ChallengeMethod.S256
    )

    suspend fun exchangeCodeForTokens(
        authorizationCode: String,
        codeVerifier: String
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
                    .build()
            )
            .build()

        return client.newCall(exchangeRequest).send()
            .parseBody(adapter = exchangeResponseJsonAdapter)
    }

    suspend fun refresh(refreshToken: String): RefreshResponse {
        val refreshRequest = Request.Builder()
            .url(config.tokenUrl.toString())
            .post(
                FormBody.Builder()
                    .add("grant_type", "refresh_token")
                    .add("client_id", config.clientId)
                    .add("refresh_token", refreshToken)
                    .build()
            )
            .build()

        return client.newCall(refreshRequest).send().parseBody(adapter = refreshResponseJsonAdapter)
    }

    /**
     * Parse the response body with the specified JsonAdapter
     */
    private fun <T> CallResult.parseBody(adapter: JsonAdapter<T>) = when (this) {
        is CallResult.Response -> {
            runCatching {
                response.body!!.let { body ->
                    adapter.fromJson(body.source())!!
                }
            }.getOrThrow()
        }
        is CallResult.Error -> {
            throw Exception(error)
        }
    }

}
