package com.evanisnor.handyauth.client

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import com.evanisnor.handyauth.client.internal.HandyAuthComponent
import com.evanisnor.handyauth.client.util.TestAuthorizationValidator
import com.evanisnor.handyauth.client.util.TestInstantFactory
import com.evanisnor.handyauth.client.util.TestLoginActivity

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
    ): HandyAuthComponent {
        val application = ApplicationProvider.getApplicationContext() as Application
        return HandyAuthComponent.Builder()
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
    }


     fun performAuthorization(handyAuth: HandyAuth) {
        launchActivity<TestLoginActivity>()
            .moveToState(Lifecycle.State.CREATED)
            .onActivity { activity ->
                handyAuth.authorize(activity)
            }
            .moveToState(Lifecycle.State.RESUMED)
    }
}