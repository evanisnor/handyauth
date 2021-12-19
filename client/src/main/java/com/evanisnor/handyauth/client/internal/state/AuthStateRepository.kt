package com.evanisnor.handyauth.client.internal.state

import android.content.Context
import com.evanisnor.handyauth.client.HandyAccessToken
import com.evanisnor.handyauth.client.internal.model.ExchangeResponse
import com.evanisnor.handyauth.client.internal.model.RefreshResponse
import com.evanisnor.handyauth.client.internal.time.DefaultInstantFactory
import com.evanisnor.handyauth.client.internal.time.InstantFactory
import com.squareup.moshi.Moshi
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class AuthStateRepository(
    private val instantFactory: InstantFactory = DefaultInstantFactory(),
    moshi: Moshi = Moshi.Builder()
        .add(InstantJsonAdapter())
        .build(),
    private val authStateJsonAdapter: MutableAuthStateJsonAdapter = MutableAuthStateJsonAdapter(
        moshi
    )
) {

    companion object {
        const val STATE_PREFS_NAME = "com.evanisnor.handyauth.client.internal"
        private const val STATE_KEY = "authState"
    }

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

    fun restore(context: Context) {
        if (isRestored.get()) return

        mutableAccessToken.getAndUpdate {
            sharedPrefs(context).apply {
                val state = authStateJsonAdapter.fromJson(getString(STATE_KEY, null) ?: "{}")
                mutableAuthState.set(state)
            }
            isRestored.getAndSet(true)

            HandyAccessToken(
                token = mutableAuthState.get().accessToken ?: "",
                tokenType = mutableAuthState.get().accessTokenType ?: ""
            )
        }
    }

    fun save(context: Context, exchangeResponse: ExchangeResponse) {
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

            sharedPrefs(context).edit()
                .apply {
                    putString(STATE_KEY, authStateJsonAdapter.toJson(mutableAuthState.get()))
                    apply()
                }

            HandyAccessToken(
                token = exchangeResponse.accessToken,
                tokenType = exchangeResponse.tokenType
            )
        }
    }

    fun save(context: Context, refreshResponse: RefreshResponse) {
        mutableAccessToken.getAndUpdate {
            val expiry = instantFactory.now().plusMillis(refreshResponse.expiresIn)

            mutableAuthState.getAndUpdate { state ->
                state.apply {
                    tokenExpiry = expiry
                    accessToken = refreshResponse.accessToken
                    accessTokenType = refreshResponse.tokenType
                }
            }

            sharedPrefs(context).edit()
                .apply {
                    putString(STATE_KEY, authStateJsonAdapter.toJson(mutableAuthState.get()))
                    apply()
                }

            HandyAccessToken(
                token = refreshResponse.accessToken,
                tokenType = refreshResponse.tokenType
            )
        }
    }

    fun clear(context: Context) {
        mutableAccessToken.getAndUpdate {
            mutableAuthState.getAndUpdate { state ->
                state.clear()
                sharedPrefs(context).edit().apply {
                    clear()
                    apply()
                }
                state
            }

            HandyAccessToken()
        }
    }

    @Synchronized
    private fun sharedPrefs(context: Context) =
        context.getSharedPreferences(STATE_PREFS_NAME, Context.MODE_PRIVATE)
}
