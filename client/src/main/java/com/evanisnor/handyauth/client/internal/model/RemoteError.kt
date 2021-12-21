package com.evanisnor.handyauth.client.internal.model

import android.os.Parcelable
import com.evanisnor.handyauth.client.HandyAuth
import kotlinx.parcelize.Parcelize

@Parcelize
data class RemoteError(
    val statusCode: Int? = 0,
    val error: String? = null,
    val description: String? = null,
    val uri: String? = null
) : Parcelable {

    fun toResultError(): HandyAuth.Result.Error = when (error) {
        "access_denied" -> HandyAuth.Result.Denied(error, description, uri)
        is String -> HandyAuth.Result.ParameterError(error, description, uri)
        else -> when (statusCode) {
            in 500..599 -> HandyAuth.Result.ServerError
            else -> HandyAuth.Result.UnknownError(error, description, uri)
        }
    }

}