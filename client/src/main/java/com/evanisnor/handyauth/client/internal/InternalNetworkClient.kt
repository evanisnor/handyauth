package com.evanisnor.handyauth.client.internal

import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.internal.secure.CodeGenerator
import com.evanisnor.handyauth.client.internal.model.*
import com.squareup.moshi.Moshi
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

class InternalNetworkClient(
    private val config: HandyAuthConfig,
    private val codeGenerator: CodeGenerator = CodeGenerator(),
    private val moshi: Moshi = Moshi.Builder().build(),
    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build(),
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
        authorizationResponse: AuthResponse,
        codeVerifier: String
    ): ExchangeResponse? {
        val exchangeRequest = Request.Builder()
            .url(config.tokenUrl.toString())
            .post(
                FormBody.Builder()
                    .add("grant_type", "authorization_code")
                    .add("client_id", config.clientId)
                    .add("code_verifier", codeVerifier)
                    .add("code", authorizationResponse.authorizationCode)
                    .add("redirect_uri", config.redirectUrl)
                    .build()
            )
            .build()

        return client.newCall(exchangeRequest).send().response?.body?.let { body ->
            runCatching {
                ExchangeResponseJsonAdapter(moshi).fromJson(body.source())
            }.getOrNull()
        }
    }

    suspend fun refresh(refreshToken: String): RefreshResponse? {
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

        return client.newCall(refreshRequest).send().response?.body?.let { body ->
            runCatching {
                RefreshResponseJsonAdapter(moshi).fromJson(body.source())
            }.getOrNull()
        }
    }

}
