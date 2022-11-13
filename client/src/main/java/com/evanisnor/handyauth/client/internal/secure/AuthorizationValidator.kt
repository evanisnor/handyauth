package com.evanisnor.handyauth.client.internal.secure

import com.evanisnor.handyauth.client.internal.model.AuthRequest
import com.evanisnor.handyauth.client.internal.model.AuthResponse

interface AuthorizationValidator {
  fun isValid(authRequest: AuthRequest, authResponse: AuthResponse?): Boolean
}

class DefaultAuthorizationValidator : AuthorizationValidator {

  override fun isValid(authRequest: AuthRequest, authResponse: AuthResponse?): Boolean =
    authResponse != null && authRequest.state == authResponse.state
}
