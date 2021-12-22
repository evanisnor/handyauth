package com.evanisnor.handyauth.client.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.evanisnor.handyauth.client.databinding.HandyAuthActivityBinding
import com.evanisnor.handyauth.client.internal.AuthResponseContract
import com.evanisnor.handyauth.client.internal.model.AuthRequest
import com.evanisnor.handyauth.client.internal.model.AuthResponse
import com.evanisnor.handyauth.client.internal.model.RemoteError
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class HandyAuthActivity : AppCompatActivity() {

    companion object {
        internal const val authorizationRequestExtra: String = "authorizationRequest"
        internal const val authorizationResponseExtra: String = "authorizationResponse"

        fun start(
            callingActivity: ComponentActivity,
            authorizationRequest: AuthRequest,
            onAuthResponse: (AuthResponse?) -> Unit
        ) {
            callingActivity.registerForActivityResult(AuthResponseContract(), onAuthResponse)
                .launch(authorizationRequest)
        }
    }

    private var authRequest: AuthRequest? = null
    private var redirectUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authRequest = intent.getParcelableExtra(authorizationRequestExtra)
        redirectUrl = authRequest?.config?.redirectUrl

        HandyAuthActivityBinding.inflate(layoutInflater).apply {
            setContentView(webView)
            webView.apply(config)

            authRequest?.authorizationUrl.let {
                webView.loadUrl(it.toString())
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private val config: WebView.() -> Unit = {

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        webViewClient = object : WebViewClient() {


            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val urlString = request?.url.toString()
                redirectUrl?.let { redirectUrl ->
                    if (urlString.startsWith(redirectUrl)) {
                        handleAuthorizationResponse(
                            redirectUrl,
                            urlString
                        ) { response ->
                            setResult(Activity.RESULT_OK, Intent().apply {
                                putExtra(authorizationResponseExtra, response)
                            })
                        }
                        finish()
                        return false
                    }
                }

                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                if (errorResponse != null) {
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(
                            authorizationResponseExtra,
                            AuthResponse(
                                error = RemoteError(errorResponse.statusCode)
                            )
                        )
                    })
                    finish()
                    return
                }
            }

        }
    }

    private fun handleAuthorizationResponse(
        redirectUrl: String,
        url: String,
        onAuthResponse: (AuthResponse) -> Unit
    ) {
        url.replace(redirectUrl, "https://ok").toHttpUrlOrNull()?.let {
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
}
