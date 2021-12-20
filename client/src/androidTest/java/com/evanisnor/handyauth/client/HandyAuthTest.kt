package com.evanisnor.handyauth.client

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.evanisnor.handyauth.client.fakeserver.FakeAuthServerRobot
import com.evanisnor.handyauth.client.fakeserver.FakeAuthorizationServer
import com.evanisnor.handyauth.client.internal.InternalHandyAuth
import com.evanisnor.handyauth.client.util.TestAuthorizationValidator
import com.evanisnor.handyauth.client.util.TestInstantFactory
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class HandyAuthTest {

    private val fakeAuthServerRobot = FakeAuthServerRobot()
    private val handyAuthRobot = HandyAuthRobot()

    @Test
    fun handyAuthInterfaceCreate_ReturnsInternalHandyAuth() {
        handyAuthRobot.createTestHandyAuthComponent().use { component ->
            assertThat(component.handyAuth).isInstanceOf(InternalHandyAuth::class.java)
        }
    }

    @Test
    fun beforeAuthorization_UserIsNotAuthorized() {
        handyAuthRobot.createTestHandyAuthComponent().use { component ->
            val handyAuth = component.handyAuth
            assertThat(handyAuth.isAuthorized).isFalse()
        }
    }

    @Test
    fun beforeAuthorization_AccessTokenIsBlank() = runBlocking {
        handyAuthRobot.createTestHandyAuthComponent().use { component ->
            val handyAuth = component.handyAuth
            assertThat(handyAuth.accessToken()).isEqualTo(HandyAccessToken())
        }
    }

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

            assertThat(handyAuth.isAuthorized).isTrue()

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

            assertThat(handyAuth.isAuthorized).isFalse()
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

            assertThat(handyAuth.isAuthorized).isFalse()
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

            assertThat(handyAuth.accessToken()).isEqualTo(
                HandyAccessToken(
                    token = "exchange-response-access-token",
                    tokenType = "Fake"
                )
            )
        }
    }

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

            assertThat(handyAuth.accessToken()).isEqualTo(
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
            testInstantFactory.now = Instant.ofEpochMilli(2000L)

            assertThat(handyAuth.accessToken()).isEqualTo(
                HandyAccessToken(
                    token = "refresh-response-access-token",
                    tokenType = "Fake"
                )
            )
        }
    }

    @Test
    fun afterLogout_WhereInstanceIsSame_UserIsNotAuthenticated() = runBlocking {
        val server = FakeAuthorizationServer()
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth = component.handyAuth
            fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)
            handyAuthRobot.performAuthorization(handyAuth)
            server.waitForThisManyRequests(2)
            handyAuth.logout()

            assertThat(handyAuth.isAuthorized).isFalse()
            assertThat(handyAuth.accessToken()).isEqualTo(HandyAccessToken())
        }
    }

    @Test
    fun afterLogout_WhereInstanceIsNew_UserIsNotAuthenticated(): Unit = runBlocking {
        val server = FakeAuthorizationServer()
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth = component.handyAuth
            fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)
            handyAuthRobot.performAuthorization(handyAuth)
            server.waitForThisManyRequests(2)
            handyAuth.logout()

            // Create a new instance
            handyAuthRobot.createTestHandyAuthComponent(
                config = config
            ).use { newComponent ->
                val newHandyAuth = newComponent.handyAuth
                assertThat(newHandyAuth.accessToken()).isEqualTo(HandyAccessToken())
                assertThat(newHandyAuth.isAuthorized).isFalse()
            }
        }
    }

    @Test
    fun afterMemoryCleared_AccessTokenPersists() = runBlocking {
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
            handyAuthRobot.performAuthorization(handyAuth)
            server.waitForThisManyRequests(2)

            // Create a new instance
            handyAuthRobot.createTestHandyAuthComponent(
                config = config,
                testInstantFactory = testInstantFactory
            ).use { newComponent ->
                val newHandyAuth: HandyAuth = newComponent.handyAuth

                assertThat(newHandyAuth.accessToken()).isEqualTo(
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
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testInstantFactory = testInstantFactory,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth = component.handyAuth
            fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)
            handyAuthRobot.performAuthorization(handyAuth)
            server.waitForThisManyRequests(2)

            // Create a new instance
            handyAuthRobot.createTestHandyAuthComponent(
                config = config,
                testInstantFactory = testInstantFactory
            ).use { newComponent ->
                val newHandyAuth: HandyAuth = newComponent.handyAuth
                fakeAuthServerRobot.setupFreshAccessToken(server)
                // Current time to compare to exchange-response token expiry - After, expired
                testInstantFactory.now = Instant.ofEpochMilli(2000L)

                assertThat(newHandyAuth.accessToken()).isEqualTo(
                    HandyAccessToken(
                        token = "refresh-response-access-token",
                        tokenType = "Fake"
                    )
                )
            }
        }
    }


}