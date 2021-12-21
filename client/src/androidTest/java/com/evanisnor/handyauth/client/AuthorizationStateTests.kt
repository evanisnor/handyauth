package com.evanisnor.handyauth.client

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.evanisnor.handyauth.client.fakes.TestAuthorizationValidator
import com.evanisnor.handyauth.client.fakeserver.FakeAuthServerRobot
import com.evanisnor.handyauth.client.fakeserver.FakeAuthorizationServer
import com.evanisnor.handyauth.client.robot.HandyAuthRobot
import com.google.common.truth.Truth
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthorizationStateTests {

    private val fakeAuthServerRobot = FakeAuthServerRobot()
    private val handyAuthRobot = HandyAuthRobot()

    @Test
    fun afterAuthorizationSuccess_UserIsAuthorized() = runBlocking {
        val server = FakeAuthorizationServer()
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)
            handyAuthRobot.performAuthorization(handyAuth)
            server.waitForThisManyRequests(2)

            Truth.assertThat(handyAuth.isAuthorized).isTrue()
        }
    }

    @Test
    fun afterAuthorizationError_UserIsNotAuthorized() = runBlocking {
        val server = FakeAuthorizationServer()
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config
        ).use { component ->
            val handyAuth = component.handyAuth
            fakeAuthServerRobot.setupFailedAuthorization(server)
            handyAuthRobot.performAuthorization(handyAuth)
            server.waitForThisManyRequests(1)

            Truth.assertThat(handyAuth.isAuthorized).isFalse()
        }
    }

    @Test
    fun afterInvalidAuthorization_UserIsNotAuthorized() = runBlocking {
        val authorizationValidator = TestAuthorizationValidator()
        val server = FakeAuthorizationServer()
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = authorizationValidator
        ).use { component ->
            authorizationValidator.isValid = false

            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)
            handyAuthRobot.performAuthorization(handyAuth)
            server.waitForThisManyRequests(1)

            Truth.assertThat(handyAuth.isAuthorized).isFalse()
        }
    }

    @Test
    fun afterAuthorizationSuccess_AccessTokenIsAvailable() = runBlocking {
        val server = FakeAuthorizationServer()
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)
            handyAuthRobot.performAuthorization(handyAuth)
            server.waitForThisManyRequests(2)

            Truth.assertThat(handyAuth.accessToken()).isEqualTo(
                HandyAccessToken(
                    token = "exchange-response-access-token",
                    tokenType = "Fake"
                )
            )
        }
    }
}