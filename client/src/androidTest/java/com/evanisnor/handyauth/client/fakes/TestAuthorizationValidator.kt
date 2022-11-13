package com.evanisnor.handyauth.client.fakes

import com.evanisnor.handyauth.client.internal.model.AuthRequest
import com.evanisnor.handyauth.client.internal.model.AuthResponse
import com.evanisnor.handyauth.client.internal.secure.AuthorizationValidator

class TestAuthorizationValidator : AuthorizationValidator {

  var isValid: Boolean = true

  override fun isValid(authRequest: AuthRequest, authResponse: AuthResponse?): Boolean =
    authResponse != null && isValid
}
