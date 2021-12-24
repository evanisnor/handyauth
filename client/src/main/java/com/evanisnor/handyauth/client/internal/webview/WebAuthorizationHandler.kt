package com.evanisnor.handyauth.client.internal.webview

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.evanisnor.handyauth.client.internal.ext.isAuthorizationRedirect
import com.evanisnor.handyauth.client.internal.model.AuthResponse
import com.evanisnor.handyauth.client.internal.model.RemoteError
import com.evanisnor.handyauth.client.internal.ext.urlDecode
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Provides a WebView that can be used to handle OAuth2 responses from an authorization server.
 */
internal class WebAuthorizationHandler(
    private val redirectUrl: String,
    private val onAuthResponse: (AuthResponse) -> Unit
) {

    @SuppressLint("SetJavaScriptEnabled")
    val webViewConfig: WebView.() -> Unit = {

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (request.isAuthorizationRedirect(redirectUrl)) {
                    onAuthorizationResponse(request?.url?.toString())
                    return false
                }

                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                if (errorResponse != null) {
                    onHttpError(errorResponse.statusCode)
                }
            }

        }
    }

    private fun onAuthorizationResponse(url: String?) {
        url?.replace(redirectUrl, "https://ok")
            ?.toHttpUrlOrNull()
            ?.let {
                val code = it.queryParameter("code")
                val state = it.queryParameter("state")
                val error = it.queryParameter("error")
                val errorDescription = it.queryParameter("error_description")
                val errorUri = it.queryParameter("error_uri")

                if (code != null && state != null) {
                    onAuthResponse(
                        AuthResponse(
                            authorizationCode = code,
                            state = state
                        )
                    )
                } else {
                    onAuthResponse(
                        AuthResponse(
                            error = RemoteError(
                                error = error?.urlDecode(),
                                description = errorDescription?.urlDecode(),
                                uri = errorUri?.urlDecode()
                            )
                        )
                    )
                }
            }
    }

    private fun onHttpError(statusCode: Int) {
        onAuthResponse(
            AuthResponse(
                error = RemoteError(statusCode)
            )
        )
    }
}