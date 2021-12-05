package com.evanisnor.handyauth.client

import android.content.Context
import androidx.activity.ComponentActivity
import com.evanisnor.handyauth.client.internal.InternalHandyAuth

interface HandyAuth {

    companion object {
        fun create(config: HandyAuthConfig): HandyAuth = InternalHandyAuth(config)
    }

    val isAuthorized: Boolean

    fun authorize(callingActivity: ComponentActivity)

    suspend fun accessToken(context: Context): HandyAccessToken

    fun logout(context: Context)
}