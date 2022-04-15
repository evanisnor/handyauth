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
class PersistenceTests {

    private val fakeAuthServerRobot = FakeAuthServerRobot()
    private val handyAuthRobot = HandyAuthRobot()

    @Test
    fun afterMemoryCleared_AccessTokenPersists() = runBlocking {
        val testInstantFactory = TestInstantFactory()
        val server = FakeAuthorizationServer()
        val config = handyAuthRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testInstantFactory = testInstantFactory,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth = component.handyAuth
            fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)

            handyAuthRobot.performAuthorization(handyAuth)
            server.waitForThisManyRequests(3)

            // Create a new instance
            handyAuthRobot.createTestHandyAuthComponent(
                config = config,
                testInstantFactory = testInstantFactory
            ).use { newComponent ->
                val newHandyAuth: HandyAuth = newComponent.handyAuth

                Truth.assertThat(newHandyAuth.accessToken()).isEqualTo(
                    HandyAccessToken(
                        token = "exchange-response-access-token",
                        tokenType = "Fake"
                    )
                )
            }
        }
    }

    @Test
    fun afterMemoryCleared_WhenTokenExpired_AccessTokenRefreshes() = runBlocking {
        val testInstantFactory = TestInstantFactory()
        val server = FakeAuthorizationServer()
        val config = handyAuthRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testInstantFactory = testInstantFactory,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth = component.handyAuth
            fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)
            handyAuthRobot.performAuthorization(handyAuth)
            server.waitForThisManyRequests(3)

            // Create a new instance
            handyAuthRobot.createTestHandyAuthComponent(
                config = config,
                testInstantFactory = testInstantFactory
            ).use { newComponent ->
                val newHandyAuth: HandyAuth = newComponent.handyAuth
                fakeAuthServerRobot.setupFreshAccessToken(server)
                // Current time to compare to exchange-response token expiry - After, expired
                testInstantFactory.now = Instant.ofEpochSecond(2000L)

                Truth.assertThat(newHandyAuth.accessToken()).isEqualTo(
                    HandyAccessToken(
                        token = "refresh-response-access-token",
                        tokenType = "Fake"
                    )
                )
            }
        }
    }
}