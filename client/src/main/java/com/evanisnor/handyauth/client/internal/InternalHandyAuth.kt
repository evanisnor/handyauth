package com.evanisnor.handyauth.client.internal

import androidx.activity.ComponentActivity
import com.evanisnor.handyauth.client.HandyAccessToken
import com.evanisnor.handyauth.client.HandyAuth
import com.evanisnor.handyauth.client.internal.model.AuthRequest
import com.evanisnor.handyauth.client.internal.model.AuthResponse
import com.evanisnor.handyauth.client.internal.network.InternalNetworkClient
import com.evanisnor.handyauth.client.internal.secure.AuthorizationValidator
import com.evanisnor.handyauth.client.internal.state.AuthStateRepository
import com.evanisnor.handyauth.client.ui.HandyAuthActivity
import kotlinx.coroutines.*

internal class InternalHandyAuth @ObsoleteCoroutinesApi constructor(
    private val internalNetworkClient: InternalNetworkClient,
    private val authStateRepository: AuthStateRepository,
    private val authorizationValidator: AuthorizationValidator,
    private val scope: CoroutineScope = CoroutineScope(newSingleThreadContext("HandyAuth") + SupervisorJob())
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
            scope.launch {
                authResponse?.let {
                    handleAuthorization(
                        authorizationRequest,
                        authResponse,
                        codeVerifier
                    )
                }
            }
        }
    }

    override suspend fun accessToken(): HandyAccessToken = withContext(scope.coroutineContext) {
        if (authStateRepository.isTokenExpired()) {
            internalNetworkClient.refresh(authStateRepository.refreshToken)
                ?.also { refreshResponse ->
                    authStateRepository.save(refreshResponse)
                }
        }

        authStateRepository.accessToken
    }

    override suspend fun logout(): Unit = withContext(scope.coroutineContext) {
        authStateRepository.clear()
    }

    private suspend fun handleAuthorization(
        authorizationRequest: AuthRequest,
        authResponse: AuthResponse,
        codeVerifier: String
    ) {
        if (authorizationValidator.isValid(authorizationRequest, authResponse)) {
            internalNetworkClient.exchangeCodeForTokens(
                authorizationResponse = authResponse,
                codeVerifier = codeVerifier
            )?.also { exchangeResponse ->
                authStateRepository.save(exchangeResponse)
            }
        }
    }
}

