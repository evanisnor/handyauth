package com.evanisnor.handyauth.client

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.evanisnor.handyauth.client.fakes.TestAuthorizationValidator
import com.evanisnor.handyauth.client.fakeserver.FakeAuthServerRobot
import com.evanisnor.handyauth.client.fakeserver.FakeAuthorizationServer
import com.evanisnor.handyauth.client.robot.HandyAuthRobot
import com.google.common.truth.Truth
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogoutTests {

  private val fakeAuthServerRobot = FakeAuthServerRobot()
  private val handyAuthRobot = HandyAuthRobot()

  @Test
  fun afterLogout_WhereInstanceIsSame_UserIsNotAuthenticated() = runTest {
    val server = FakeAuthorizationServer()
    val config = handyAuthRobot.createFakeConfig(server)
    handyAuthRobot.createTestHandyAuthComponent(
      config = config,
      testAuthorizationValidator = TestAuthorizationValidator(),
    ).use { component ->
      val handyAuth = component.handyAuth
      fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)

      handyAuthRobot.performAuthorization(handyAuth)
      server.waitForThisManyRequests(3)
      handyAuth.logout()

      Truth.assertThat(handyAuth.isAuthorized).isFalse()
      Truth.assertThat(handyAuth.accessToken()).isEqualTo(HandyAccessToken())
    }
  }

  @Test
  fun afterLogout_WhereInstanceIsNew_UserIsNotAuthenticated(): Unit = runTest {
    val server = FakeAuthorizationServer()
    val config = handyAuthRobot.createFakeConfig(server)
    handyAuthRobot.createTestHandyAuthComponent(
      config = config,
      testAuthorizationValidator = TestAuthorizationValidator(),
    ).use { component ->
      val handyAuth = component.handyAuth
      fakeAuthServerRobot.setupSuccessfulAuthorization(server, config)

      handyAuthRobot.performAuthorization(handyAuth)
      server.waitForThisManyRequests(3)
      handyAuth.logout()

      // Create a new instance
      handyAuthRobot.createTestHandyAuthComponent(
        config = config,
      ).use { newComponent ->
        val newHandyAuth = newComponent.handyAuth
        Truth.assertThat(newHandyAuth.accessToken()).isEqualTo(HandyAccessToken())
        Truth.assertThat(newHandyAuth.isAuthorized).isFalse()
      }
    }
  }
}
