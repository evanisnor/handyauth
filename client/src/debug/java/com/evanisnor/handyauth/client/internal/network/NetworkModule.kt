package com.evanisnor.handyauth.client.internal.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal class DefaultNetworkModule : NetworkModule {

    override fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

}