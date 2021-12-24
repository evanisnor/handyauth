package com.evanisnor.handyauth.client.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.evanisnor.handyauth.client.databinding.HandyAuthActivityBinding
import com.evanisnor.handyauth.client.internal.AuthResponseContract
import com.evanisnor.handyauth.client.internal.model.AuthRequest
import com.evanisnor.handyauth.client.internal.model.AuthResponse
import com.evanisnor.handyauth.client.internal.webview.WebAuthorizationHandler

/**
 * HandyAuthActivity is presented to the user during the authorization flow. A WebView is used to
 * load the server's Authorization URL and handle the responses.
 */
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRequest: AuthRequest? = intent.getParcelableExtra(authorizationRequestExtra)

        val webAuthorizationInterpreter = WebAuthorizationHandler(
            redirectUrl = authRequest?.config?.redirectUrl ?: "",
            onAuthResponse = { response ->
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(authorizationResponseExtra, response)
                })
                finish()
            }
        )

        HandyAuthActivityBinding.inflate(layoutInflater).apply {
            setContentView(webView)
            webView.apply(webAuthorizationInterpreter.webViewConfig)

            authRequest?.authorizationUrl.let {
                webView.loadUrl(it.toString())
            }
        }
    }
}
