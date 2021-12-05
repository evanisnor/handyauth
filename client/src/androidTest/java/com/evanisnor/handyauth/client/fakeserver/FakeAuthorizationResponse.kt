package com.evanisnor.handyauth.client.fakeserver

data class FakeAuthorizationResponse(
    val code: String = "test-code",
    val state: String = "test-state"
)