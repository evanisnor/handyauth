package com.evanisnor.handyauth.client.internal.state

import com.evanisnor.handyauth.client.HandyAccessToken
import com.evanisnor.handyauth.client.internal.model.ExchangeResponse
import com.evanisnor.handyauth.client.internal.model.RefreshResponse
import com.evanisnor.handyauth.client.internal.time.InstantFactory

class AuthStateRepository(
    private val instantFactory: InstantFactory,
    private val cache: AuthStateCache
) {

    val isAuthorized: Boolean get() = cache.read().isAuthorized
    val refreshToken: String get() = cache.read().refreshToken ?: ""
    val accessToken: HandyAccessToken get() = cache.read().asAccessToken()

    fun isTokenExpired(): Boolean = cache.read().isExpired(instantFactory.now())

    fun save(exchangeResponse: ExchangeResponse) {
        val expiry = instantFactory.now().plusMillis(exchangeResponse.expiresIn)

        val updatedAuthState = cache.read().asMutableAuthState().apply {
            isAuthorized = true
            refreshToken = exchangeResponse.refreshToken
            tokenExpiry = expiry
            accessToken = exchangeResponse.accessToken
            accessTokenType = exchangeResponse.tokenType
        }

        cache.save(updatedAuthState.asAuthState())
    }

    fun save(refreshResponse: RefreshResponse) {
        val expiry = instantFactory.now().plusMillis(refreshResponse.expiresIn)

        val updatedAuthState = cache.read().asMutableAuthState().apply {
            tokenExpiry = expiry
            accessToken = refreshResponse.accessToken
            accessTokenType = refreshResponse.tokenType
        }

        cache.save(updatedAuthState.asAuthState())
    }

    fun clear() {
        cache.clear()
    }
}
