package com.evanisnor.handyauth.client

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.evanisnor.handyauth.client.internal.InternalHandyAuth
import com.evanisnor.handyauth.client.robot.HandyAuthRobot
import com.google.common.truth.Truth
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultsTests {

  private val handyAuthRobot = HandyAuthRobot()

  @Test
  fun handyAuthInterfaceCreate_ReturnsInternalHandyAuth() {
    handyAuthRobot.createTestHandyAuthComponent().use { component ->
      Truth.assertThat(component.handyAuth).isInstanceOf(InternalHandyAuth::class.java)
    }
  }

  @Test
  fun beforeAuthorization_UserIsNotAuthorized() {
    handyAuthRobot.createTestHandyAuthComponent().use { component ->
      val handyAuth = component.handyAuth
      Truth.assertThat(handyAuth.isAuthorized).isFalse()
    }
  }

  @Test
  fun beforeAuthorization_AccessTokenIsBlank() = runTest {
    handyAuthRobot.createTestHandyAuthComponent().use { component ->
      val handyAuth = component.handyAuth
      Truth.assertThat(handyAuth.accessToken()).isEqualTo(HandyAccessToken())
    }
  }
}
