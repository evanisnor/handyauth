package com.evanisnor.handyauth.client.internal

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.evanisnor.handyauth.client.HandyAccessToken
import com.evanisnor.handyauth.client.HandyAuth
import com.evanisnor.handyauth.client.internal.network.TokenNetworkClient
import com.evanisnor.handyauth.client.internal.secure.AuthorizationValidator
import com.evanisnor.handyauth.client.internal.state.AuthStateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext

internal class InternalHandyAuth @DelicateCoroutinesApi constructor(
  private val tokenNetworkClient: TokenNetworkClient,
  private val authStateRepository: AuthStateRepository,
  private val authorizationValidator: AuthorizationValidator,
  private val scope: CoroutineScope = CoroutineScope(newSingleThreadContext("HandyAuth") + SupervisorJob()),
) : HandyAuth {

  override val isAuthorized: Boolean
    get() = authStateRepository.isAuthorized

  override suspend fun prepareLoginUserFlow(callingFragment: Fragment) : HandyAuth.PendingAuthorization = InternalPendingAuthorization(
    tokenNetworkClient, authStateRepository, authorizationValidator, scope
  ).apply {
    prepare(callingFragment)
  }

  override suspend fun prepareLoginUserFlow(callingActivity: ComponentActivity) : HandyAuth.PendingAuthorization = InternalPendingAuthorization(
    tokenNetworkClient, authStateRepository, authorizationValidator, scope
  ).apply {
    prepare(callingActivity)
  }

  override suspend fun authorize(callingFragment: Fragment): HandyAuth.Result =
    prepareLoginUserFlow(callingFragment).authorize()

  override suspend fun authorize(callingActivity: ComponentActivity): HandyAuth.Result =
    prepareLoginUserFlow(callingActivity).authorize()

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
}
