package com.evanisnor.handyauth.client

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.evanisnor.handyauth.client.fakeserver.FakeAuthorizationServer
import com.evanisnor.handyauth.client.fakeserver.FakeExchangeResponse
import com.evanisnor.handyauth.client.fakeserver.FakeRefreshResponse
import com.evanisnor.handyauth.client.internal.AuthStateRepository
import com.evanisnor.handyauth.client.internal.AuthStateRepository.Companion.STATE_PREFS_NAME
import com.evanisnor.handyauth.client.internal.InternalHandyAuth
import com.evanisnor.handyauth.client.util.TestAuthorizationValidator
import com.evanisnor.handyauth.client.util.TestInstantFactory
import com.evanisnor.handyauth.client.util.TestLoginActivity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.time.ExperimentalTime

@RunWith(AndroidJUnit4::class)
class HandyAuthTest {

    @After
    fun cleanupState() {
        ApplicationProvider.getApplicationContext<Context>().apply {
            getSharedPreferences(STATE_PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                clear()
                apply()
            }
        }
    }

    @Test
    fun handyAuthInterfaceCreate_ReturnsInternalHandyAuth() {
        val config = createFakeConfig()
        val handyAuth = HandyAuth.create(config)

        assertThat(handyAuth).isInstanceOf(InternalHandyAuth::class.java)
    }

    @Test
    fun beforeAuthorization_UserIsNotAuthorized() {
        val config = createFakeConfig()
        val handyAuth: HandyAuth = InternalHandyAuth(config)

        assertThat(handyAuth.isAuthorized).isFalse()
    }

    @Test
    fun beforeAuthorization_AccessTokenIsBlank() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = createFakeConfig()
        val handyAuth: HandyAuth = InternalHandyAuth(
            config = config,
            scope = CoroutineScope(TestCoroutineDispatcher())
        )

        runBlocking {
            assertThat(handyAuth.accessToken(context)).isEqualTo(HandyAccessToken())
        }
    }

    @Test
    fun afterAuthorizationSuccess_UserIsAuthorized() {
        val server = FakeAuthorizationServer()
        val config = createFakeConfig(server)
        val handyAuth: HandyAuth = InternalHandyAuth(
            config = config,
            scope = CoroutineScope(TestCoroutineDispatcher()),
            authorizationValidator = TestAuthorizationValidator()
        )
        setupSuccessfulAuthorization(server, config)

        testAuthorization(handyAuth)

        server.waitForThisManyRequests(2)
        runBlocking {
            delay(200)
            assertThat(handyAuth.isAuthorized).isTrue()
        }
    }

    @Test
    fun afterAuthorizationError_UserIsNotAuthorized() {
        val server = FakeAuthorizationServer()
        val config = createFakeConfig(server)
        val handyAuth: HandyAuth = InternalHandyAuth(
            config = config,
            scope = CoroutineScope(TestCoroutineDispatcher())
        )
        setupFailedAuthorization(server)

        testAuthorization(handyAuth)

        server.waitForThisManyRequests(1)
        assertThat(handyAuth.isAuthorized).isFalse()
    }

    @Test
    fun afterInvalidAuthorization_UserIsNotAuthorized() {
        val authorizationValidator = TestAuthorizationValidator()
        val server = FakeAuthorizationServer()
        val config = createFakeConfig(server)
        val handyAuth: HandyAuth = InternalHandyAuth(
            config = config,
            scope = CoroutineScope(TestCoroutineDispatcher()),
            authorizationValidator = authorizationValidator
        )
        setupSuccessfulAuthorization(server, config)
        authorizationValidator.isValid = false

        testAuthorization(handyAuth)

        server.waitForThisManyRequests(1)
        runBlocking {
            assertThat(handyAuth.isAuthorized).isFalse()
        }
    }

    @Test
    fun afterAuthorizationSuccess_AccessTokenIsAvailable() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val server = FakeAuthorizationServer()
        val config = createFakeConfig(server)
        val handyAuth: HandyAuth = InternalHandyAuth(
            config = config,
            scope = CoroutineScope(TestCoroutineDispatcher()),
            authorizationValidator = TestAuthorizationValidator()
        )
        setupSuccessfulAuthorization(server, config)

        testAuthorization(handyAuth)

        server.waitForThisManyRequests(2)
        runBlocking {
            delay(200)
            assertThat(handyAuth.accessToken(context)).isEqualTo(
                HandyAccessToken(
                    token = "exchange-response-access-token",
                    tokenType = "Fake"
                )
            )
        }
    }

    @ExperimentalTime
    @Test
    fun afterTokenRefresh_WhenTokenIsNotExpired_CurrentAccessTokenIsAvailable() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testInstantFactory = TestInstantFactory()
        val server = FakeAuthorizationServer()
        val config = createFakeConfig(server)
        val handyAuth: HandyAuth = InternalHandyAuth(
            config = config,
            scope = CoroutineScope(TestCoroutineDispatcher()),
            authStateRepository = AuthStateRepository(
                instantFactory = testInstantFactory
            ),
            instantFactory = testInstantFactory,
            authorizationValidator = TestAuthorizationValidator()
        )
        setupSuccessfulAuthorization(server, config)
        setupFreshAccessToken(server)

        testAuthorization(handyAuth)

        server.waitForThisManyRequests(2)

        runBlocking {
            assertThat(handyAuth.accessToken(context)).isEqualTo(
                HandyAccessToken(
                    token = "exchange-response-access-token",
                    tokenType = "Fake"
                )
            )
        }
    }

    @ExperimentalTime
    @Test
    fun afterTokenRefresh_WhenTokenIsExpired_FreshAccessTokenIsAvailable() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testInstantFactory = TestInstantFactory()
        val server = FakeAuthorizationServer()
        val config = createFakeConfig(server)
        val handyAuth: HandyAuth = InternalHandyAuth(
            config = config,
            scope = CoroutineScope(TestCoroutineDispatcher()),
            authStateRepository = AuthStateRepository(
                instantFactory = testInstantFactory
            ),
            instantFactory = testInstantFactory,
            authorizationValidator = TestAuthorizationValidator()
        )
        setupSuccessfulAuthorization(server, config)
        setupFreshAccessToken(server)
        // Exchange-response token expiry

        testAuthorization(handyAuth)

        server.waitForThisManyRequests(2)

        // Current time to compare to exchange-response token expiry - After, expired
        testInstantFactory.now = Instant.ofEpochMilli(2000L)
        runBlocking {
            delay(200)
            assertThat(handyAuth.accessToken(context)).isEqualTo(
                HandyAccessToken(
                    token = "refresh-response-access-token",
                    tokenType = "Fake"
                )
            )
        }
    }

    @Test
    fun afterLogout_WhereInstanceIsSame_UserIsNotAuthenticated() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val server = FakeAuthorizationServer()
        val config = createFakeConfig(server)
        val handyAuth: HandyAuth = InternalHandyAuth(
            config = config,
            scope = CoroutineScope(TestCoroutineDispatcher()),
            authorizationValidator = TestAuthorizationValidator()
        )
        setupSuccessfulAuthorization(server, config)

        testAuthorization(handyAuth)

        server.waitForThisManyRequests(2)
        runBlocking {
            handyAuth.logout(context)

            assertThat(handyAuth.isAuthorized).isFalse()
            assertThat(handyAuth.accessToken(context)).isEqualTo(HandyAccessToken())
        }
    }

    @Test
    fun afterLogout_WhereInstanceIsNew_UserIsNotAuthenticated() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val server = FakeAuthorizationServer()
        val config = createFakeConfig(server)
        val handyAuth: HandyAuth = InternalHandyAuth(
            config = config,
            scope = CoroutineScope(TestCoroutineDispatcher()),
            authorizationValidator = TestAuthorizationValidator()
        )
        setupSuccessfulAuthorization(server, config)

        testAuthorization(handyAuth)

        server.waitForThisManyRequests(2)
        runBlocking {
            handyAuth.logout(context)

            delay(200)

            InternalHandyAuth(
                config = config
            ).apply {
                accessToken(context)
                assertThat(isAuthorized).isFalse()
                assertThat(accessToken(context)).isEqualTo(HandyAccessToken())
            }
        }
    }

    @Test
    fun afterMemoryCleared_AccessTokenPersists() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testInstantFactory = TestInstantFactory()
        val server = FakeAuthorizationServer()
        val config = createFakeConfig(server)
        val handyAuth: HandyAuth = InternalHandyAuth(
            config = config,
            scope = CoroutineScope(TestCoroutineDispatcher()),
            authStateRepository = AuthStateRepository(
                instantFactory = testInstantFactory
            ),
            instantFactory = testInstantFactory,
            authorizationValidator = TestAuthorizationValidator()
        )
        setupSuccessfulAuthorization(server, config)

        testAuthorization(handyAuth)

        server.waitForThisManyRequests(2)

        //----------------------------------------

        val newHandyAuth: HandyAuth = InternalHandyAuth(
            config = config,
            authStateRepository = AuthStateRepository(
                instantFactory = testInstantFactory
            )
        )

        runBlocking {
            assertThat(newHandyAuth.accessToken(context)).isEqualTo(
                HandyAccessToken(
                    token = "exchange-response-access-token",
                    tokenType = "Fake"
                )
            )
        }
    }

    @Test
    fun afterMemoryCleared_WhenTokenExpired_AccessTokenRefreshes() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testInstantFactory = TestInstantFactory()
        val server = FakeAuthorizationServer()
        val config = createFakeConfig(server)
        val handyAuth: HandyAuth = InternalHandyAuth(
            config = config,
            scope = CoroutineScope(TestCoroutineDispatcher()),
            authStateRepository = AuthStateRepository(
                instantFactory = testInstantFactory
            ),
            instantFactory = testInstantFactory,
            authorizationValidator = TestAuthorizationValidator()
        )
        setupSuccessfulAuthorization(server, config)

        testAuthorization(handyAuth)

        server.waitForThisManyRequests(2)

        //----------------------------------------

        val newHandyAuth: HandyAuth = InternalHandyAuth(
            config = config,
            authStateRepository = AuthStateRepository(
                instantFactory = testInstantFactory
            )
        )
        setupFreshAccessToken(server)

        // Current time to compare to exchange-response token expiry - After, expired
        testInstantFactory.now = Instant.ofEpochMilli(2000L)
        runBlocking {
            assertThat(newHandyAuth.accessToken(context)).isEqualTo(
                HandyAccessToken(
                    token = "refresh-response-access-token",
                    tokenType = "Fake"
                )
            )
        }
    }


    private fun createFakeConfig(): HandyAuthConfig =
        HandyAuthConfig(
            clientId = "test-id",
            redirectUrl = "my.app://redirect",
            authorizationUrl = "https://fake.com/authorization",
            tokenUrl = "https://fake.com/token",
            scopes = listOf("test_scope_a", "test_scope_b")
        )

    private fun createFakeConfig(server: FakeAuthorizationServer): HandyAuthConfig =
        HandyAuthConfig(
            clientId = "test-id",
            redirectUrl = "my.app://redirect",
            authorizationUrl = "${server.mockWebServerUrl}authorization",
            tokenUrl = "${server.mockWebServerUrl}token",
            scopes = listOf("test_scope_a", "test_scope_b")
        )

    private fun createExchangeResponse(): FakeExchangeResponse =
        FakeExchangeResponse(
            accessToken = "exchange-response-access-token",
            refreshToken = "test-refresh-token",
            tokenType = "Fake",
            expiresIn = 1000L,
            scope = "test_scope_a test_scope_b"
        )

    private fun createRefreshResponse(): FakeRefreshResponse =
        FakeRefreshResponse(
            accessToken = "refresh-response-access-token",
            tokenType = "Fake",
            expiresIn = 1000L,
            scope = "test_scope_a test_scope_b"
        )

    private fun setupSuccessfulAuthorization(
        server: FakeAuthorizationServer,
        config: HandyAuthConfig
    ) {
        // test server enqueue Location redirect with code & state
        server.acceptAuthorizationRequest(config)
        // test server enqueue exchange response
        server.acceptExchangeRequest(
            response = createExchangeResponse()
        )
    }

    private fun setupFailedAuthorization(server: FakeAuthorizationServer) {
        // test server return 401
        server.denyAuthorizationRequest()
    }

    private fun setupFreshAccessToken(server: FakeAuthorizationServer) {
        // test server enqueue fresh access token
        server.acceptRefreshRequest(response = createRefreshResponse())
    }

    private fun testAuthorization(handyAuth: HandyAuth) {
        launchActivity<TestLoginActivity>()
            .moveToState(Lifecycle.State.CREATED)
            .onActivity { activity ->
                handyAuth.authorize(activity)
            }
            .moveToState(Lifecycle.State.RESUMED)
    }


}