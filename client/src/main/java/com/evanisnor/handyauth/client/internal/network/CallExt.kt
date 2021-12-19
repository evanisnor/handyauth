package com.evanisnor.handyauth.client.internal.network

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


data class CallResult(
    val response: Response?,
    val error: Throwable?
) {
    constructor(response: Response) : this(response, null)
    constructor(error: Throwable) : this(null, error)
}


suspend fun Call.send(): CallResult = suspendCancellableCoroutine { continuation ->
    enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            continuation.resume(CallResult(response)) { error ->
                CallResult(error)
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            continuation.resume(CallResult(e)) { error ->
                CallResult(error)
            }
        }
    })
}