package com.evanisnor.handyauth.client.internal.state

import android.content.Context
import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.internal.state.model.AuthStateJsonAdapter
import com.evanisnor.handyauth.client.internal.time.DefaultInstantFactory
import com.evanisnor.handyauth.client.internal.time.InstantFactory
import com.squareup.moshi.Moshi

internal interface StateModule {

  fun instantFactory(): InstantFactory

  fun moshi(): Moshi

  fun memoryCache(
    persistentCache: AuthStateCache,
  ): AuthStateCache

  fun persistentCache(
    context: Context,
    config: HandyAuthConfig,
    authStateJsonAdapter: AuthStateJsonAdapter,
  ): AuthStateCache

  fun authStateJsonAdapter(
    moshi: Moshi,
  ): AuthStateJsonAdapter
}

internal class DefaultStateModule : StateModule {

  override fun instantFactory(): InstantFactory = DefaultInstantFactory()

  override fun moshi(): Moshi = Moshi.Builder()
    .add(InstantJsonAdapter())
    .build()

  override fun memoryCache(
    persistentCache: AuthStateCache,
  ): AuthStateCache = MemoryCache(
    persistentCache = persistentCache,
  )

  override fun persistentCache(
    context: Context,
    config: HandyAuthConfig,
    authStateJsonAdapter: AuthStateJsonAdapter,
  ): AuthStateCache = SharedPrefsCache(
    context = context,
    instanceIdentifier = config.authorizationUrl.host ?: "unknown",
    authStateJsonAdapter = authStateJsonAdapter,
  )

  override fun authStateJsonAdapter(
    moshi: Moshi,
  ): AuthStateJsonAdapter = AuthStateJsonAdapter(
    moshi = moshi,
  )
}
