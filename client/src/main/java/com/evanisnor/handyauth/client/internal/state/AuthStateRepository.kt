package com.evanisnor.handyauth.client.internal.state

import com.evanisnor.handyauth.client.HandyAccessToken
import com.evanisnor.handyauth.client.internal.model.ExchangeResponse
import com.evanisnor.handyauth.client.internal.model.RefreshResponse
import com.evanisnor.handyauth.client.internal.state.model.MutableAuthState
import com.evanisnor.handyauth.client.internal.time.InstantFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class AuthStateRepository(
    private val instantFactory: InstantFactory,
    private val cache: AuthStateCache
) {

    private val mutableAuthState: AtomicReference<MutableAuthState> = AtomicReference(
        MutableAuthState()
    )
    private val mutableAccessToken: AtomicReference<HandyAccessToken> = AtomicReference(
        HandyAccessToken()
    )

    private var isRestored: AtomicBoolean = AtomicBoolean()

    val isAuthorized: Boolean get() = mutableAuthState.get().isAuthorized
    val refreshToken: String get() = mutableAuthState.get().refreshToken ?: ""
    val accessToken: HandyAccessToken get() = mutableAccessToken.get()

    fun isTokenExpired(): Boolean = mutableAuthState.get().isExpired(instantFactory.now())

    fun restore() {
        if (isRestored.get()) return

        mutableAccessToken.getAndUpdate {
            mutableAuthState.set(cache.read().asMutableAuthState())
            isRestored.getAndSet(true)

            HandyAccessToken(
                token = mutableAuthState.get().accessToken ?: "",
                tokenType = mutableAuthState.get().accessTokenType ?: ""
            )
        }
    }

    fun save(exchangeResponse: ExchangeResponse) {
        mutableAccessToken.getAndUpdate {
            val expiry = instantFactory.now().plusMillis(exchangeResponse.expiresIn)

            mutableAuthState.getAndUpdate { state ->
                state.apply {
                    isAuthorized = true
                    refreshToken = exchangeResponse.refreshToken
                    tokenExpiry = expiry
                    accessToken = exchangeResponse.accessToken
                    accessTokenType = exchangeResponse.tokenType
                }
            }

            cache.save(mutableAuthState.get().asAuthState())

            HandyAccessToken(
                token = exchangeResponse.accessToken,
                tokenType = exchangeResponse.tokenType
            )
        }
    }

    fun save(refreshResponse: RefreshResponse) {
        mutableAccessToken.getAndUpdate {
            val expiry = instantFactory.now().plusMillis(refreshResponse.expiresIn)

            mutableAuthState.getAndUpdate { state ->
                state.apply {
                    tokenExpiry = expiry
                    accessToken = refreshResponse.accessToken
                    accessTokenType = refreshResponse.tokenType
                }
            }

            cache.save(mutableAuthState.get().asAuthState())

            HandyAccessToken(
                token = refreshResponse.accessToken,
                tokenType = refreshResponse.tokenType
            )
        }
    }

    fun clear() {
        mutableAccessToken.getAndUpdate {
            mutableAuthState.getAndUpdate { state ->
                state.clear()
                cache.clear()
                state
            }

            HandyAccessToken()
        }
    }
}
