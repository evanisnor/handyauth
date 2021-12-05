package com.evanisnor.handyauth.client.internal

import android.content.Context
import androidx.activity.ComponentActivity
import com.evanisnor.handyauth.client.HandyAccessToken
import com.evanisnor.handyauth.client.HandyAuth
import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.internal.secure.AuthorizationValidator
import com.evanisnor.handyauth.client.internal.secure.DefaultAuthorizationValidator
import com.evanisnor.handyauth.client.internal.time.DefaultInstantFactory
import com.evanisnor.handyauth.client.internal.time.InstantFactory
import com.evanisnor.handyauth.client.ui.HandyAuthActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull

internal class InternalHandyAuth(
    private val config: HandyAuthConfig,
    private val internalNetworkClient: InternalNetworkClient = InternalNetworkClient(config),
    private val authStateRepository: AuthStateRepository = AuthStateRepository(),
    private val authorizationValidator: AuthorizationValidator = DefaultAuthorizationValidator(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    private val instantFactory: InstantFactory = DefaultInstantFactory()
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

            if (authorizationValidator.isValid(authorizationRequest, authResponse)) {
                authResponse?.let { successfulResponse ->
                    scope.launch {
                        internalNetworkClient.exchangeCodeForTokens(
                            authorizationResponse = successfulResponse,
                            codeVerifier = codeVerifier
                        )
                            ?.let { exchangeResponse ->
                                authStateRepository.save(callingActivity, exchangeResponse)
                            }
                    }
                }
            }

        }
    }

    override suspend fun accessToken(context: Context): HandyAccessToken {
        authStateRepository.restore(context)
        authStateRepository.tokenExpiry?.let { expiry ->
            if (instantFactory.now().isAfter(expiry) && authStateRepository.refreshToken != null) {
                internalNetworkClient.refresh(authStateRepository.refreshToken!!)
                    ?.let { refreshResponse ->
                        authStateRepository.save(context, refreshResponse)
                    }
            }
        }

        return authStateRepository.accessToken.firstOrNull() ?: HandyAccessToken()
    }

    override fun logout(context: Context) {
        scope.launch {
            authStateRepository.clear(context)
        }
    }
}

