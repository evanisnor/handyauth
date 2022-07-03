package com.evanisnor.handyauth.client

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.evanisnor.handyauth.client.fakes.TestAuthorizationValidator
import com.evanisnor.handyauth.client.fakeserver.FakeAuthServerRobot
import com.evanisnor.handyauth.client.fakeserver.FakeAuthorizationServer
import com.evanisnor.handyauth.client.robot.HandyAuthRobot
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class AuthorizationResultTests {

    private val fakeAuthServerRobot = FakeAuthServerRobot()
    private val handyAuthRobot = HandyAuthRobot()

    @Test
    fun afterAuthorizationSuccess_ResultIsSuccess() = runTest {
        val server = FakeAuthorizationServer()
        val config = handyAuthRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val receivedResult = AtomicReference<HandyAuth.Result>()

            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)

            handyAuthRobot.performAuthorization(handyAuth, receivedResult::set)
            server.waitForThisManyRequests(3)

            assertThat(receivedResult.get()).isEqualTo(HandyAuth.Result.Authorized)
        }
    }

    @Test
    fun afterAuthorizationFailed_WhenLoginIsDenied_ResultIsDenied() = runTest {
        val server = FakeAuthorizationServer()
        val config = handyAuthRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val receivedResult = AtomicReference<HandyAuth.Result>()

            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupFailedAuthorization(
                server = server,
                config = config,
                expectedError = "access_denied",
                expectedErrorDescription = "Credentials are incorrect",
                expectedErrorUri = "https://fake.com/help"
            )

            handyAuthRobot.performAuthorization(handyAuth, receivedResult::set)
            server.waitForThisManyRequests(2)

            receivedResult.get().let { result ->
                assertThat(result).isSameInstanceAs(HandyAuth.Result.Error.Denied)
            }
        }
    }

    @Test
    fun afterAuthorizationFailed_WhenLoginIsForbidden_ResultIsDenied() = runTest {
        val server = FakeAuthorizationServer()
        val config = handyAuthRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val receivedResult = AtomicReference<HandyAuth.Result>()

            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupFailedAuthorization(
                server = server,
                config = config,
                expectedError = "access_denied",
                expectedErrorDescription = "Credentials are incorrect",
                expectedErrorUri = "https://fake.com/help"
            )

            handyAuthRobot.performAuthorization(handyAuth, receivedResult::set)
            server.waitForThisManyRequests(2)

            receivedResult.get().let { result ->
                assertThat(result).isSameInstanceAs(HandyAuth.Result.Error.Denied)
            }
        }
    }

    @Test
    fun afterAuthorizationFailed_WhenRequestIsInvalid_ResultIsParameterError() = runTest {
        val server = FakeAuthorizationServer()
        val config = handyAuthRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val receivedResult = AtomicReference<HandyAuth.Result>()

            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupFailedAuthorization(
                server = server,
                config = config,
                expectedError = "invalid_request",
                expectedErrorDescription = "Missing required parameter",
                expectedErrorUri = "https://fake.com/help"
            )

            handyAuthRobot.performAuthorization(handyAuth, receivedResult::set)
            server.waitForThisManyRequests(2)

            receivedResult.get().let { result ->
                assertThat(result).isInstanceOf(HandyAuth.Result.Error.ParameterError::class.java)
                (result as HandyAuth.Result.Error.ParameterError).let { errorResult ->
                    assertThat(errorResult.error).isEqualTo("invalid_request")
                    assertThat(errorResult.description).isEqualTo("Missing required parameter")
                    assertThat(errorResult.uri).isEqualTo("https://fake.com/help")
                }
            }
        }
    }

    @Test
    fun afterAuthorizationFailed_WhenAuthMethodUnsupported_ResultIsParameterError() = runTest {
        val server = FakeAuthorizationServer()
        val config = handyAuthRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val receivedResult = AtomicReference<HandyAuth.Result>()

            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupFailedAuthorization(
                server = server,
                config = config,
                expectedError = "unsupported_response_type",
                expectedErrorDescription = "Unsupported method",
                expectedErrorUri = "https://fake.com/help"
            )

            handyAuthRobot.performAuthorization(handyAuth, receivedResult::set)
            server.waitForThisManyRequests(2)

            receivedResult.get().let { result ->
                assertThat(result).isInstanceOf(HandyAuth.Result.Error.ParameterError::class.java)
                (result as HandyAuth.Result.Error.ParameterError).let { errorResult ->
                    assertThat(errorResult.error).isEqualTo("unsupported_response_type")
                    assertThat(errorResult.description).isEqualTo("Unsupported method")
                    assertThat(errorResult.uri).isEqualTo("https://fake.com/help")
                }
            }
        }
    }

    @Test
    fun afterAuthorizationFailed_WhenInvalidClientSecret_ResultIsParameterError() = runTest {
        val server = FakeAuthorizationServer()
        val config = handyAuthRobot.createFakeConfig(server)
        handyAuthRobot.createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val receivedResult = AtomicReference<HandyAuth.Result>()

            val handyAuth: HandyAuth = component.handyAuth
            fakeAuthServerRobot.setupFailedAuthorization(
                server = server,
                config = config,
                expectedError = "invalid_client_secret",
                expectedErrorDescription = "Client secret is invalid",
                expectedErrorUri = "https://fake.com/help"
            )

            handyAuthRobot.performAuthorization(handyAuth, receivedResult::set)
            server.waitForThisManyRequests(2)

            receivedResult.get().let { result ->
                assertThat(result).isInstanceOf(HandyAuth.Result.Error.ParameterError::class.java)
                (result as HandyAuth.Result.Error.ParameterError).let { errorResult ->
                    assertThat(errorResult.error).isEqualTo("invalid_client_secret")
                    assertThat(errorResult.description).isEqualTo("Client secret is invalid")
                    assertThat(errorResult.uri).isEqualTo("https://fake.com/help")
                }
            }
        }
    }

}