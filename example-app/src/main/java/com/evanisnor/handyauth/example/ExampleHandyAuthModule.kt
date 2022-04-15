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
        clientId = "3db837a49d3c4df0a277b0adaa748d87",
        redirectUrl = "com.evanisnor.freshwaves://authorize",
        authorizationUrl = "https://accounts.spotify.com/authorize",
        tokenUrl = "https://accounts.spotify.com/api/token",
        scopes = listOf("user-top-read", "user-read-private", "user-read-email")
    )

    @DelicateCoroutinesApi
    @Provides
    fun handyAuth(application: Application, handyAuthConfig: HandyAuthConfig): HandyAuth =
        HandyAuth.create(
            application = application,
            config = handyAuthConfig
        )

}