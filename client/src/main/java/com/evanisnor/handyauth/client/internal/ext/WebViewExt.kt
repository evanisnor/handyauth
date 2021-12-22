package com.evanisnor.handyauth.client.internal.ext

import android.webkit.WebResourceRequest


fun WebResourceRequest?.isAuthorizationRedirect(redirectUrl: String): Boolean =
    this != null && url.toString().startsWith(redirectUrl)