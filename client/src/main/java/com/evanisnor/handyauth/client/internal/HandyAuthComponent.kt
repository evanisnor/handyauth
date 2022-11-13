package com.evanisnor.handyauth.client.internal

import android.app.Application
import android.content.Context
import com.evanisnor.handyauth.client.HandyAuth
import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.internal.model.ExchangeResponseJsonAdapter
import com.evanisnor.handyauth.client.internal.model.RefreshResponseJsonAdapter
import com.evanisnor.handyauth.client.internal.network.DefaultNetworkModule
import com.evanisnor.handyauth.client.internal.network.NetworkModule
import com.evanisnor.handyauth.client.internal.network.TokenNetworkClient
import com.evanisnor.handyauth.client.internal.secure.AuthorizationValidator
import com.evanisnor.handyauth.client.internal.secure.CodeGenerator
import com.evanisnor.handyauth.client.internal.secure.DefaultSecureModule
import com.evanisnor.handyauth.client.internal.secure.SecureModule
import com.evanisnor.handyauth.client.internal.state.AuthStateCache
import com.evanisnor.handyauth.client.internal.state.AuthStateRepository
import com.evanisnor.handyauth.client.internal.state.DefaultStateModule
import com.evanisnor.handyauth.client.internal.state.StateModule
import com.evanisnor.handyauth.client.internal.state.model.AuthStateJsonAdapter
import com.evanisnor.handyauth.client.internal.time.InstantFactory
import com.squareup.moshi.Moshi
import kotlinx.coroutines.DelicateCoroutinesApi
import okhttp3.OkHttpClient

/**
 * Dependency graph for HandyAuth instances.
 *
 * Patterns used for DI in HandyAuth resemble those that may be produced by Dagger, however
 * HandyAuth does not depend on Dagger. This decision was made to keep HandyAuth light and reduce
 * usage of third-party dependencies
 */
internal class HandyAuthComponent(
  private val context: Context,
  private val config: HandyAuthConfig,
  private val stateModule: StateModule,
  private val secureModule: SecureModule,
  private val networkModule: NetworkModule,
) {

  /**
   * Build the HandyAuth dependency graph. Custom dependency modules may be provided for testing
   * purposes.
   */
  internal class Builder {

    private var stateModule: StateModule = DefaultStateModule()
    private var secureModule: SecureModule = DefaultSecureModule()
    private val networkModule: NetworkModule = DefaultNetworkModule()

    fun stateModule(stateModule: StateModule): Builder {
      this.stateModule = stateModule
      return this
    }

    fun secureModule(secureModule: SecureModule): Builder {
      this.secureModule = secureModule
      return this
    }

    fun build(application: Application, config: HandyAuthConfig): HandyAuthComponent =
      HandyAuthComponent(
        context = application,
        config = config,
        stateModule = stateModule,
        secureModule = secureModule,
        networkModule = networkModule,
      )
  }

  // region Graph Composition

  // region State Module

  private val moshi: Moshi = stateModule.moshi()
  private val instantFactory: InstantFactory = stateModule.instantFactory()
  private val authStateJsonAdapter: AuthStateJsonAdapter = stateModule.authStateJsonAdapter(moshi)

  internal val persistentCache: AuthStateCache =
    stateModule.persistentCache(context, config, authStateJsonAdapter)

  internal val memoryCache: AuthStateCache = stateModule.memoryCache(persistentCache)

  private val authStateRepository = AuthStateRepository(
    instantFactory = instantFactory,
    cache = memoryCache,
  )

  // endregion

  // region Secure Module

  private val authorizationValidator: AuthorizationValidator =
    secureModule.authorizationValidator()
  private val codeGenerator: CodeGenerator = secureModule.codeGenerator()

  // endregion

  // region Network Module

  private val okHttpClient: OkHttpClient = networkModule.okHttpClient()
  private val exchangeResponseJsonAdapter: ExchangeResponseJsonAdapter =
    networkModule.exchangeResponseJsonAdapter(moshi)
  private val refreshResponseJsonAdapter: RefreshResponseJsonAdapter =
    networkModule.refreshResponseJsonAdapter(moshi)
  private val tokenNetworkClient: TokenNetworkClient = networkModule.tokenNetworkClient(
    config = config,
    codeGenerator = codeGenerator,
    okHttpClient = okHttpClient,
    exchangeResponseJsonAdapter = exchangeResponseJsonAdapter,
    refreshResponseJsonAdapter = refreshResponseJsonAdapter,
  )

  // endregion

  // endregion

  /**
   * An instance of HandyAuth, created with dependencies as provided by this [HandyAuthComponent]
   * instance.
   */
  @DelicateCoroutinesApi
  internal val handyAuth: HandyAuth = InternalHandyAuth(
    tokenNetworkClient = tokenNetworkClient,
    authStateRepository = authStateRepository,
    authorizationValidator = authorizationValidator,
  )
}
