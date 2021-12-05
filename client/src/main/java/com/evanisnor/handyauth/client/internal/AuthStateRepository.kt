package com.evanisnor.handyauth.client.internal

import android.content.Context
import com.evanisnor.handyauth.client.HandyAccessToken
import com.evanisnor.handyauth.client.internal.model.ExchangeResponse
import com.evanisnor.handyauth.client.internal.model.RefreshResponse
import com.evanisnor.handyauth.client.internal.time.DefaultInstantFactory
import com.evanisnor.handyauth.client.internal.time.InstantFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

class AuthStateRepository(
    private val instantFactory: InstantFactory = DefaultInstantFactory()
) {

    companion object {
        const val STATE_PREFS_NAME = "com.evanisnor.handyauth.client.internal.authState"
        private const val STATE_IS_AUTHORIZED = "isAuthorized"
        private const val STATE_REFRESH_TOKEN = "refreshToken"
        private const val STATE_ACCESS_TOKEN = "accessToken"
        private const val STATE_TOKEN_TYPE = "tokenType"
        private const val STATE_TOKEN_EXPIRY = "tokenExpiry"
    }


    private var mutableIsAuthorized: Boolean = false
    val isAuthorized: Boolean get() = mutableIsAuthorized

    var refreshToken: String? = null

    private val mutableAccessToken: MutableStateFlow<HandyAccessToken?> = MutableStateFlow(null)
    val accessToken: Flow<HandyAccessToken?> = mutableAccessToken

    var tokenExpiry: Instant? = null

    var isRestored: Boolean = false

    suspend fun restore(context: Context) {
        if (isRestored) return

        sharedPrefs(context).apply {
            mutableIsAuthorized = getBoolean(STATE_IS_AUTHORIZED, false)
            refreshToken = getString(STATE_REFRESH_TOKEN, "")
            tokenExpiry = Instant.ofEpochMilli(getLong(STATE_TOKEN_EXPIRY, Long.MAX_VALUE))

            mutableAccessToken.emit(
                HandyAccessToken(
                    token = getString(STATE_ACCESS_TOKEN, "") ?: "",
                    tokenType = getString(STATE_TOKEN_TYPE, "") ?: ""
                )
            )
        }
        isRestored = true
    }

    suspend fun save(context: Context, exchangeResponse: ExchangeResponse) {
        val expiry = instantFactory.now().plusMillis(exchangeResponse.expiresIn)
        mutableIsAuthorized = true
        refreshToken = exchangeResponse.refreshToken
        tokenExpiry = expiry

        sharedPrefs(context).edit()
            .apply {
                putBoolean(STATE_IS_AUTHORIZED, true)
                putString(STATE_REFRESH_TOKEN, exchangeResponse.refreshToken)
                putString(STATE_ACCESS_TOKEN, exchangeResponse.accessToken)
                putString(STATE_TOKEN_TYPE, exchangeResponse.tokenType)
                putLong(STATE_TOKEN_EXPIRY, expiry.toEpochMilli())
                apply()
            }

        mutableAccessToken.emit(
            HandyAccessToken(
                token = exchangeResponse.accessToken,
                tokenType = exchangeResponse.tokenType,
            )
        )
    }

    suspend fun save(context: Context, refreshResponse: RefreshResponse) {
        val expiry = instantFactory.now().plusMillis(refreshResponse.expiresIn)
        tokenExpiry = expiry

        sharedPrefs(context).edit()
            .apply {
                putString(STATE_ACCESS_TOKEN, refreshResponse.accessToken)
                putString(STATE_TOKEN_TYPE, refreshResponse.tokenType)
                putLong(STATE_TOKEN_EXPIRY, expiry.toEpochMilli())
                apply()
            }

        mutableAccessToken.emit(
            HandyAccessToken(
                token = refreshResponse.accessToken,
                tokenType = refreshResponse.tokenType
            )
        )
    }

    suspend fun clear(context: Context) {
        mutableIsAuthorized = false
        refreshToken = null
        tokenExpiry = null
        mutableAccessToken.emit(null)
        sharedPrefs(context).edit().apply {
            clear()
            apply()
        }
    }

    private fun sharedPrefs(context: Context) =
        context.getSharedPreferences(STATE_PREFS_NAME, Context.MODE_PRIVATE)
}
