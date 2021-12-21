package com.evanisnor.handyauth.client.internal

import android.app.Application
import android.content.Context
import com.evanisnor.handyauth.client.HandyAuth
import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.internal.model.ExchangeResponseJsonAdapter
import com.evanisnor.handyauth.client.internal.model.RefreshResponseJsonAdapter
import com.evanisnor.handyauth.client.internal.network.DefaultNetworkModule
import com.evanisnor.handyauth.client.internal.network.InternalNetworkClient
import com.evanisnor.handyauth.client.internal.network.NetworkModule
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
import okhttp3.OkHttpClient

internal class HandyAuthComponent(
    private val context: Context,
    private val config: HandyAuthConfig,
    private val stateModule: StateModule,
    private val secureModule: SecureModule,
    private val networkModule: NetworkModule
) {

    class Builder {

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
                networkModule = networkModule
            )

    }

    // State Module
    private val moshi: Moshi = stateModule.moshi()
    private val instantFactory: InstantFactory = stateModule.instantFactory()
    private val authStateJsonAdapter: AuthStateJsonAdapter = stateModule.authStateJsonAdapter(moshi)

    val persistentCache: AuthStateCache =
        stateModule.persistentCache(context, config, authStateJsonAdapter)

    private val memoryCache: AuthStateCache = stateModule.memoryCache(persistentCache)

    private val authStateRepository = AuthStateRepository(
        instantFactory = instantFactory,
        cache = memoryCache
    )

    // Secure Module
    private val authorizationValidator: AuthorizationValidator =
        secureModule.authorizationValidator()
    private val codeGenerator: CodeGenerator = secureModule.codeGenerator()

    // Network Module
    private val okHttpClient: OkHttpClient = networkModule.okHttpClient()
    private val exchangeResponseJsonAdapter: ExchangeResponseJsonAdapter =
        networkModule.exchangeResponseJsonAdapter(moshi)
    private val refreshResponseJsonAdapter: RefreshResponseJsonAdapter =
        networkModule.refreshResponseJsonAdapter(moshi)
    private val internalNetworkClient: InternalNetworkClient = networkModule.internalNetworkClient(
        config = config,
        codeGenerator = codeGenerator,
        okHttpClient = okHttpClient,
        exchangeResponseJsonAdapter = exchangeResponseJsonAdapter,
        refreshResponseJsonAdapter = refreshResponseJsonAdapter
    )


    val handyAuth: HandyAuth = InternalHandyAuth(
        internalNetworkClient = internalNetworkClient,
        authStateRepository = authStateRepository,
        authorizationValidator = authorizationValidator
    )

}