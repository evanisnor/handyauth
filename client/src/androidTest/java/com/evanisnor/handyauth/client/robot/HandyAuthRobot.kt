package com.evanisnor.handyauth.client.robot

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import com.evanisnor.handyauth.client.HandyAuth
import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.fakes.*
import com.evanisnor.handyauth.client.fakeserver.FakeAuthorizationServer
import com.evanisnor.handyauth.client.internal.HandyAuthComponent

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
            scopes = listOf("test_scope_a", "test_scope_b")
        )

    fun createFakeConfig(server: FakeAuthorizationServer): HandyAuthConfig =
        HandyAuthConfig(
            clientId = "test-id",
            redirectUrl = redirectUrl,
            authorizationUrl = "${server.mockWebServerUrl}authorization",
            tokenUrl = "${server.mockWebServerUrl}token",
            scopes = listOf("test_scope_a", "test_scope_b")
        )

    // endregion

    internal fun createTestHandyAuthComponent(
        config: HandyAuthConfig = createFakeConfig(),
        testInstantFactory: TestInstantFactory? = null,
        testAuthorizationValidator: TestAuthorizationValidator? = null
    ): TestHandyAuthComponent {
        val application = ApplicationProvider.getApplicationContext() as Application
        val handyAuthComponent = HandyAuthComponent.Builder()
            .stateModule(
                TestStateModule(
                    testInstantFactory = testInstantFactory
                )
            )
            .secureModule(
                TestSecureModule(
                    testAuthorizationValidator = testAuthorizationValidator
                )
            )
            .build(application, config)
        return TestHandyAuthComponent(handyAuthComponent)
    }

    internal fun performAuthorization(
        handyAuth: HandyAuth,
        resultCallback: (HandyAuth.Result) -> Unit = {}
    ) {
        launchActivity<TestLoginActivity>()
            .moveToState(Lifecycle.State.CREATED)
            .onActivity { activity ->

                // Start the authorization flow - this will launch a browser
                handyAuth.authorize(activity, resultCallback)
            }
            .moveToState(Lifecycle.State.RESUMED)
    }

}