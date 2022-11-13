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

@RunWith(AndroidJUnit4::class)
class AuthorizationStateTests {

  private val fakeAuthServerRobot = FakeAuthServerRobot()
  private val handyAuthRobot = HandyAuthRobot()

  @Test
  fun afterAuthorizationSuccess_UserIsAuthorized() = runTest {
    val server = FakeAuthorizationServer()
    val config = handyAuthRobot.createFakeConfig(server)
    handyAuthRobot.createTestHandyAuthComponent(
      config = config,
      testAuthorizationValidator = TestAuthorizationValidator(),
    ).use { component ->
      val handyAuth: HandyAuth = component.handyAuth
      fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)

      handyAuthRobot.performAuthorization(handyAuth)
      server.waitForThisManyRequests(3)

      assertThat(handyAuth.isAuthorized).isTrue()
    }
  }

  @Test
  fun afterInvalidAuthorization_UserIsNotAuthorized() = runTest {
    val authorizationValidator = TestAuthorizationValidator()
    val server = FakeAuthorizationServer()
    val config = handyAuthRobot.createFakeConfig(server)
    handyAuthRobot.createTestHandyAuthComponent(
      config = config,
      testAuthorizationValidator = authorizationValidator,
    ).use { component ->
      authorizationValidator.isValid = false

      val handyAuth: HandyAuth = component.handyAuth
      fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)

      handyAuthRobot.performAuthorization(handyAuth)
      server.waitForThisManyRequests(2)

      assertThat(handyAuth.isAuthorized).isFalse()
    }
  }

  @Test
  fun afterAuthorizationSuccess_AccessTokenIsAvailable() = runTest {
    val server = FakeAuthorizationServer()
    val config = handyAuthRobot.createFakeConfig(server)
    handyAuthRobot.createTestHandyAuthComponent(
      config = config,
      testAuthorizationValidator = TestAuthorizationValidator(),
    ).use { component ->
      val handyAuth: HandyAuth = component.handyAuth
      fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)

      handyAuthRobot.performAuthorization(handyAuth)
      server.waitForThisManyRequests(3)

      assertThat(handyAuth.accessToken()).isEqualTo(
        HandyAccessToken(
          token = "exchange-response-access-token",
          tokenType = "Fake",
        ),
      )
    }
  }
}
