package com.evanisnor.handyauth.client.fakeserver

import androidx.activity.ComponentActivity
import com.evanisnor.handyauth.client.HandyAuth
import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.internal.InternalHandyAuth
import kotlinx.coroutines.runBlocking

class FakeAuthServerRobot(
    private val server: FakeAuthorizationServer
) {

    fun createFakeConfig(): HandyAuthConfig = HandyAuthConfig(
        clientId = "test-id",
        redirectUrl = "${server.mockWebServerUrl}/redirect",
        authorizationUrl = "${server.mockWebServerUrl}/authorization",
        tokenUrl = "${server.mockWebServerUrl}/token",
        scopes = listOf("test_scope_a", "test_scope_b")
    )

    suspend fun createAuthorizedClient(componentActivity: ComponentActivity): HandyAuth {
        val config = createFakeConfig()
        server.acceptAuthorizationRequest(config)
        return InternalHandyAuth(config).apply {
            authorize(componentActivity)
        }
    }

    fun createExchangeResponse(): FakeExchangeResponse =
        FakeExchangeResponse(
            accessToken = "exchange-response-access-token",
            refreshToken = "test-refresh-token",
            tokenType = "Fake",
            expiresIn = 0L,
            scope = "test_scope_a test_scope_b"
        )

    fun createRefreshResponse(): FakeRefreshResponse =
        FakeRefreshResponse(
            accessToken = "refresh-response-access-token",
            tokenType = "Fake",
            expiresIn = 0L,
            scope = "test_scope_a test_scope_b"
        )

    fun createRefreshResponse(accessToken: String, expiresIn: Long = 1000): FakeRefreshResponse =
        FakeRefreshResponse(
            accessToken = accessToken,
            tokenType = "Fake",
            expiresIn = expiresIn,
            scope = "test_scope_a test_scope_b"
        )

}
