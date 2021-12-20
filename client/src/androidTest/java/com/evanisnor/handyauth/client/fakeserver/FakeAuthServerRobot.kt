package com.evanisnor.handyauth.client.fakeserver

import com.evanisnor.handyauth.client.HandyAuthConfig

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
