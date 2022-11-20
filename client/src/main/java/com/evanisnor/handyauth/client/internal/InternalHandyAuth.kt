package com.evanisnor.handyauth.client.internal

import androidx.activity.ComponentActivity
import com.evanisnor.handyauth.client.HandyAccessToken
import com.evanisnor.handyauth.client.HandyAuth
import com.evanisnor.handyauth.client.internal.model.AuthRequest
import com.evanisnor.handyauth.client.internal.model.AuthResponse
import com.evanisnor.handyauth.client.internal.network.TokenNetworkClient
import com.evanisnor.handyauth.client.internal.secure.AuthorizationValidator
import com.evanisnor.handyauth.client.internal.state.AuthStateRepository
import com.evanisnor.handyauth.client.ui.HandyAuthActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class InternalHandyAuth @DelicateCoroutinesApi constructor(
  private val tokenNetworkClient: TokenNetworkClient,
  private val authStateRepository: AuthStateRepository,
  private val authorizationValidator: AuthorizationValidator,
  private val scope: CoroutineScope = CoroutineScope(newSingleThreadContext("HandyAuth") + SupervisorJob()),
) : HandyAuth {

  override val isAuthorized: Boolean
    get() = authStateRepository.isAuthorized

  override suspend fun authorize(callingActivity: ComponentActivity): HandyAuth.Result =
    suspendCoroutine { continuation ->
      val codeVerifier = tokenNetworkClient.createCodeVerifier()
      val authorizationRequest = tokenNetworkClient.buildAuthorizationRequest(codeVerifier)

      var result: HandyAuth.Result?

      HandyAuthActivity.start(
        callingActivity = callingActivity,
        authorizationRequest = authorizationRequest,
      ) { authResponse ->
        scope.launch {
          authResponse?.let {
            result = handleAuthorizationResponse(
              authorizationRequest,
              authResponse,
              codeVerifier,
            )
            continuation.resume(result ?: HandyAuth.Result.Error.UnknownError)
          }
        }
      }
    }

  override suspend fun accessToken(): HandyAccessToken = withContext(scope.coroutineContext) {
    if (authStateRepository.isTokenExpired()) {
      tokenNetworkClient.refresh(authStateRepository.refreshToken)
        .also { refreshResponse ->
          authStateRepository.save(refreshResponse)
        }
    }

    authStateRepository.accessToken
  }

  override suspend fun logout(): Unit = withContext(scope.coroutineContext) {
    authStateRepository.clear()
  }

  private suspend fun handleAuthorizationResponse(
    authorizationRequest: AuthRequest,
    authResponse: AuthResponse,
    codeVerifier: String,
  ): HandyAuth.Result {
    return if (authResponse.error != null) {
      authResponse.error.toResultError()
    } else if (
      authorizationValidator.isValid(authorizationRequest, authResponse) &&
      authResponse.authorizationCode != null
    ) {
      val exchangeResponse = tokenNetworkClient.exchangeCodeForTokens(
        authorizationCode = authResponse.authorizationCode,
        codeVerifier = codeVerifier,
      )
      authStateRepository.save(exchangeResponse)
      HandyAuth.Result.Authorized
    } else {
      HandyAuth.Result.Error.UnknownError
    }
  }
}
