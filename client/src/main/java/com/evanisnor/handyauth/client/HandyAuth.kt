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

    fun authorize(callingActivity: ComponentActivity, resultCallback: (Result) -> Unit)

    suspend fun accessToken(): HandyAccessToken

    suspend fun logout()

    sealed interface Result {
        sealed class Success : Result

        sealed class Error(
            val error: String?,
            val description: String?,
            val uri: String?
        ) : Result

        object Authorized : Success()
        object ServerError : Error("Server is unreachable", null, null)

        class Denied(error: String, description: String?, uri: String?) :
            Error(error, description, uri)

        class ParameterError(error: String?, description: String?, uri: String?) :
            Error(error, description, uri)

        class UnknownError(error: String?, description: String?, uri: String?) :
            Error(error, description, uri) {
            constructor() : this("An unknown error has occurred", null, null)
        }
    }
}