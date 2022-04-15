package com.evanisnor.handyauth.client.internal.network

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


sealed interface CallResult {

    data class Response(
        val response: okhttp3.Response
    ) : CallResult

    data class Error(
        val error: Throwable
    ) : CallResult

}


suspend fun Call.send(): CallResult = suspendCancellableCoroutine { continuation ->
    enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            continuation.resume(CallResult.Response(response)) { error ->
                CallResult.Error(error)
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            continuation.resume(CallResult.Error(e)) { error ->
                CallResult.Error(error)
            }
        }
    })
}