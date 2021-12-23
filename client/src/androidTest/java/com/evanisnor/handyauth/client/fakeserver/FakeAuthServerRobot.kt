package com.evanisnor.handyauth.client.fakeserver

import com.evanisnor.handyauth.client.HandyAuthConfig

class FakeAuthServerRobot {

    fun createFakeConfig(server: FakeAuthorizationServer): HandyAuthConfig =
        HandyAuthConfig(
            clientId = "test-id",
            redirectUrl = "my.app://redirect",
            authorizationUrl = "${server.mockWebServerUrl}authorization",
            tokenUrl = "${server.mockWebServerUrl}token",
            scopes = listOf("test_scope_a", "test_scope_b")
        )

    fun setupSuccessfulAuthorization(
        server: FakeAuthorizationServer,
        config: HandyAuthConfig
    ) {
        // test server enqueue Location redirect with code & state
        server.acceptAuthorizationRequest(config)
        // test server enqueue exchange response
        server.acceptExchangeRequest(
            response = createExchangeResponse()
        )
    }

    fun setupFailedAuthorization(server: FakeAuthorizationServer) {
        // test server return 401
        server.denyAuthorizationRequest()
    }

    fun setupFailedAuthorization(
        server: FakeAuthorizationServer,
        config: HandyAuthConfig,
        error: String,
        errorDescription: String,
        errorUri: String
    ) {
        // test server return an error
        server.errorAuthorizationRequest(config, error, errorDescription, errorUri)
    }

    fun setupServerError(
        server: FakeAuthorizationServer
    ) {
        server.returnServerError()
    }

    fun setupFreshAccessToken(server: FakeAuthorizationServer) {
        // test server enqueue fresh access token
        server.acceptRefreshRequest(response = createRefreshResponse())
    }

    fun setupNewRefreshToken(server: FakeAuthorizationServer) {
        // test server enqueue new refresh token
        server.acceptRefreshRequest(response = createRefreshResponseWithNewRefreshToken())
    }

    private fun createExchangeResponse(): FakeExchangeResponse =
        FakeExchangeResponse(
            accessToken = "exchange-response-access-token",
            refreshToken = "test-refresh-token",
            tokenType = "Fake",
            expiresIn = 1000L,
            scope = "test_scope_a test_scope_b"
        )

    private fun createRefreshResponse(): FakeRefreshResponse =
        FakeRefreshResponse(
            accessToken = "refresh-response-access-token",
            refreshToken = null,
            tokenType = "Fake",
            expiresIn = 1000L,
            scope = "test_scope_a test_scope_b"
        )

    private fun createRefreshResponseWithNewRefreshToken(): FakeRefreshResponse =
        FakeRefreshResponse(
            accessToken = "updated-refresh-token-access",
            refreshToken = "new-refresh-token",
            tokenType = "Fake",
            expiresIn = 1000L,
            scope = "test_scope_a test_scope_b"
        )
}
