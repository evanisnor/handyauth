package com.evanisnor.handyauth.client.fakeserver

import com.evanisnor.handyauth.client.HandyAuthConfig

class FakeAuthServerRobot {

  fun setupSuccessfulAuthorization(
    server: FakeAuthorizationServer,
    config: HandyAuthConfig,
  ) {
    // Authorization Requests are OK
    server.acceptAuthorizationRequest(config)

    // Exchange Requests are OK
    server.acceptExchangeRequest(
      response = createExchangeResponse(),
    )
  }

  fun setupFailedAuthorization(
    server: FakeAuthorizationServer,
    config: HandyAuthConfig,
    expectedError: String = "test-error",
    expectedErrorDescription: String = "Expected error description goes here",
    expectedErrorUri: String = "https://test.app/test-error",
  ) {
    // test server return an error
    server.errorAuthorizationRequest(
      config,
      expectedError,
      expectedErrorDescription,
      expectedErrorUri,
    )
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
      scope = "test_scope_a test_scope_b",
    )

  private fun createRefreshResponse(): FakeRefreshResponse =
    FakeRefreshResponse(
      accessToken = "refresh-response-access-token",
      refreshToken = null,
      tokenType = "Fake",
      expiresIn = 1000L,
      scope = "test_scope_a test_scope_b",
    )

  private fun createRefreshResponseWithNewRefreshToken(): FakeRefreshResponse =
    FakeRefreshResponse(
      accessToken = "updated-refresh-token-access",
      refreshToken = "new-refresh-token",
      tokenType = "Fake",
      expiresIn = 1000L,
      scope = "test_scope_a test_scope_b",
    )
}
