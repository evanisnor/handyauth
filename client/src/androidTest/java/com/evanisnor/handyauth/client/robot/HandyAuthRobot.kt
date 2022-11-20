package com.evanisnor.handyauth.client.robot

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import com.evanisnor.handyauth.client.HandyAuth
import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.fakes.TestAuthorizationValidator
import com.evanisnor.handyauth.client.fakes.TestHandyAuthComponent
import com.evanisnor.handyauth.client.fakes.TestInstantFactory
import com.evanisnor.handyauth.client.fakes.TestLoginActivity
import com.evanisnor.handyauth.client.fakes.TestSecureModule
import com.evanisnor.handyauth.client.fakes.TestStateModule
import com.evanisnor.handyauth.client.fakeserver.FakeAuthorizationServer
import com.evanisnor.handyauth.client.internal.HandyAuthComponent
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class HandyAuthRobot {

  companion object {
    /**
     * Protocol scheme for the androidTest application, as registered in AndroidManifest.xml
     *
     * Used here to send a VIEW intent with the OAuth Redirect to the test app when performing
     * the authorization flow.
     */
    private const val appScheme: String = "test.app"
    const val redirectUrl: String = "$appScheme://redirect"
  }

  // region HandyAuthConfig

  private fun createFakeConfig(): HandyAuthConfig =
    HandyAuthConfig(
      clientId = "test-id",
      redirectUrl = redirectUrl,
      authorizationUrl = "https://fake.com/authorization",
      tokenUrl = "https://fake.com/token",
      scopes = listOf("test_scope_a", "test_scope_b"),
    )

  fun createFakeConfig(server: FakeAuthorizationServer): HandyAuthConfig =
    HandyAuthConfig(
      clientId = "test-id",
      redirectUrl = redirectUrl,
      authorizationUrl = "${server.mockWebServerUrl}authorization",
      tokenUrl = "${server.mockWebServerUrl}token",
      scopes = listOf("test_scope_a", "test_scope_b"),
    )

  // endregion

  internal fun createTestHandyAuthComponent(
    config: HandyAuthConfig = createFakeConfig(),
    testInstantFactory: TestInstantFactory? = null,
    testAuthorizationValidator: TestAuthorizationValidator? = null,
  ): TestHandyAuthComponent {
    val application = ApplicationProvider.getApplicationContext() as Application
    val handyAuthComponent = HandyAuthComponent.Builder()
      .stateModule(
        TestStateModule(
          testInstantFactory = testInstantFactory,
        ),
      )
      .secureModule(
        TestSecureModule(
          testAuthorizationValidator = testAuthorizationValidator,
        ),
      )
      .build(application, config)
    return TestHandyAuthComponent(handyAuthComponent)
  }

  internal suspend fun performAuthorization(handyAuth: HandyAuth): HandyAuth.Result =
    suspendCoroutine { continuation ->
      launchActivity<TestLoginActivity>()
        .moveToState(Lifecycle.State.CREATED)
        .onActivity { activity ->

          activity.lifecycleScope.launchWhenCreated {
            // Start the authorization flow - this will launch a browser
            val result = handyAuth.authorize(activity)
            continuation.resume(result)
          }
        }
        .moveToState(Lifecycle.State.RESUMED)
    }
}
