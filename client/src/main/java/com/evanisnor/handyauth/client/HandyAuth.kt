package com.evanisnor.handyauth.client

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.evanisnor.handyauth.client.internal.HandyAuthComponent
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * HandyAuth is an OAuth 2 client library for Android apps with a minimal API.
 *
 * Currently supported authorization flows:
 *  - PKCE
 */
interface HandyAuth {

  companion object {

    /**
     * Create a new instance of HandyAuth for the provided config.
     *
     * @see [HandyAuthConfig]
     */
    @DelicateCoroutinesApi
    fun create(application: Application, config: HandyAuthConfig): HandyAuth =
      HandyAuthComponent.Builder()
        .build(application, config)
        .handyAuth
  }

  /**
   * Is the user authorized with the server defined in [HandyAuthConfig]?
   */
  val isAuthorized: Boolean

  /**
   * Prepare for launching the login user flow by obtaining a [PendingAuthorization] object. You may
   * launch the user flow in a browser later by calling [PendingAuthorization.authorize]. This is
   * useful when you would like to start the authorization flow when the user clicks a button.
   * NOTE: [prepareAuthorization] can only be called during Fragment initialization (onCreate, onAttach)
   */
  suspend fun prepareAuthorization(callingFragment: Fragment) : PendingAuthorization

  /**
   * Prepare for launching the login user flow by obtaining a [PendingAuthorization] object. You may
   * launch the user flow in a browser later by calling [PendingAuthorization.authorize].
   * NOTE: [prepareAuthorization] can only be called during Activity initialization (onCreate)
   */
  suspend fun prepareAuthorization(callingActivity: ComponentActivity) : PendingAuthorization

  /**
   * Begin the authorization flow and handle the [Result]. This will launch a browser
   * that loads the server's authorization page, where the user can enter their credentials and
   * grant access to your app.
   * NOTE: [authorize] can only be called during the Fragment initialization (onCreate, onAttach)
   */
  suspend fun authorize(callingFragment: Fragment): Result

  /**
   * Begin the authorization flow and handle the [Result]. This will launch a browser
   * that loads the server's authorization page, where the user can enter their credentials and
   * grant access to your app.
   * NOTE: [authorize] can only be called during Activity initialization (onCreate)
   */
  suspend fun authorize(callingActivity: ComponentActivity): Result

  /**
   * Get the access token in the form of a [HandyAccessToken]. As long as the user [isAuthorized]
   * (true), then the access token provided here will always be valid.
   */
  suspend fun accessToken(): HandyAccessToken

  /**
   * Perform a logout, which will clear local authentication state for this instance. Afterward,
   * [isAuthorized] will return false.
   */
  suspend fun logout()

  // region Pending Authorization

  /**
   * Represents a pending login flow. To proceed with authorization, call [PendingAuthorization.authorize].
   */
  interface PendingAuthorization {

    /**
     * Begin the authorization flow and handle the [Result]. This will launch a browser
     * that loads the server's authorization page, where the user can enter their credentials and
     * grant access to your app.
     */
    suspend fun authorize() : Result

  }

  // endregion

  // region Result Types

  /**
   * Results are sealed types that are returned after executing the [authorize] flow.
   */
  sealed interface Result {

    /**
     * Authorization is successful. Calls to [accessToken] will now receive valid OAuth2 access tokens.
     */
    object Authorized : Result

    /**
     * An error occurred during the authorization flow.
     */
    sealed interface Error : Result {

      /**
       * Access has been denied by the server.
       */
      object Denied : Error

      /**
       * An error occurred on the server.
       */
      data class ServerError(val statusCode: Int) : Error

      /**
       * The server returned an OAuth 2 error. Standard OAuth 2 authorization error
       * parameters are provided.
       */
      data class ParameterError(
        val error: String,
        val description: String?,
        val uri: String?,
      ) : Error

      /**
       * An unknown error has occurred.
       */
      object UnknownError : Error
    }
  }

  // endregion
}
