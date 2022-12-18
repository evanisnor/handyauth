package com.evanisnor.handyauth.client.internal.network

import java.io.IOException
import java.nio.charset.Charset
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

sealed interface CallResult {

  data class Response(
    val response: okhttp3.Response,
  ) : CallResult

  data class Error(
    val body: String?,
    val error: Throwable,
  ) : CallResult
}

suspend fun Call.send(): CallResult = suspendCancellableCoroutine { continuation ->
  enqueue(
    object : Callback {
      override fun onResponse(call: Call, response: Response) {
        continuation.resume(CallResult.Response(response)) { error ->
          CallResult.Error(
            response.body?.source()?.readString(Charset.forName("UTF-8")),
            error)
        }
      }

      override fun onFailure(call: Call, e: IOException) {
        continuation.resume(CallResult.Error(null, e)) { error ->
          CallResult.Error(
            null,
            error)
        }
      }
    },
  )
}
