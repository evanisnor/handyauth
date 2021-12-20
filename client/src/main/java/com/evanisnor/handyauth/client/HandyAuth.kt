package com.evanisnor.handyauth.client

import android.app.Application
import androidx.activity.ComponentActivity
import com.evanisnor.handyauth.client.internal.HandyAuthComponent

interface HandyAuth {

    companion object {
        fun create(application: Application, config: HandyAuthConfig): HandyAuth =
            HandyAuthComponent.Builder()
                .build(application, config).handyAuth
    }

    val isAuthorized: Boolean

    fun authorize(callingActivity: ComponentActivity)

    suspend fun accessToken(): HandyAccessToken

    suspend fun logout()
}