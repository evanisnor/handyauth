package com.evanisnor.handyauth.client.robot

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import com.evanisnor.handyauth.client.HandyAuth
import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.fakes.*
import com.evanisnor.handyauth.client.internal.HandyAuthComponent

class HandyAuthRobot {

    private fun createFakeConfig(): HandyAuthConfig =
        HandyAuthConfig(
            clientId = "test-id",
            redirectUrl = "my.app://redirect",
            authorizationUrl = "https://fake.com/authorization",
            tokenUrl = "https://fake.com/token",
            scopes = listOf("test_scope_a", "test_scope_b")
        )

    internal fun createTestHandyAuthComponent(
        config: HandyAuthConfig = createFakeConfig(),
        testInstantFactory: TestInstantFactory? = null,
        testAuthorizationValidator: TestAuthorizationValidator? = null
    ): TestHandyAuthComponent {
        val application = ApplicationProvider.getApplicationContext() as Application
        val handyAuthComponent= HandyAuthComponent.Builder()
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


    fun performAuthorization(
        handyAuth: HandyAuth,
        resultCallback: (HandyAuth.Result) -> Unit = {}
    ) {
        launchActivity<TestLoginActivity>()
            .moveToState(Lifecycle.State.CREATED)
            .onActivity { activity ->
                handyAuth.authorize(activity, resultCallback)
            }
            .moveToState(Lifecycle.State.RESUMED)
    }
}