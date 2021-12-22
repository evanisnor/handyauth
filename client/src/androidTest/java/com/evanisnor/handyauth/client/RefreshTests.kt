package com.evanisnor.handyauth.client

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.evanisnor.handyauth.client.fakes.TestAuthorizationValidator
import com.evanisnor.handyauth.client.fakes.TestInstantFactory
import com.evanisnor.handyauth.client.fakeserver.FakeAuthServerRobot
import com.evanisnor.handyauth.client.fakeserver.FakeAuthorizationServer
import com.evanisnor.handyauth.client.robot.HandyAuthRobot
import com.google.common.truth.Truth
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class RefreshTests {

    private val fakeAuthServerRobot = FakeAuthServerRobot()
    private val handyAuthRobot = HandyAuthRobot()

    @Test
    fun afterTokenRefresh_WhenTokenIsNotExpired_CurrentAccessTokenIsAvailable() = runBlocking {
        val server = FakeAuthorizationServer()
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testInstantFactory = TestInstantFactory(),
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth = component.handyAuth
            fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)
            fakeAuthServerRobot.setupFreshAccessToken(server)
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

    @Test
    fun afterTokenRefresh_WhenTokenIsExpired_FreshAccessTokenIsAvailable() = runBlocking {
        val testInstantFactory = TestInstantFactory()
        val server = FakeAuthorizationServer()
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testInstantFactory = testInstantFactory,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth = component.handyAuth
            fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)
            fakeAuthServerRobot.setupFreshAccessToken(server)
            handyAuthRobot.performAuthorization(handyAuth)
            server.waitForThisManyRequests(2)
            // Current time to compare to exchange-response token expiry - After, expired
            testInstantFactory.now = Instant.ofEpochSecond(2000L)

            Truth.assertThat(handyAuth.accessToken()).isEqualTo(
                HandyAccessToken(
                    token = "refresh-response-access-token",
                    tokenType = "Fake"
                )
            )
        }
    }
}