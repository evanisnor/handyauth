package com.evanisnor.handyauth.client.fakeserver

import com.evanisnor.handyauth.client.HandyAuthConfig
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.net.URLEncoder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class FakeAuthorizationServer {

    private val moshi = Moshi.Builder().build()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val mockWebServer = MockWebServer()
    val mockWebServerUrl = mockWebServer.url("/").toUrl()

    fun waitForThisManyRequests(n: Int) {
        val latch = CountDownLatch(1)
        scope.launch {
            while (mockWebServer.requestCount < n) {
                delay(100)
            }
            latch.countDown()
        }
        latch.await(20L, TimeUnit.SECONDS)
    }

    fun acceptAuthorizationRequest(
        config: HandyAuthConfig,
        response: FakeAuthorizationResponse = FakeAuthorizationResponse()
    ) {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(302)
                .addHeader(
                    "Location",
                    "${config.redirectUrl}?code=${response.code}&state=${response.state}"
                )
        )
    }

    fun denyAuthorizationRequest() {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(401)
        )
    }

    fun errorAuthorizationRequest(
        config: HandyAuthConfig,
        error: String,
        errorDescription: String,
        errorUri: String
    ) {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(302)
                .addHeader(
                    "Location",
                    "${config.redirectUrl}?" +
                            "error=${error.urlEncode()}&" +
                            "error_description=${errorDescription.urlEncode()}&" +
                            "error_uri=${errorUri.urlEncode()}"
                )
        )
    }

    fun returnServerError() {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(500)
        )
    }

    fun acceptExchangeRequest(response: FakeExchangeResponse) {
        mockWebServer.enqueue(
            MockResponse().setBody(
                FakeExchangeResponseJsonAdapter(moshi).toJson(response)
            )
        )
    }

    fun acceptRefreshRequest(response: FakeRefreshResponse) {
        mockWebServer.enqueue(
            MockResponse().setBody(
                FakeRefreshResponseJsonAdapter(moshi).toJson(response)
            )
        )
    }

    fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")
}