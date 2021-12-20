package com.evanisnor.handyauth.client

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.evanisnor.handyauth.client.fakeserver.FakeAuthorizationServer
import com.evanisnor.handyauth.client.fakeserver.FakeExchangeResponse
import com.evanisnor.handyauth.client.fakeserver.FakeRefreshResponse
import com.evanisnor.handyauth.client.internal.HandyAuthComponent
import com.evanisnor.handyauth.client.internal.InternalHandyAuth
import com.evanisnor.handyauth.client.internal.secure.AuthorizationValidator
import com.evanisnor.handyauth.client.internal.secure.CodeGenerator
import com.evanisnor.handyauth.client.internal.secure.DefaultSecureModule
import com.evanisnor.handyauth.client.internal.secure.SecureModule
import com.evanisnor.handyauth.client.internal.state.*
import com.evanisnor.handyauth.client.internal.state.model.AuthStateJsonAdapter
import com.evanisnor.handyauth.client.internal.time.InstantFactory
import com.evanisnor.handyauth.client.util.TestAuthorizationValidator
import com.evanisnor.handyauth.client.util.TestInstantFactory
import com.evanisnor.handyauth.client.util.TestLoginActivity
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.time.ExperimentalTime

@RunWith(AndroidJUnit4::class)
class HandyAuthTest {

    lateinit var context: Context
//    lateinit var persistentCache: AuthStateCache
//    lateinit var handyAuth: HandyAuth

    internal class TestStateModule(
        private val defaultStateModule: DefaultStateModule = DefaultStateModule(),
        private val testInstantFactory: TestInstantFactory?
    ) : StateModule {

        override fun instantFactory(): InstantFactory =
            testInstantFactory ?: defaultStateModule.instantFactory()

        override fun moshi(): Moshi = defaultStateModule.moshi()

        override fun memoryCache(
            persistentCache: AuthStateCache
        ): AuthStateCache =
            defaultStateModule.memoryCache(persistentCache)

        override fun persistentCache(
            context: Context,
            config: HandyAuthConfig,
            authStateJsonAdapter: AuthStateJsonAdapter
        ): AuthStateCache =
            defaultStateModule.persistentCache(context, config, authStateJsonAdapter)

        override fun authStateJsonAdapter(
            moshi: Moshi
        ): AuthStateJsonAdapter =
            defaultStateModule.authStateJsonAdapter(moshi)
    }

    internal class TestSecureModule(
        private val defaultSecureModule: DefaultSecureModule = DefaultSecureModule(),
        private val testAuthorizationValidator: TestAuthorizationValidator?
    ) : SecureModule {


        override fun authorizationValidator(): AuthorizationValidator =
            testAuthorizationValidator ?: defaultSecureModule.authorizationValidator()

        override fun codeGenerator(): CodeGenerator = defaultSecureModule.codeGenerator()
    }

    private fun createTestHandyAuthComponent(
        config: HandyAuthConfig = createFakeConfig(),
        testInstantFactory: TestInstantFactory? = null,
        testAuthorizationValidator: TestAuthorizationValidator? = null
    ): HandyAuthComponent {
        val application = ApplicationProvider.getApplicationContext() as Application
        return HandyAuthComponent.Builder()
            .stateModule(
                TestStateModule(
                    testInstantFactory = testInstantFactory
                )
            )
            .secureModule(
                TestSecureModule(
                    testAuthorizationValidator = testAuthorizationValidator
                )
            )
            .build(application, config)
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun handyAuthInterfaceCreate_ReturnsInternalHandyAuth() {
        createTestHandyAuthComponent().use { component ->
            assertThat(component.handyAuth).isInstanceOf(InternalHandyAuth::class.java)
        }
    }

    @Test
    fun beforeAuthorization_UserIsNotAuthorized() {
        createTestHandyAuthComponent().use { component ->
            val handyAuth = component.handyAuth
            assertThat(handyAuth.isAuthorized).isFalse()
        }
    }

    @Test
    fun beforeAuthorization_AccessTokenIsBlank() = runBlocking {
        createTestHandyAuthComponent().use { component ->
            val handyAuth = component.handyAuth
            assertThat(handyAuth.accessToken()).isEqualTo(HandyAccessToken())
        }
    }

    @Test
    fun afterAuthorizationSuccess_UserIsAuthorized() = runBlocking {
        val server = FakeAuthorizationServer()
        val config = createFakeConfig(server)
        createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth: HandyAuth = component.handyAuth
            setupSuccessfulAuthorization(server, config)
            performAuthorization(handyAuth)
            server.waitForThisManyRequests(2)

            assertThat(handyAuth.isAuthorized).isTrue()

        }
    }

    @Test
    fun afterAuthorizationError_UserIsNotAuthorized() = runBlocking {
        val server = FakeAuthorizationServer()
        createTestHandyAuthComponent().use { component ->
            val handyAuth = component.handyAuth
            setupFailedAuthorization(server)
            performAuthorization(handyAuth)

            server.waitForThisManyRequests(1)
            assertThat(handyAuth.isAuthorized).isFalse()
        }
    }

    @Test
    fun afterInvalidAuthorization_UserIsNotAuthorized() = runBlocking {
        val authorizationValidator = TestAuthorizationValidator()
        val server = FakeAuthorizationServer()
        val config = createFakeConfig(server)
        createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = authorizationValidator
        ).use { component ->
            authorizationValidator.isValid = false

            val handyAuth: HandyAuth = component.handyAuth
            setupSuccessfulAuthorization(server, config)
            performAuthorization(handyAuth)
            server.waitForThisManyRequests(1)

            assertThat(handyAuth.isAuthorized).isFalse()
        }
    }

    @Test
    fun afterAuthorizationSuccess_AccessTokenIsAvailable() = runBlocking {
        val server = FakeAuthorizationServer()
        val config = createFakeConfig(server)
        createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth: HandyAuth = component.handyAuth
            setupSuccessfulAuthorization(server, config)
            performAuthorization(handyAuth)
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
        val config = createFakeConfig(server)
        createTestHandyAuthComponent(
            config = config,
            testInstantFactory = TestInstantFactory(),
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth = component.handyAuth
            setupSuccessfulAuthorization(server, config)
            setupFreshAccessToken(server)
            performAuthorization(handyAuth)
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
        val config = createFakeConfig(server)
        createTestHandyAuthComponent(
            config = config,
            testInstantFactory = testInstantFactory,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth = component.handyAuth
            setupSuccessfulAuthorization(server, config)
            setupFreshAccessToken(server)
            performAuthorization(handyAuth)
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
        val config = createFakeConfig(server)
        createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth = component.handyAuth
            setupSuccessfulAuthorization(server, config)
            performAuthorization(handyAuth)
            server.waitForThisManyRequests(2)
            handyAuth.logout()

            assertThat(handyAuth.isAuthorized).isFalse()
            assertThat(handyAuth.accessToken()).isEqualTo(HandyAccessToken())
        }
    }

    @Test
    fun afterLogout_WhereInstanceIsNew_UserIsNotAuthenticated(): Unit = runBlocking {
        val server = FakeAuthorizationServer()
        val config = createFakeConfig(server)
        createTestHandyAuthComponent(
            config = config,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth = component.handyAuth
            setupSuccessfulAuthorization(server, config)
            performAuthorization(handyAuth)
            server.waitForThisManyRequests(2)
            handyAuth.logout()

            // Create a new instance
            createTestHandyAuthComponent(
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
        val config = createFakeConfig(server)
        createTestHandyAuthComponent(
            config = config,
            testInstantFactory = testInstantFactory,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth = component.handyAuth
            setupSuccessfulAuthorization(server, config)
            performAuthorization(handyAuth)
            server.waitForThisManyRequests(2)

            // Create a new instance
            createTestHandyAuthComponent(
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
        val config = createFakeConfig(server)
        createTestHandyAuthComponent(
            config = config,
            testInstantFactory = testInstantFactory,
            testAuthorizationValidator = TestAuthorizationValidator()
        ).use { component ->
            val handyAuth = component.handyAuth
            setupSuccessfulAuthorization(server, config)
            performAuthorization(handyAuth)
            server.waitForThisManyRequests(2)

            // Create a new instance
            createTestHandyAuthComponent(
                config = config,
                testInstantFactory = testInstantFactory
            ).use { newComponent ->
                val newHandyAuth: HandyAuth = newComponent.handyAuth
                setupFreshAccessToken(server)
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

    private fun performAuthorization(handyAuth: HandyAuth) {
        launchActivity<TestLoginActivity>()
            .moveToState(Lifecycle.State.CREATED)
            .onActivity { activity ->
                handyAuth.authorize(activity)
            }
            .moveToState(Lifecycle.State.RESUMED)
    }


}