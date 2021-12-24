package com.evanisnor.handyauth.client

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.net.URLEncoder

/**
 * OAuth2 Configuration
 */
@Parcelize
data class HandyAuthConfig(
    val clientId: String,
    val redirectUrl: String,
    val authorizationUrl: Uri,
    val tokenUrl: Uri,
    val scopes: List<String>,
) : Parcelable {

    constructor(
        clientId: String,
        redirectUrl: String,
        authorizationUrl: String,
        tokenUrl: String,
        scopes: List<String>
    ) : this(
        clientId,
        redirectUrl,
        Uri.parse(authorizationUrl),
        Uri.parse(tokenUrl),
        scopes
    )

    @IgnoredOnParcel
    internal val encodedRedirectUrl: String = URLEncoder.encode(redirectUrl, "utf-8")
}
