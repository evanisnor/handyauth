package com.evanisnor.handyauth.client.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import androidx.fragment.app.Fragment
import com.evanisnor.handyauth.client.databinding.HandyAuthActivityBinding
import com.evanisnor.handyauth.client.internal.AuthResponseContract
import com.evanisnor.handyauth.client.internal.browser.WebAuthorizationHandler
import com.evanisnor.handyauth.client.internal.ext.getParcelableExtraCompat
import com.evanisnor.handyauth.client.internal.ext.queryIntentActivitiesCompat
import com.evanisnor.handyauth.client.internal.ext.resolveServiceCompat
import com.evanisnor.handyauth.client.internal.model.AuthRequest
import com.evanisnor.handyauth.client.internal.model.AuthResponse

/**
 * HandyAuthActivity is presented to the user during the authorization flow.
 */
class HandyAuthActivity : AppCompatActivity() {

  companion object {
    internal const val authorizationRequestExtra: String =
      "com.evanisnor.handyauth.client.ui.authorizationRequest"

    internal const val authorizationResponseExtra: String =
      "com.evanisnor.handyauth.client.ui.authorizationResponse"

    fun registerForResult(
      callingFragment: Fragment,
      onAuthResponse: (AuthResponse?) -> Unit,
    ) = callingFragment.registerForActivityResult(AuthResponseContract(), onAuthResponse)

    fun registerForResult(
      callingActivity: ComponentActivity,
      onAuthResponse: (AuthResponse?) -> Unit,
    ) = callingActivity.registerForActivityResult(AuthResponseContract(), onAuthResponse)

    fun startWithResponseUri(
      callingActivity: ComponentActivity,
      responseUri: Uri,
    ) {
      callingActivity.startActivity(
        Intent(
          callingActivity,
          HandyAuthActivity::class.java,
        ).apply {
          data = responseUri
          flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        },
      )
    }
  }

  private var authRequest: AuthRequest? = null
  private var webAuthorizationHandler: WebAuthorizationHandler? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    authRequest = intent.getParcelableExtraCompat(authorizationRequestExtra, AuthRequest::class)
    webAuthorizationHandler = WebAuthorizationHandler(
      redirectUrl = authRequest?.config?.redirectUrl ?: "",
      onAuthResponse = { response ->
        setResult(
          Activity.RESULT_OK,
          Intent().apply {
            putExtra(authorizationResponseExtra, response)
          },
        )
        finish()
      },
    )
  }

  override fun onResume() {
    super.onResume()

    intent.data?.let { authorizationUri ->
      webAuthorizationHandler?.onAuthorizationResponse(authorizationUri.toString())
      finish()
    }
  }

  override fun onPostResume() {
    super.onPostResume()

    if (isFinishing) {
      return
    }

    authRequest?.authorizationUri?.let { authorizationUri ->

      if (hasCustomTabBrowser()) {
        CustomTabsIntent.Builder()
          .build()
          .launchUrl(this@HandyAuthActivity, authorizationUri)
      } else {
        HandyAuthActivityBinding.inflate(layoutInflater).apply {
          setContentView(webView)

          webAuthorizationHandler?.let {
            webView.apply(it.webViewConfig)
          }

          webView.loadUrl(authorizationUri.toString())
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    setIntent(intent)
  }

  private fun hasCustomTabBrowser(): Boolean {
    val browsableIntent = Intent().apply {
      action = Intent.ACTION_VIEW
      addCategory(Intent.CATEGORY_BROWSABLE)
      data = Uri.fromParts("http", "", null)
    }

    return packageManager.queryIntentActivitiesCompat(browsableIntent, 0).any { resolveInfo ->
      val customTabsIntent = Intent().apply {
        action = CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
        `package` = resolveInfo.activityInfo.packageName
      }
      packageManager.resolveServiceCompat(customTabsIntent, 0) != null
    }
  }
}
