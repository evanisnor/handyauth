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
        "access_denied" -> HandyAuth.Result.Error.Denied
        is String -> HandyAuth.Result.Error.ParameterError(error, description, uri)
        else -> when (statusCode) {
            is Int -> when (statusCode) {
                401 -> HandyAuth.Result.Error.Denied
                403 -> HandyAuth.Result.Error.Denied
                else -> HandyAuth.Result.Error.ServerError(statusCode)
            }
            else -> HandyAuth.Result.Error.UnknownError
        }
    }

}