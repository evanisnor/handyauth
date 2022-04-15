package com.evanisnor.handyauth.client.internal.network

import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.internal.model.ExchangeResponseJsonAdapter
import com.evanisnor.handyauth.client.internal.model.RefreshResponseJsonAdapter
import com.evanisnor.handyauth.client.internal.secure.CodeGenerator
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient

internal interface NetworkModule {

    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    fun exchangeResponseJsonAdapter(moshi: Moshi): ExchangeResponseJsonAdapter =
        ExchangeResponseJsonAdapter(moshi)

    fun refreshResponseJsonAdapter(moshi: Moshi): RefreshResponseJsonAdapter =
        RefreshResponseJsonAdapter(moshi)

    fun tokenNetworkClient(
        config: HandyAuthConfig,
        codeGenerator: CodeGenerator,
        okHttpClient: OkHttpClient,
        exchangeResponseJsonAdapter: ExchangeResponseJsonAdapter,
        refreshResponseJsonAdapter: RefreshResponseJsonAdapter
    ): TokenNetworkClient = TokenNetworkClient(
        config = config,
        codeGenerator = codeGenerator,
        client = okHttpClient,
        exchangeResponseJsonAdapter = exchangeResponseJsonAdapter,
        refreshResponseJsonAdapter = refreshResponseJsonAdapter
    )

}
