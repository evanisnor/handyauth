package com.evanisnor.handyauth.client.internal

import android.content.Context
import androidx.activity.ComponentActivity
import com.evanisnor.handyauth.client.HandyAccessToken
import com.evanisnor.handyauth.client.HandyAuth
import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.internal.model.AuthRequest
import com.evanisnor.handyauth.client.internal.model.AuthResponse
import com.evanisnor.handyauth.client.internal.network.InternalNetworkClient
import com.evanisnor.handyauth.client.internal.secure.AuthorizationValidator
import com.evanisnor.handyauth.client.internal.secure.DefaultAuthorizationValidator
import com.evanisnor.handyauth.client.internal.state.AuthStateRepository
import com.evanisnor.handyauth.client.ui.HandyAuthActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal class InternalHandyAuth(
    private val config: HandyAuthConfig,
    private val internalNetworkClient: InternalNetworkClient = InternalNetworkClient(config),
    private val authStateRepository: AuthStateRepository = AuthStateRepository(),
    private val authorizationValidator: AuthorizationValidator = DefaultAuthorizationValidator(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : HandyAuth {

    override val isAuthorized: Boolean
        get() = authStateRepository.isAuthorized

    override fun authorize(callingActivity: ComponentActivity) {
        val codeVerifier = internalNetworkClient.createCodeVerifier()
        val authorizationRequest = internalNetworkClient.buildAuthorizationRequest(codeVerifier)

        HandyAuthActivity.start(
            callingActivity = callingActivity,
            authorizationRequest = authorizationRequest
        ) { authResponse ->

            authResponse?.also {
                handleAuthorization(
                    callingActivity,
                    authorizationRequest,
                    authResponse,
                    codeVerifier
                )
            }

        }
    }

    override suspend fun accessToken(context: Context): HandyAccessToken {
        authStateRepository.restore(context)

        if (authStateRepository.isTokenExpired()) {
            internalNetworkClient.refresh(authStateRepository.refreshToken)
                ?.also { refreshResponse ->
                    authStateRepository.save(context, refreshResponse)
                }
        }

        return authStateRepository.accessToken
    }

    override fun logout(context: Context) {
        authStateRepository.clear(context)
    }

    private fun handleAuthorization(
        callingActivity: ComponentActivity,
        authorizationRequest: AuthRequest,
        authResponse: AuthResponse,
        codeVerifier: String
    ) {
        if (authorizationValidator.isValid(authorizationRequest, authResponse)) {
            scope.launch {
                internalNetworkClient.exchangeCodeForTokens(
                    authorizationResponse = authResponse,
                    codeVerifier = codeVerifier
                )?.also { exchangeResponse ->
                    authStateRepository.save(callingActivity, exchangeResponse)
                }
            }
        }
    }
}

