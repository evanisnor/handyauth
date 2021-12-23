package com.evanisnor.handyauth.client.internal.network

import com.evanisnor.handyauth.client.HandyAuthConfig
import com.evanisnor.handyauth.client.internal.model.ExchangeResponseJsonAdapter
import com.evanisnor.handyauth.client.internal.model.RefreshResponseJsonAdapter
import com.evanisnor.handyauth.client.internal.secure.CodeGenerator
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal class DefaultNetworkModule : NetworkModule {

    override fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

}