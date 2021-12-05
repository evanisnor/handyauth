package com.evanisnor.handyauth.client.internal.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthResponse(
    val authorizationCode: String,
    val state: String
) : Parcelable