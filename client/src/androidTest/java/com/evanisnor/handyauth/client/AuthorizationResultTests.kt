package com.evanisnor.handyauth.client

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.evanisnor.handyauth.client.fakes.TestAuthorizationValidator
import com.evanisnor.handyauth.client.fakeserver.FakeAuthServerRobot
import com.evanisnor.handyauth.client.fakeserver.FakeAuthorizationServer
import com.evanisnor.handyauth.client.robot.HandyAuthRobot
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class AuthorizationResultTests {

    private val fakeAuthServerRobot = FakeAuthServerRobot()
    private val handyAuthRobot = HandyAuthRobot()

    @Test
    fun afterAuthorizationSuccess_ResultIsSuccess() = runBlocking {
        val server = FakeAuthorizationServer()
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val receivedResult = AtomicReference<HandyAuth.Result>()

            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)
            handyAuthRobot.performAuthorization(handyAuth, receivedResult::set)
            server.waitForThisManyRequests(2)

            assertThat(receivedResult.get()).isEqualTo(HandyAuth.Result.Authorized)
        }
    }

    @Test
    fun afterAuthorizationFailed_WhenLoginIsDenied_ResultIsDenied() = runBlocking {
        val server = FakeAuthorizationServer()
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val receivedResult = AtomicReference<HandyAuth.Result>()

            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupFailedAuthorization(
                server = server,
                config = config,
                error = "access_denied",
                errorDescription = "Credentials are incorrect",
                errorUri = "https://fake.com/help"
            )
            handyAuthRobot.performAuthorization(handyAuth, receivedResult::set)
            server.waitForThisManyRequests(1)

            receivedResult.get().let { result ->
                assertThat(result).isInstanceOf(HandyAuth.Result.Denied::class.java)
                (result as HandyAuth.Result.Error).let { errorResult ->
                    assertThat(errorResult.error).isEqualTo("access_denied")
                    assertThat(errorResult.description).isEqualTo("Credentials are incorrect")
                    assertThat(errorResult.uri).isEqualTo("https://fake.com/help")
                }
            }
        }
    }

    @Test
    fun afterAuthorizationFailed_WhenLoginIsForbidden_ResultIsDenied() = runBlocking {
        val server = FakeAuthorizationServer()
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val receivedResult = AtomicReference<HandyAuth.Result>()

            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupFailedAuthorization(
                server = server,
                config = config,
                error = "access_denied",
                errorDescription = "Credentials are incorrect",
                errorUri = "https://fake.com/help"
            )
            handyAuthRobot.performAuthorization(handyAuth, receivedResult::set)
            server.waitForThisManyRequests(1)

            receivedResult.get().let { result ->
                assertThat(result).isInstanceOf(HandyAuth.Result.Denied::class.java)
                (result as HandyAuth.Result.Error).let { errorResult ->
                    assertThat(errorResult.error).isEqualTo("access_denied")
                    assertThat(errorResult.description).isEqualTo("Credentials are incorrect")
                    assertThat(errorResult.uri).isEqualTo("https://fake.com/help")
                }
            }
        }
    }

    @Test
    fun afterAuthorizationFailed_WhenRequestIsInvalid_ResultIsParameterError() = runBlocking {
        val server = FakeAuthorizationServer()
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val receivedResult = AtomicReference<HandyAuth.Result>()

            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupFailedAuthorization(
                server = server,
                config = config,
                error = "invalid_request",
                errorDescription = "Missing required parameter",
                errorUri = "https://fake.com/help"
            )
            handyAuthRobot.performAuthorization(handyAuth, receivedResult::set)
            server.waitForThisManyRequests(1)

            receivedResult.get().let { result ->
                assertThat(result).isInstanceOf(HandyAuth.Result.ParameterError::class.java)
                (result as HandyAuth.Result.Error).let { errorResult ->
                    assertThat(errorResult.error).isEqualTo("invalid_request")
                    assertThat(errorResult.description).isEqualTo("Missing required parameter")
                    assertThat(errorResult.uri).isEqualTo("https://fake.com/help")
                }
            }
        }
    }

    @Test
    fun afterAuthorizationFailed_WhenAuthMethodUnsupported_ResultIsParameterError() = runBlocking {
        val server = FakeAuthorizationServer()
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val receivedResult = AtomicReference<HandyAuth.Result>()

            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupFailedAuthorization(
                server = server,
                config = config,
                error = "unsupported_response_type",
                errorDescription = "Unsupported method",
                errorUri = "https://fake.com/help"
            )
            handyAuthRobot.performAuthorization(handyAuth, receivedResult::set)
            server.waitForThisManyRequests(1)

            receivedResult.get().let { result ->
                assertThat(result).isInstanceOf(HandyAuth.Result.ParameterError::class.java)
                (result as HandyAuth.Result.Error).let { errorResult ->
                    assertThat(errorResult.error).isEqualTo("unsupported_response_type")
                    assertThat(errorResult.description).isEqualTo("Unsupported method")
                    assertThat(errorResult.uri).isEqualTo("https://fake.com/help")
                }
            }
        }
    }

    @Test
    fun afterAuthorizationFailed_WhenInvalidClientSecret_ResultIsParameterError() = runBlocking {
        val server = FakeAuthorizationServer()
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val receivedResult = AtomicReference<HandyAuth.Result>()

            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupFailedAuthorization(
                server = server,
                config = config,
                error = "invalid_client_secret",
                errorDescription = "Client secret is invalid",
                errorUri = "https://fake.com/help"
            )
            handyAuthRobot.performAuthorization(handyAuth, receivedResult::set)
            server.waitForThisManyRequests(1)

            receivedResult.get().let { result ->
                assertThat(result).isInstanceOf(HandyAuth.Result.ParameterError::class.java)
                (result as HandyAuth.Result.Error).let { errorResult ->
                    assertThat(errorResult.error).isEqualTo("invalid_client_secret")
                    assertThat(errorResult.description).isEqualTo("Client secret is invalid")
                    assertThat(errorResult.uri).isEqualTo("https://fake.com/help")
                }
            }
        }
    }

    @Test
    fun afterAuthorizationFailed_WhenServerIsBroken_ResultIsServerError() = runBlocking {
        val server = FakeAuthorizationServer()
        val config = fakeAuthServerRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val receivedResult = AtomicReference<HandyAuth.Result>()

            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupServerError(server)
            handyAuthRobot.performAuthorization(handyAuth, receivedResult::set)
            server.waitForThisManyRequests(1)

            receivedResult.get().let { result ->
                assertThat(result).isInstanceOf(HandyAuth.Result.ServerError::class.java)
                (result as HandyAuth.Result.Error).let { errorResult ->
                    assertThat(errorResult.error).isEqualTo("Server is unreachable")
                    assertThat(errorResult.description).isNull()
                    assertThat(errorResult.uri).isNull()
                }
            }
        }
    }

}