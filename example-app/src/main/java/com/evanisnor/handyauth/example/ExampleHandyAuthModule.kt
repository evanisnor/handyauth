package com.evanisnor.handyauth.example

import android.app.Application
import com.evanisnor.handyauth.client.HandyAuth
import com.evanisnor.handyauth.client.HandyAuthConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.DelicateCoroutinesApi

@Module
@InstallIn(SingletonComponent::class)
object ExampleHandyAuthModule {

    @Provides
    fun handyAuthConfig(): HandyAuthConfig = HandyAuthConfig(
        clientId = "",
        redirectUrl = "",
        authorizationUrl = "",
        tokenUrl = "",
        scopes = listOf()
    )

    @DelicateCoroutinesApi
    @Provides
    fun handyAuth(application: Application, handyAuthConfig: HandyAuthConfig): HandyAuth =
        HandyAuth.create(
            application = application,
            config = handyAuthConfig
        )

}