package com.evanisnor.handyauth.client.internal.network

import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.internal.model.*
import com.evanisnor.handyauth.client.internal.secure.CodeGenerator
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class InternalNetworkClient(
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
    ): ExchangeResponse? {
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

        return send(exchangeRequest)?.body?.let { body ->
            runCatching {
                exchangeResponseJsonAdapter.fromJson(body.source())
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

        return send(refreshRequest)?.body?.let { body ->
            runCatching {
                refreshResponseJsonAdapter.fromJson(body.source())
            }.getOrNull()
        }
    }

    private suspend fun send(request: Request): Response? = client.newCall(request).send().response

}
