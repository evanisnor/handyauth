package com.evanisnor.handyauth.client

import android.content.Context
import com.evanisnor.handyauth.client.internal.state.AuthStateCache
import com.evanisnor.handyauth.client.internal.state.DefaultStateModule
import com.evanisnor.handyauth.client.internal.state.StateModule
import com.evanisnor.handyauth.client.internal.state.model.AuthStateJsonAdapter
import com.evanisnor.handyauth.client.internal.time.InstantFactory
import com.evanisnor.handyauth.client.util.TestInstantFactory
import com.squareup.moshi.Moshi

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