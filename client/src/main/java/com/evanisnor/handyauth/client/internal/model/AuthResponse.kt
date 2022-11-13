package com.evanisnor.handyauth.client.internal.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthResponse(
  val authorizationCode: String? = null,
  val state: String? = null,
  val error: RemoteError? = null,
) : Parcelable
