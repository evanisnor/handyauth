package com.evanisnor.handyauth.client.internal

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.evanisnor.handyauth.client.HandyAuth
import com.evanisnor.handyauth.client.internal.model.AuthRequest
import com.evanisnor.handyauth.client.internal.model.AuthResponse
import com.evanisnor.handyauth.client.internal.network.TokenNetworkClient
import com.evanisnor.handyauth.client.internal.secure.AuthorizationValidator
import com.evanisnor.handyauth.client.internal.state.AuthStateRepository
import com.evanisnor.handyauth.client.ui.HandyAuthActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal class InternalPendingAuthorization(
  private val tokenNetworkClient: TokenNetworkClient,
  private val authStateRepository: AuthStateRepository,
  private val authorizationValidator: AuthorizationValidator,
  private val scope: CoroutineScope,
) : HandyAuth.PendingAuthorization {
  private val codeVerifier = tokenNetworkClient.createCodeVerifier()
  private val authorizationRequest = tokenNetworkClient.buildAuthorizationRequest(codeVerifier)

  private var loginFlowLauncher: ActivityResultLauncher<AuthRequest>? = null
  private var authorizationResult: MutableSharedFlow<HandyAuth.Result> = MutableSharedFlow()

  fun prepare(callingFragment: Fragment) {
    loginFlowLauncher = HandyAuthActivity.registerForResult(callingFragment, this::launchAuthResponseHandling)
  }

  fun prepare(callingActivity: ComponentActivity) {
    loginFlowLauncher = HandyAuthActivity.registerForResult(callingActivity, this::launchAuthResponseHandling)
  }

  override suspend fun authorize(): HandyAuth.Result {
    loginFlowLauncher?.launch(authorizationRequest)
    return authorizationResult.conflate().first()
  }

  private fun launchAuthResponseHandling(authResponse: AuthResponse?) {
    scope.launch {
      authResponse?.let {
        val result = parseAuthorizationResponse(
          authorizationRequest,
          authResponse,
          codeVerifier,
        )
        authorizationResult.emit(result)
      }
    }
  }

  private suspend fun parseAuthorizationResponse(
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
