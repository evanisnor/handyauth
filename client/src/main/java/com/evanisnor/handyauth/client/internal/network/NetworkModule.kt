package com.evanisnor.handyauth.client.internal.network

import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.internal.model.ExchangeResponseJsonAdapter
import com.evanisnor.handyauth.client.internal.model.RefreshResponseJsonAdapter
import com.evanisnor.handyauth.client.internal.secure.CodeGenerator
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal interface NetworkModule {

    fun okHttpClient(): OkHttpClient

    fun exchangeResponseJsonAdapter(moshi: Moshi): ExchangeResponseJsonAdapter

    fun refreshResponseJsonAdapter(moshi: Moshi): RefreshResponseJsonAdapter

    fun internalNetworkClient(
        config: HandyAuthConfig,
        codeGenerator: CodeGenerator,
        okHttpClient: OkHttpClient,
        exchangeResponseJsonAdapter: ExchangeResponseJsonAdapter,
        refreshResponseJsonAdapter: RefreshResponseJsonAdapter
    ): InternalNetworkClient

}

internal class DefaultNetworkModule : NetworkModule {

    override fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    override fun exchangeResponseJsonAdapter(moshi: Moshi): ExchangeResponseJsonAdapter =
        ExchangeResponseJsonAdapter(moshi)

    override fun refreshResponseJsonAdapter(moshi: Moshi): RefreshResponseJsonAdapter =
        RefreshResponseJsonAdapter(moshi)

    override fun internalNetworkClient(
        config: HandyAuthConfig,
        codeGenerator: CodeGenerator,
        okHttpClient: OkHttpClient,
        exchangeResponseJsonAdapter: ExchangeResponseJsonAdapter,
        refreshResponseJsonAdapter: RefreshResponseJsonAdapter
    ): InternalNetworkClient = InternalNetworkClient(
        config = config,
        codeGenerator = codeGenerator,
        client = okHttpClient,
        exchangeResponseJsonAdapter = exchangeResponseJsonAdapter,
        refreshResponseJsonAdapter = refreshResponseJsonAdapter
    )
}